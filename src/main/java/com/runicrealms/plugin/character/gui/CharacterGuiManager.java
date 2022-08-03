package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.CharacterSelectUtil;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.ClassData;
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

import java.util.*;

/**
 * Manages the character select menu which the player sees upon login
 *
 * @author Excel, Skyfallin
 */
public class CharacterGuiManager implements Listener {

    private static final Map<UUID, CharacterGuiInfo> characterCache = new HashMap<>();
    private static final Map<UUID, CharacterGui> classMenu = new HashMap<>();
    private static final Map<UUID, Integer> deletingCharacters = new HashMap<>();

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
            handleRemoveCharacter(event.getCurrentItem(), (Player) event.getWhoClicked());
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
        if (currentItem.getType() == Material.REDSTONE_BLOCK) {
            player.kickPlayer(ChatColor.GREEN + "Come back soon!");
            return;
        }
        if (checkIsCharacterIcon(currentItem)) {
            if (isRightClick) {
                openRemoveCharacterInventory(player, eventSlot < 9 ? eventSlot - 1 : eventSlot - 5);
            } else {
                classMenu.remove(player.getUniqueId());
                player.closeInventory();
                Integer slot = eventSlot < 9 ? eventSlot - 1 : eventSlot - 5;
                RunicCore.getDatabaseManager().getLoadedCharactersMap().put(player.getUniqueId(), slot); // now we always know which character is playing
                CharacterData characterData = RunicCore.getDatabaseManager().loadCharacterData(player.getUniqueId(), slot);
                if (characterData == null) {
                    Bukkit.getLogger().info("Something went wrong with character selection");
                    return;
                }
                CharacterSelectEvent characterSelectEvent = new CharacterSelectEvent(player, characterData);
                Bukkit.getPluginManager().callEvent(characterSelectEvent);
            }
        } else if (currentItem.getType() == CharacterSelectUtil.CHARACTER_CREATE_ITEM.getType()) {
            openAddCharacterInventory(player);
        }
    }

    /**
     * Handles the logic for creating a new character profile
     *
     * @param currentItem the itemStack in the inventory
     * @param player      the player who clicked
     */
    private void handleAddCharacter(ItemStack currentItem, Player player) {
        if (currentItem.getType() != CharacterSelectUtil.GO_BACK_ITEM.getType()) {
            String className = getClassNameFromIcon(currentItem);
            RunicCore.getDatabaseManager().addNewCharacter(player, className, characterCache.get(player.getUniqueId()).getFirstUnusedSlot());
            characterCache.get(player.getUniqueId()).addCharacter(new ClassData(player.getUniqueId(), ClassEnum.getFromName(className), 0, 0));
        }
        openSelectGui(player);
    }

    /**
     * Handles the logic for removing a new character profile
     *
     * @param currentItem the itemStack in the inventory
     * @param player      the player who clicked
     */
    private void handleRemoveCharacter(ItemStack currentItem, Player player) {
        if (currentItem.getType() == CharacterSelectUtil.CONFIRM_DELETION_ITEM.getType()) {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
                mongoData.remove("character." + deletingCharacters.get(player.getUniqueId()));
                mongoData.save();
                characterCache.get(player.getUniqueId()).removeCharacter(deletingCharacters.get(player.getUniqueId()));
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
     * Once the resource pack is loaded (or declined), we build the character select screen async
     */
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                UUID playerUuid = event.getPlayer().getUniqueId();
                try {
                    characterCache.put(playerUuid, new CharacterGuiInfo(RunicCore.getDatabaseManager().getPlayerDataMap().get(playerUuid)));
                    openSelectGui(event.getPlayer());
                } catch (Exception exception) {
                    exception.printStackTrace();
                    characterCache.remove(playerUuid);
                    classMenu.remove(playerUuid);
                }
            });
        }
    }

    /**
     *
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        characterCache.remove(event.getPlayer().getUniqueId());
        classMenu.remove(event.getPlayer().getUniqueId());
        deletingCharacters.remove(event.getPlayer().getUniqueId());
    }

    private static boolean checkIsCharacterIcon(ItemStack item) {
        for (Map.Entry<ClassEnum, ItemStack> classIcon : CharacterSelectUtil.getClassIcons().entrySet()) {
            if (item.getType() == classIcon.getValue().getType()) {
                return true;
            }
        }
        return false;
    }

    private static String getClassNameFromIcon(ItemStack icon) {
        for (Map.Entry<ClassEnum, ItemStack> classIcon : CharacterSelectUtil.getClassIcons().entrySet()) {
            if (icon.getType() == classIcon.getValue().getType()) {
                return classIcon.getKey().getName();
            }
        }
        return null;
    }

    private void openSelectGui(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Select Your Character");
        for (int i = 1; i <= 10; i++) {
            if (characterCache.get(player.getUniqueId()).getCharacterInfo().get(i) != null) {
                inventory.setItem(i <= 5 ? i + 1 : i + 5, getCharacterIcon(characterCache.get(player.getUniqueId()).getCharacterInfo().get(i)));
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
            inventory.setItem(i, GUIUtil.borderItem());
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

    /**
     * Builds and opens the inventory to create a new character (alt)
     *
     * @param player to open the inventory
     */
    public static void openAddCharacterInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Choose Your Class!");
        inventory.setItem(0, CharacterSelectUtil.GO_BACK_ITEM);
        inventory.setItem(2, CharacterSelectUtil.getClassIcons().get(ClassEnum.ARCHER));
        inventory.setItem(3, CharacterSelectUtil.getClassIcons().get(ClassEnum.CLERIC));
        inventory.setItem(4, CharacterSelectUtil.getClassIcons().get(ClassEnum.MAGE));
        inventory.setItem(5, CharacterSelectUtil.getClassIcons().get(ClassEnum.ROGUE));
        inventory.setItem(6, CharacterSelectUtil.getClassIcons().get(ClassEnum.WARRIOR));
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

    public static Map<UUID, CharacterGuiInfo> getCharacterCache() {
        return characterCache;
    }
}
