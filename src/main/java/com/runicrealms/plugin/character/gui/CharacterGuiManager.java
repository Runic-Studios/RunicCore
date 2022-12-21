package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.Pair;
import com.runicrealms.plugin.character.CharacterSelectUtil;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.database.DatabaseHelper;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.ShutdownSaveWrapper;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.ClassData;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Manages the character select menu which the player sees upon login
 *
 * @author Excel, Skyfallin
 */
public class CharacterGuiManager implements Listener {

    private static final Map<UUID, CharacterGui> classMenu = new HashMap<>();
    private static final Map<UUID, Integer> deletingCharacters = new HashMap<>();

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
    public static void openRemoveCharacterInventory(Player player, Integer classSlot) {
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
    public static ItemStack getCharacterIcon(ClassData character) {
        ItemStack item;
        if (RunicCore.getInstance().getConfig().contains("class-icons." + character.getClassType().getName().toLowerCase() + ".damage")) {
            item = new ItemStack(
                    Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons." + character.getClassType().getName().toLowerCase() + ".material")),
                    1,
                    (short) RunicCore.getInstance().getConfig().getInt("class-icons." + character.getClassType().getName().toLowerCase() + ".damage"));
        } else {
            item = new ItemStack(
                    Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons." + character.getClassType().getName().toLowerCase() + ".material")), 1);
        }
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GREEN + character.getClassType().getName());
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        List<String> lore = new ArrayList<>(3);
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.GREEN + "" + character.getLevel());
        lore.add(ChatColor.GRAY + "Exp: " + ChatColor.GREEN + "" + character.getExp());
        lore.add(ChatColor.RED + "[Right click] to delete");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Handles the logic for creating a new character profile
     *
     * @param currentItem the itemStack in the inventory
     * @param player      the player who clicked
     */
    private void handleAddCharacter(ItemStack currentItem, Player player) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            if (currentItem.getType() != CharacterSelectUtil.GO_BACK_ITEM.getType()) {
                String className = getClassNameFromIcon(currentItem);
                PlayerData playerData = RunicCore.getDataAPI().getPlayerData(player.getUniqueId());
                int slot = playerData.findFirstUnusedSlot();
                PlayerMongoData playerMongoData = new PlayerMongoData(player.getUniqueId().toString());
                DatabaseHelper.addNewCharacter(playerMongoData, className, slot, () -> {
                    new CharacterData(player.getUniqueId(), slot, playerMongoData, jedis); // add to jedis
                    playerData.addCharacter(new ClassData(player.getUniqueId(), CharacterClass.getFromName(className), 0, 0));
                    openSelectGui(player);
                });
            }
        }
    }

    /**
     * Handles the logic for removing a new character profile
     *
     * @param currentItem the itemStack in the inventory
     * @param player      the player who clicked
     * @param jedis       the jedis resource
     */
    private void handleRemoveCharacter(ItemStack currentItem, Player player, Jedis jedis) {
        if (currentItem.getType() == CharacterSelectUtil.CONFIRM_DELETION_ITEM.getType()) {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                String parentKey = player.getUniqueId() + ":character:" + deletingCharacters.get(player.getUniqueId());
                RunicCore.getRedisAPI().removeAllFromRedis(jedis, parentKey); // removes all sub-keys
                PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
                mongoData.remove("character." + deletingCharacters.get(player.getUniqueId()));
                mongoData.save();
                RunicCore.getDataAPI().getPlayerData(player.getUniqueId()).removeCharacter(deletingCharacters.get(player.getUniqueId()));
                deletingCharacters.remove(player.getUniqueId());
                openSelectGui(player);
            });
        } else {
            classMenu.put(player.getUniqueId(), CharacterGui.SELECT);
            deletingCharacters.remove(player.getUniqueId());
            openSelectGui(player);
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
                openRemoveCharacterInventory(player, eventSlot < 9 ? eventSlot - 1 : eventSlot - 5);
            } else {
                classMenu.remove(player.getUniqueId());
                player.closeInventory();
                int slot = eventSlot < 9 ? eventSlot - 1 : eventSlot - 5;
                markCharacterForSave(player, slot);
                // Async request a data lookup in redis / mongo and build data object then load and run event sync
                loadCharacterData(player, slot);
            }
        } else if (currentItem.getType() == CharacterSelectUtil.CHARACTER_CREATE_ITEM.getType()) {
            openAddCharacterInventory(player);
        }
    }

    /**
     * Creates a CharacterData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param player player who is attempting to load their data
     * @param slot   the slot of the character
     */
    private void loadCharacterData(Player player, Integer slot) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                UUID uuid = player.getUniqueId();
                // Step 1: check if character data is cached in redis
                CharacterData characterData = RunicCore.getDataAPI().checkRedisForCharacterData(uuid, slot, jedis);
                // Step 2: check mongo documents
                if (characterData == null) {
                    characterData = new CharacterData(uuid, slot, new PlayerMongoData(uuid.toString()), jedis);
                }
                RunicCore.getCharacterAPI().getLoadedCharacters().getMap().put(uuid, Pair.pair(slot, characterData.getClassInfo().getClassType())); // now we always know which character is playing
                CharacterSelectEvent characterSelectEvent = new CharacterSelectEvent(player, characterData);
                Bukkit.getPluginManager().callEvent(characterSelectEvent);
            }
        });
    }

    /**
     * Redis saves all characters that logged into the current server on shutdown. This method keeps track of
     * which characters were logged in at any point to avoid it saving characters from other shards
     *
     * @param player        who logged in
     * @param characterSlot the character slot they selected
     */
    private void markCharacterForSave(Player player, int characterSlot) {
        ShutdownSaveWrapper shutdownSaveWrapper = RunicCore.getDataAPI().getPlayersToSave().get(player.getUniqueId());
        if (shutdownSaveWrapper == null) {
            Set<Integer> charactersToSave = new HashSet<Integer>() {{
                add(characterSlot);
            }};
            shutdownSaveWrapper = new ShutdownSaveWrapper(new PlayerMongoData(player.getUniqueId().toString()), charactersToSave);
            RunicCore.getDataAPI().getPlayersToSave().put(player.getUniqueId(), shutdownSaveWrapper);
        } else {
            shutdownSaveWrapper.getCharactersToSave().add(characterSlot);
        }
    }

    /**
     * Handles swapping between gui inventories in the character select screen
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (classMenu.containsKey(event.getPlayer().getUniqueId())) {
            switch (classMenu.get(event.getPlayer().getUniqueId())) {
                case SELECT:
                    openSelectGui((Player) event.getPlayer());
                    break;
                case ADD:
                    openAddCharacterInventory((Player) event.getPlayer());
                    break;
                case REMOVE:
                    openRemoveCharacterInventory((Player) event.getPlayer(), deletingCharacters.get(event.getPlayer().getUniqueId()));
                    break;
                default:
                    break;
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
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                handleRemoveCharacter(event.getCurrentItem(), (Player) event.getWhoClicked(), jedis);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        classMenu.remove(event.getPlayer().getUniqueId());
        deletingCharacters.remove(event.getPlayer().getUniqueId());
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
                PlayerData playerData = RunicCore.getDataAPI().getPlayerData(uuid);
                try {
                    openSelectGui(event.getPlayer());
                } catch (Exception exception) {
                    exception.printStackTrace();
                    RunicCore.getDataAPI().getPlayerDataMap().remove(playerData.getPlayerUuid());
                    classMenu.remove(uuid);
                }
            });
        }
    }

    private void openSelectGui(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Select Your Character");
        PlayerData playerData = RunicCore.getDataAPI().getPlayerData(player.getUniqueId());
        for (int i = 1; i <= 10; i++) {
            if (playerData.getPlayerCharacters().get(i) != null) {
                inventory.setItem(i <= 5 ? i + 1 : i + 5, getCharacterIcon(playerData.getPlayerCharacters().get(i)));
            } else {
                if (i == 6) {
                    inventory.setItem(
                            i <= 5 ? i + 1 : i + 5,
                            player.hasPermission("runic.rank.knight") || player.hasPermission("runic.rank.champion") ? CharacterSelectUtil.CHARACTER_CREATE_ITEM : CharacterSelectUtil.ONLY_KNIGHT_CREATE_ITEM);
                } else if (i >= 7 && i <= 10) {
                    inventory.setItem(
                            i <= 5 ? i + 1 : i + 5,
                            player.hasPermission("runic.rank.champion") ? CharacterSelectUtil.CHARACTER_CREATE_ITEM : CharacterSelectUtil.ONLY_CHAMPION_CREATE_ITEM);
                } else {
                    inventory.setItem(i <= 5 ? i + 1 : i + 5, CharacterSelectUtil.CHARACTER_CREATE_ITEM);
                }
            }
        }
        //set bottom row
        for (int i = 18; i < 27; i++) {
            inventory.setItem(i, GUIUtil.BORDER_ITEM);
        }
        //set exit button
        inventory.setItem(22, CharacterSelectUtil.EXIT_GAME_ITEM);
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.SELECT);
        });
    }
}
