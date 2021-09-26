package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.CharacterManager;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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

public class CharacterGuiManager implements Listener {

    private static final FileConfiguration FILE_CONFIGURATION = RunicCore.getInstance().getConfig();
    private static final short ARCHER_ITEM_DURAB = (short) FILE_CONFIGURATION.getInt("class-icons.archer.damage");
    private static final short CLERIC_ITEM_DURAB = (short) FILE_CONFIGURATION.getInt("class-icons.cleric.damage");
    private static final short MAGE_ITEM_DURAB = (short) FILE_CONFIGURATION.getInt("class-icons.mage.damage");
    private static final short ROGUE_ITEM_DURAB = (short) FILE_CONFIGURATION.getInt("class-icons.rogue.damage");
    private static final short WARRIOR_ITEM_DURAB = (short) FILE_CONFIGURATION.getInt("class-icons.warrior.damage");

    private static final Map<UUID, CharacterGuiInfo> characterCache = new HashMap<>();
    private static final Map<UUID, CharacterGui> classMenu = new HashMap<>();
    private static final Map<UUID, Integer> deletingCharacters = new HashMap<>();

    private static ItemStack creationIcon;
    private static ItemStack onlyKnightCreateIcon;
    private static ItemStack onlyChampionCreateIcon;
    private static ItemStack goBackIcon;
    private static ItemStack confirmDeletionIcon;
    private static final Map<ClassEnum, ItemStack> classIcons = new HashMap<>();

    public static void initIcons() {

        creationIcon = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta creationMeta = creationIcon.getItemMeta();
        creationMeta.setUnbreakable(true);
        creationMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        creationMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Create a Class");
        creationIcon.setItemMeta(creationMeta);

        onlyKnightCreateIcon = new ItemStack(Material.BARRIER, 1);
        ItemMeta knightMeta = onlyKnightCreateIcon.getItemMeta();
        knightMeta.setUnbreakable(true);
        knightMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        knightMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You need " + ChatColor.AQUA + "Knight" + ChatColor.RED + " rank to use this slot");
        onlyKnightCreateIcon.setItemMeta(knightMeta);

        onlyChampionCreateIcon = new ItemStack(Material.BARRIER, 1);
        ItemMeta championMeta = onlyChampionCreateIcon.getItemMeta();
        championMeta.setUnbreakable(true);
        championMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        championMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You need " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Champion" + ChatColor.RED + "" + ChatColor.BOLD + " rank to use this slot");
        onlyChampionCreateIcon.setItemMeta(championMeta);

        goBackIcon = new ItemStack(Material.BARRIER);
        ItemMeta goBackMeta = goBackIcon.getItemMeta();
        goBackMeta.setUnbreakable(true);
        goBackMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        goBackMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Cancel");
        goBackIcon.setItemMeta(goBackMeta);

        confirmDeletionIcon = new ItemStack(Material.SLIME_BALL);
        ItemMeta confirmDeletionMeta = confirmDeletionIcon.getItemMeta();
        confirmDeletionMeta.setUnbreakable(true);
        confirmDeletionMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        confirmDeletionMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Confirm Deletion");
        confirmDeletionMeta.setLore(Collections.singletonList(ChatColor.DARK_RED + "WARNING: There is no going back!"));
        confirmDeletionIcon.setItemMeta(confirmDeletionMeta);

        ItemStack archerItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.archer.material")), 1, ARCHER_ITEM_DURAB);
        ItemMeta archerMeta = archerItem.getItemMeta();
        archerMeta.setUnbreakable(true);
        archerMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        archerMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Archer ⚔");
        archerMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Long-range",
                ChatColor.GOLD + "● Bowman",
                ChatColor.GOLD + "● Single Target",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The archer features a diverse",
                ChatColor.GRAY + "pool of damage, mobility, and",
                ChatColor.GRAY + "utility spells, a master of",
                ChatColor.GRAY + "terrain and single combat!"
        ));
        archerItem.setItemMeta(archerMeta);
        classIcons.put(ClassEnum.ARCHER, archerItem);

        ItemStack clericItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.cleric.material")),1, CLERIC_ITEM_DURAB);
        ItemMeta clericMeta = clericItem.getItemMeta();
        clericMeta.setUnbreakable(true);
        clericMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        clericMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clericMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Cleric ✦");
        clericMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Close-range",
                ChatColor.GOLD + "● Healer",
                ChatColor.GOLD + "● Area-of-effect",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The cleric features a range",
                ChatColor.GRAY + "crowd control, healing, and",
                ChatColor.GRAY + "utility spells, bolstering",
                ChatColor.GRAY + "any party!"
        ));
        clericItem.setItemMeta(clericMeta);
        classIcons.put(ClassEnum.CLERIC, clericItem);

        ItemStack mageItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.mage.material")), 1, MAGE_ITEM_DURAB);
        ItemMeta mageMeta = mageItem.getItemMeta();
        mageMeta.setUnbreakable(true);
        mageMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        mageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        mageMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Mage ʔ");
        mageMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Medium-range",
                ChatColor.GOLD + "● Caster",
                ChatColor.GOLD + "● Area-of-effect",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The mage is a master of widespread",
                ChatColor.GRAY + "damage, controlling the flow of",
                ChatColor.GRAY + "battle and deadly if left unchecked",
                ChatColor.GRAY + "in the back lines!"
        ));
        mageItem.setItemMeta(mageMeta);
        classIcons.put(ClassEnum.MAGE, mageItem);

        ItemStack rogueItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.rogue.material")), 1, ROGUE_ITEM_DURAB);
        ItemMeta rogueMeta = rogueItem.getItemMeta();
        rogueMeta.setUnbreakable(true);
        rogueMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        rogueMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rogueMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Rogue ⚔");
        rogueMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Close-range",
                ChatColor.GOLD + "● Duelist",
                ChatColor.GOLD + "● Single Target",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The rogue does not play fair,",
                ChatColor.GRAY + "Equipped with a pool of crowd",
                ChatColor.GRAY + "control, stealth, and damage",
                ChatColor.GRAY + "to engage any foe!"
                ));
        rogueItem.setItemMeta(rogueMeta);
        classIcons.put(ClassEnum.ROGUE, rogueItem);

        ItemStack warriorItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.warrior.material")), 1, WARRIOR_ITEM_DURAB);
        ItemMeta warriorMeta = warriorItem.getItemMeta();
        warriorMeta.setUnbreakable(true);
        warriorMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        warriorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        warriorMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Warrior ■");
        warriorMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Close-range",
                ChatColor.GOLD + "● Tank",
                ChatColor.GOLD + "● Single Target",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The warrior is a force to be",
                ChatColor.GRAY + "reckoned with, featuring both",
                ChatColor.GRAY + "offensive and defensive spells",
                ChatColor.GRAY + "to charge into the front lines!"
        ));
        warriorItem.setItemMeta(warriorMeta);
        classIcons.put(ClassEnum.WARRIOR, warriorItem);
    }

    private static boolean checkIsCharacterIcon(ItemStack item) {
        for (Map.Entry<ClassEnum, ItemStack> classIcon : classIcons.entrySet()) {
            if (item.getType() == classIcon.getValue().getType()) {
                return true;
            }
        }
        return false;
    }

    private static String getClassNameFromIcon(ItemStack icon) {
        for (Map.Entry<ClassEnum, ItemStack> classIcon : classIcons.entrySet()) {
            if (icon.getType() == classIcon.getValue().getType()) {
                return classIcon.getKey().getName();
            }
        }
        return null;
    }

    private static void openSelectGui(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, ChatColor.GREEN + "Select Your Character");
        for (int i = 1; i <= 10; i++) {
            if (characterCache.get(player.getUniqueId()).getCharacterInfo().get(i) != null) {
                inventory.setItem(i <= 5 ? i + 1 : i + 5, getCharacterIcon(characterCache.get(player.getUniqueId()).getCharacterInfo().get(i)));
            } else {
                if (i == 6) {
                    inventory.setItem(
                            i <= 5 ? i + 1 : i + 5,
                            player.hasPermission("runic.rank.knight") || player.hasPermission("runic.rank.champion") ? creationIcon : onlyKnightCreateIcon);
                } else if (i >= 7 && i <= 10) {
                    inventory.setItem(
                            i <= 5 ? i + 1 : i + 5,
                            player.hasPermission("runic.rank.champion") ? creationIcon : onlyChampionCreateIcon);
                } else {
                    inventory.setItem(i <= 5 ? i + 1 : i + 5, creationIcon);
                }
            }
        }
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.SELECT);
        });
    }

    public static void openAddGui(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Choose Your Class");
        inventory.setItem(0, goBackIcon);
        inventory.setItem(2, classIcons.get(ClassEnum.ARCHER));
        inventory.setItem(3, classIcons.get(ClassEnum.CLERIC));
        inventory.setItem(4, classIcons.get(ClassEnum.MAGE));
        inventory.setItem(5, classIcons.get(ClassEnum.ROGUE));
        inventory.setItem(6, classIcons.get(ClassEnum.WARRIOR));
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.ADD);
        });
    }

    public static void openRemoveGui(Player player, Integer classSlot) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.RED + "Confirm Character Deletion");
        inventory.setItem(2, goBackIcon);
        inventory.setItem(6, confirmDeletionIcon);
        deletingCharacters.put(player.getUniqueId(), classSlot);
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.REMOVE);
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (classMenu.containsKey(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null) {
                    if (event.getCurrentItem().getType() != Material.AIR) {
                        if (classMenu.get(event.getWhoClicked().getUniqueId()) == CharacterGui.SELECT) {
                            if (checkIsCharacterIcon(event.getCurrentItem())) {
                                if (event.isRightClick()) {
                                    openRemoveGui((Player) event.getWhoClicked(), event.getSlot() < 9 ? event.getSlot() - 1 : event.getSlot() - 5);
                                } else {
                                    classMenu.remove(event.getWhoClicked().getUniqueId());
                                    event.getWhoClicked().closeInventory();
                                    Integer slot = event.getSlot() < 9 ? event.getSlot() - 1 : event.getSlot() - 5;
                                    CharacterManager.getSelectedCharacters().put(event.getWhoClicked().getUniqueId(), slot);
                                    CharacterLoadEvent characterLoadEvent = new CharacterLoadEvent(
                                            RunicCore.getCacheManager().buildPlayerCache((Player) event.getWhoClicked(), slot),
                                            (Player) event.getWhoClicked());
                                    RunicCore.getCacheManager().getPlayerCaches().put(characterLoadEvent.getPlayer(), characterLoadEvent.getPlayerCache());
                                    Bukkit.getPluginManager().callEvent(characterLoadEvent);
                                }
                            } else if (event.getCurrentItem().getType() == creationIcon.getType()) {
                                openAddGui((Player) event.getWhoClicked());
                            }
                        } else if (classMenu.get(event.getWhoClicked().getUniqueId()) == CharacterGui.ADD) {
                            if (event.getCurrentItem().getType() == goBackIcon.getType()) {
                                openSelectGui((Player) event.getWhoClicked());
                            } else {
                                String className = getClassNameFromIcon(event.getCurrentItem());
                                RunicCore.getCacheManager().tryCreateNewCharacter((Player) event.getWhoClicked(), className, characterCache.get(event.getWhoClicked().getUniqueId()).getFirstUnusedSlot());
                                characterCache.get(event.getWhoClicked().getUniqueId()).addCharacter(new CharacterInfo(ClassEnum.getFromName(className), 0, 0));
                                openSelectGui((Player) event.getWhoClicked());
                            }
                        } else if (classMenu.get(event.getWhoClicked().getUniqueId()) == CharacterGui.REMOVE) {
                            if (event.getCurrentItem().getType() == confirmDeletionIcon.getType()) {
                                classMenu.remove(event.getWhoClicked().getUniqueId());
                                event.getWhoClicked().closeInventory();
                                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                                    PlayerMongoData mongoData = new PlayerMongoData(event.getWhoClicked().getUniqueId().toString());
                                    mongoData.remove("character." + deletingCharacters.get(event.getWhoClicked().getUniqueId()));
                                    mongoData.save();
                                    characterCache.get(event.getWhoClicked().getUniqueId()).removeCharacter(deletingCharacters.get(event.getWhoClicked().getUniqueId()));
                                    deletingCharacters.remove(event.getWhoClicked().getUniqueId());
                                    openSelectGui((Player) event.getWhoClicked());
                                });
                            } else {
                                classMenu.put(event.getWhoClicked().getUniqueId(), CharacterGui.SELECT);
                                deletingCharacters.remove(event.getWhoClicked().getUniqueId());
                                openSelectGui((Player) event.getWhoClicked());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (classMenu.containsKey(event.getPlayer().getUniqueId())) {
            switch (classMenu.get(event.getPlayer().getUniqueId())) {
                case SELECT:
                    openSelectGui((Player) event.getPlayer());
                    break;
                case ADD:
                    openAddGui((Player) event.getPlayer());
                    break;
                case REMOVE:
                    openRemoveGui((Player) event.getPlayer(), deletingCharacters.get(event.getPlayer().getUniqueId()));
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED ||
                event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                UUID playerUuid = event.getPlayer().getUniqueId();
                try {
                    characterCache.put(playerUuid, new CharacterGuiInfo(new PlayerMongoData(playerUuid.toString())));
                    openSelectGui(event.getPlayer());
                } catch (Exception exception) {
                    exception.printStackTrace();
                    characterCache.remove(playerUuid);
                    classMenu.remove(playerUuid);
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        characterCache.remove(event.getPlayer().getUniqueId());
        if (classMenu.containsKey(event.getPlayer().getUniqueId())) {
            classMenu.remove(event.getPlayer().getUniqueId());
        }
        if (deletingCharacters.containsKey(event.getPlayer().getUniqueId())) {
            deletingCharacters.remove(event.getPlayer().getUniqueId());
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getCharacterIcon(CharacterInfo character) {
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
