package com.runicrealms.plugin.character.gui;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.CharacterSelectUtil;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.donor.DonorRank;
import com.runicrealms.plugin.model.ClassData;
import com.runicrealms.plugin.model.CoreCharacterData;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.model.ProjectedData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterDeleteEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.rdb.model.CharacterField;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the character select menu which the player sees upon login
 *
 * @author Excel, Skyfallin
 */
public class CharacterGuiManager implements Listener {
    private static final Map<UUID, CharacterGui> classMenu = new HashMap<>();
    private static final Map<UUID, Integer> deletingCharacters = new HashMap<>();
    private final Map<UUID, CharacterSelectEvent> loadingEventMap = new ConcurrentHashMap<>();

    private static boolean checkIsCharacterIcon(ItemStack item) {
        for (Map.Entry<CharacterClass, ItemStack> classIcon : CharacterSelectUtil.getClassIcons().entrySet()) {
            if (item.getType() == classIcon.getValue().getType()) {
                return true;
            }
        }
        return false;
    }

    private static String getClassNameFromIcon(ItemStack icon) {
        for (Map.Entry<CharacterClass, ItemStack> classIcon : CharacterSelectUtil.getClassIcons().entrySet()) {
            if (icon.getType() == classIcon.getValue().getType()) {
                return classIcon.getKey().getName();
            }
        }
        return null;
    }

    /**
     * Builds and opens the inventory to create a new character (alt)
     *
     * @param player to open the inventory
     */
    public static void openAddCharacterInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Choose Your Class!");
        inventory.setItem(0, CharacterSelectUtil.GO_BACK_ITEM);
        inventory.setItem(2, CharacterSelectUtil.getClassIcons().get(CharacterClass.ARCHER));
        inventory.setItem(3, CharacterSelectUtil.getClassIcons().get(CharacterClass.CLERIC));
        inventory.setItem(4, CharacterSelectUtil.getClassIcons().get(CharacterClass.MAGE));
        inventory.setItem(5, CharacterSelectUtil.getClassIcons().get(CharacterClass.ROGUE));
        inventory.setItem(6, CharacterSelectUtil.getClassIcons().get(CharacterClass.WARRIOR));
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.ADD);
        });
    }

    /**
     * Builds and opens the inventory to remove a character (alt)
     *
     * @param player    to open the inventory
     * @param classSlot of the character to be deleted
     */
    public static void openDelCharacterInventory(Player player, Integer classSlot) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.RED + "Confirm Character Deletion");
        inventory.setItem(2, CharacterSelectUtil.GO_BACK_ITEM);
        inventory.setItem(6, CharacterSelectUtil.CONFIRM_DELETION_ITEM);
        deletingCharacters.put(player.getUniqueId(), classSlot);
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.REMOVE);
        });
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getCharacterIcon(ClassData classData) {
        ItemStack item;
        if (RunicCore.getInstance().getConfig().contains("class-icons." + classData.getClassType().getName().toLowerCase() + ".damage")) {
            item = new ItemStack(
                    Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons." + classData.getClassType().getName().toLowerCase() + ".material")),
                    1,
                    (short) RunicCore.getInstance().getConfig().getInt("class-icons." + classData.getClassType().getName().toLowerCase() + ".damage"));
        } else {
            item = new ItemStack(
                    Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons." + classData.getClassType().getName().toLowerCase() + ".material")), 1);
        }
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GREEN + classData.getClassType().getName());
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        List<String> lore = new ArrayList<>(3);
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.GREEN + "" + classData.getLevel());
        lore.add(ChatColor.GRAY + "Exp: " + ChatColor.GREEN + "" + classData.getExp());
        lore.add(ChatColor.RED + "[Right click] to delete");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private BukkitTask displayLoadingTitle(Player player) {
        return new BukkitRunnable() {
            int dots = 1;
            boolean increasing = true;

            @Override
            public void run() {

                player.sendTitle(ChatColor.YELLOW + "Loading" + ".".repeat(Math.max(0, dots)), "", 0, 30, 0);

                if (increasing) {
                    dots++;
                    if (dots == 3) {
                        increasing = false;
                    }
                } else {
                    dots = 1;
                    increasing = true;
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 10L);
    }

    /**
     * Handles the logic for creating a new character profile.
     *
     * @param currentItem the itemStack in the inventory
     * @param player      the player who clicked
     */
    private void handleAddCharacter(ItemStack currentItem, Player player) {
        if (currentItem.getType() != CharacterSelectUtil.GO_BACK_ITEM.getType()) {
            CorePlayerData corePlayerData = RunicCore.getPlayerDataAPI().getCorePlayerData(player.getUniqueId());
            String className = getClassNameFromIcon(currentItem);
            TaskChain<?> chain = RunicCore.newChain();
            chain
                    .asyncFirst(() -> new ProjectedData(player))
                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to load projected data!")
                    .syncLast(projectedData -> {
                        // Open the select UI!
                        int slot = projectedData.findFirstUnusedSlot();
                        CoreCharacterData.createCoreCharacterData(corePlayerData, className, slot, () -> { // Callback function when task is complete
                            projectedData.addCharacter(new ClassData(player.getUniqueId(), CharacterClass.getFromName(className), 0, 0));
                            openSelectGui(player, projectedData);
                        });
                    })
                    .execute();
        } else {
            openSelectGui(player, new ProjectedData(player));
        }
    }

    /**
     * Handles the logic for removing a new character profile
     *
     * @param currentItem the itemStack in the inventory
     * @param player      the player who clicked
     */
    private void handleDeleteCharacter(ItemStack currentItem, Player player) {
        if (currentItem.getType() == CharacterSelectUtil.CONFIRM_DELETION_ITEM.getType()) {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                CharacterDeleteEvent event = new CharacterDeleteEvent(player, deletingCharacters.get(player.getUniqueId()));
                Bukkit.getPluginManager().callEvent(event);
                // Create a 'callback' task that waits until all plugins have deleted data
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (event.getPluginsToDeleteData().size() > 0)
                            return; // Other plugins deleting data
                        this.cancel();
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                            // Remove character from memory
                            CorePlayerData corePlayerData = RunicCore.getPlayerDataAPI().getCorePlayerData(player.getUniqueId());
                            corePlayerData.getCoreCharacterDataMap().remove(event.getSlot());
                            deletingCharacters.remove(player.getUniqueId());
                            // Update UI
                            openSelectGui(player, new ProjectedData(player));
                        });
                    }
                }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
            });
        } else {
            classMenu.put(player.getUniqueId(), CharacterGui.SELECT);
            deletingCharacters.remove(player.getUniqueId());
            openSelectGui(player, new ProjectedData(player));
        }
    }

    /**
     * Handles the logic for selecting a character profile
     *
     * @param currentItem  the itemStack in the inventory
     * @param player       the player who clicked
     * @param isRightClick whether the player right-clicked the item
     * @param eventSlot    the slot of the item in the event inventory
     */
    private void handleSelectCharacter(ItemStack currentItem, Player player, boolean isRightClick, int eventSlot) {
        if (currentItem.getType() == Material.OAK_DOOR) {
            player.kickPlayer(ChatColor.GREEN + "Come back soon!");
            return;
        }
        if (checkIsCharacterIcon(currentItem)) {
            if (isRightClick) {
                openDelCharacterInventory(player, eventSlot < 9 ? eventSlot - 1 : eventSlot - 5);
            } else {
                classMenu.remove(player.getUniqueId());
                player.closeInventory();
                int slot = eventSlot < 9 ? eventSlot - 1 : eventSlot - 5;
                // Async request a data lookup in redis / mongo and build data object then load and run event sync
                initializeCharacterObject(player, slot);
            }
        } else if (currentItem.getType() == CharacterSelectUtil.CHARACTER_CREATE_ITEM.getType()) {
            openAddCharacterInventory(player);
        }
    }

    /**
     * Creates a CoreCharacterData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param player player who is attempting to load their data
     * @param slot   the slot of the character
     */
    private void initializeCharacterObject(Player player, Integer slot) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            UUID uuid = player.getUniqueId();
            BukkitTask bukkitTask = displayLoadingTitle(player);

            CharacterSelectEvent characterSelectEvent = new CharacterSelectEvent
                    (
                            player,
                            slot,
                            RunicCore.getPlayerDataAPI().getCorePlayerData(uuid),
                            bukkitTask
                    );
            Bukkit.getPluginManager().callEvent(characterSelectEvent);
            loadingEventMap.put(player.getUniqueId(), characterSelectEvent);
        });
    }

    @EventHandler(priority = EventPriority.HIGH) // runs late to let other plugins finish
    public void onCharacterDelete(CharacterDeleteEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();
        // 1. Delete from Redis
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        String parentKey = player.getUniqueId() + ":character:" + slot;
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            // Removes all sub-keys for slot
            RunicDatabase.getAPI().getRedisAPI().removeAllFromRedis(jedis, database + ":" + parentKey);
            jedis.del(database + ":" + parentKey);
            jedis.srem(database + ":" + uuid + ":characterData", String.valueOf(slot));
            jedis.srem(database + ":" + uuid + ":skillTreeData", String.valueOf(slot));
            jedis.srem(database + ":" + uuid + ":spellData", String.valueOf(slot));
        }
        // 2. Delete from Mongo
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();

        Query query = new Query();
        query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));

        // Fetch the existing document
        CorePlayerData corePlayerData = mongoTemplate.findOne(query, CorePlayerData.class);
        if (corePlayerData == null) {
            // handle missing document error
            return;
        }

        Update update = new Update();
        // Only unset the fields that exist
        if (corePlayerData.getCoreCharacterDataMap().containsKey(slot)) {
            update.unset("coreCharacterDataMap." + slot);
        }
        if (corePlayerData.getSkillTreeDataMap().containsKey(slot)) {
            update.unset("skillTreeDataMap." + slot);
        }
        if (corePlayerData.getSpellDataMap().containsKey(slot)) {
            update.unset("spellDataMap." + slot);
        }

        mongoTemplate.updateFirst(query, update, CorePlayerData.class);
        // 3. Mark this deletion as complete
        event.getPluginsToDeleteData().remove("core");
    }

    /**
     * Handles swapping between gui inventories in the character select screen
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (classMenu.containsKey(event.getPlayer().getUniqueId())) {
            switch (classMenu.get(event.getPlayer().getUniqueId())) {
                case SELECT -> openSelectGui((Player) event.getPlayer(), new ProjectedData(player));
                case ADD -> openAddCharacterInventory((Player) event.getPlayer());
                case REMOVE ->
                        openDelCharacterInventory((Player) event.getPlayer(), deletingCharacters.get(event.getPlayer().getUniqueId()));
                default -> {
                }
            }
        }
    }

    /**
     * General event listener to handle character select screen interactions
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!classMenu.containsKey(event.getWhoClicked().getUniqueId())) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() == Material.AIR) return;
        if (classMenu.get(event.getWhoClicked().getUniqueId()) == CharacterGui.SELECT) {
            handleSelectCharacter(event.getCurrentItem(), (Player) event.getWhoClicked(), event.isRightClick(), event.getSlot());
        } else if (classMenu.get(event.getWhoClicked().getUniqueId()) == CharacterGui.ADD) {
            handleAddCharacter(event.getCurrentItem(), (Player) event.getWhoClicked());
        } else if (classMenu.get(event.getWhoClicked().getUniqueId()) == CharacterGui.REMOVE) {
            handleDeleteCharacter(event.getCurrentItem(), (Player) event.getWhoClicked());
        }
    }

    /**
     * This fixes a bug where the player disconnects during the loading process
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoadingQuit(PlayerQuitEvent event) {
        if (loadingEventMap.containsKey(event.getPlayer().getUniqueId())) {
            Bukkit.getLogger().warning("PLAYER DISCONNECT DURING LOAD, PROCESS ABORTED, CANCELLING EVENT GOOD ENDING");
            loadingEventMap.get(event.getPlayer().getUniqueId()).setCancelled(true);
            loadingEventMap.remove(event.getPlayer().getUniqueId());
        } else {
            Bukkit.getLogger().warning("QUIT EVENT BAD ENDING");
        }
        classMenu.remove(event.getPlayer().getUniqueId());
        deletingCharacters.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoadingSuccess(CharacterLoadedEvent event) {
        loadingEventMap.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Once the resource pack is loaded (or declined), we build the character select screen async
     */
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                UUID uuid = event.getPlayer().getUniqueId();
                try {
                    openSelectGui(event.getPlayer(), new ProjectedData(event.getPlayer()));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    classMenu.remove(uuid);
                }
            });
        }
    }

    /**
     * ?
     *
     * @param player
     * @param projectedData
     */
    private void openSelectGui(Player player, ProjectedData projectedData) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Select Your Character");
        int characterSlots = DonorRank.getDonorRank(player).getClassSlots();
        if (player.hasPermission("runic.team")) characterSlots = 10;
        int addedSlots = 0;
        for (int i = 1; i <= RunicDatabase.getAPI().getDataAPI().getMaxCharacterSlot(); i++) {
            if (projectedData.getPlayerCharacters().get(i) != null) {
                inventory.setItem(i <= 5 ? i + 1 : i + 5, getCharacterIcon(projectedData.getPlayerCharacters().get(i)));
                addedSlots++;
            } else {
                if (addedSlots < characterSlots) {
                    inventory.setItem(i <= 5 ? i + 1 : i + 5, CharacterSelectUtil.CHARACTER_CREATE_ITEM);
                    addedSlots++;
                } else {
                    ItemStack icon = null;
                    if (i >= 9) {
                        icon = CharacterSelectUtil.ONLY_CHAMPION_CREATE_ITEM;
                    } else if (i >= 7) {
                        icon = CharacterSelectUtil.ONLY_HERO_CREATE_ITEM;
                    } else if (i == 6) {
                        icon = CharacterSelectUtil.ONLY_KNIGHT_CREATE_ITEM;
                    }
                    if (icon != null) inventory.setItem(i + 5, icon);
                }
            }
        }
        // Set bottom row
        for (int i = 18; i < 27; i++) {
            inventory.setItem(i, GUIUtil.BORDER_ITEM);
        }
        // Set exit button
        inventory.setItem(22, CharacterSelectUtil.EXIT_GAME_ITEM);
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.SELECT);
        });
    }

}
