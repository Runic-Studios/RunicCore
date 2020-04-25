package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.CharacterManager;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CharacterGuiManager implements Listener {

    private static Map<UUID, CharacterGuiInfo> characterCache = new HashMap<UUID, CharacterGuiInfo>();
    private static Map<UUID, CharacterGui> classMenu = new HashMap<UUID, CharacterGui>();
    private static Map<UUID, Integer> deletingCharacters = new HashMap<UUID, Integer>();

    private static ItemStack creationIcon;
    private static ItemStack onlyKnightCreateIcon;
    private static ItemStack onlyChampionCreateIcon;
    private static ItemStack goBackIcon;
    private static ItemStack confirmDeletionIcon;
    private static Map<ClassEnum, ItemStack> classIcons = new HashMap<ClassEnum, ItemStack>();

    public static void initIcons() {

        creationIcon = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta creationMeta = creationIcon.getItemMeta();
        creationMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Create A Class");
        creationIcon.setItemMeta(creationMeta);

        onlyKnightCreateIcon = new ItemStack(Material.BARRIER, 1);
        ItemMeta knightMeta = onlyKnightCreateIcon.getItemMeta();
        knightMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You need " + ChatColor.AQUA + "Knight" + ChatColor.RED + " rank to use this slot");
        onlyKnightCreateIcon.setItemMeta(knightMeta);

        onlyChampionCreateIcon = new ItemStack(Material.BARRIER, 1);
        ItemMeta championMeta = onlyChampionCreateIcon.getItemMeta();
        championMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You need " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Champion" + ChatColor.RED + "" + ChatColor.BOLD + " rank to use this slot");
        onlyChampionCreateIcon.setItemMeta(championMeta);

        goBackIcon = new ItemStack(Material.BARRIER);
        ItemMeta goBackMeta = goBackIcon.getItemMeta();
        goBackMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Cancel");
        goBackIcon.setItemMeta(goBackMeta);

        confirmDeletionIcon = new ItemStack(Material.SLIME_BALL);
        ItemMeta confirmDeletionMeta = confirmDeletionIcon.getItemMeta();
        confirmDeletionMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Confirm Deletion");
        confirmDeletionMeta.setLore(Arrays.asList(new String[] {ChatColor.DARK_RED + "WARNING: There is no going back!"}));
        confirmDeletionIcon.setItemMeta(confirmDeletionMeta);

        ItemStack archerItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.archer.material")));
        ItemMeta archerMeta = archerItem.getItemMeta();
        archerMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Archer");
        archerMeta.setLore(Arrays.asList(new String[] {
                ChatColor.GRAY + "Ranged shooter, high",
                ChatColor.GRAY + "damage, single target"
        }));
        archerItem.setItemMeta(archerMeta);
        classIcons.put(ClassEnum.ARCHER, archerItem);

        ItemStack clericItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.cleric.material")));
        ItemMeta clericMeta = clericItem.getItemMeta();
        clericMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clericMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Cleric");
        clericMeta.setLore(Arrays.asList(new String[] {
                ChatColor.GRAY + "Group Healer, low",
                ChatColor.GRAY + "damage, single target"
        }));
        clericItem.setItemMeta(clericMeta);
        classIcons.put(ClassEnum.CLERIC, clericItem);

        ItemStack warriorItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.warrior.material")));
        ItemMeta warriorMeta = warriorItem.getItemMeta();
        warriorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        warriorMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Warrior");
        warriorMeta.setLore(Arrays.asList(new String[] {
                ChatColor.GRAY + "Tank, high defense, low",
                ChatColor.GRAY + "damage, single target"
        }));
        warriorItem.setItemMeta(warriorMeta);
        classIcons.put(ClassEnum.WARRIOR, warriorItem);

        ItemStack mageItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.mage.material")));
        ItemMeta mageMeta = mageItem.getItemMeta();
        mageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        mageMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Mage");
        mageMeta.setLore(Arrays.asList(new String[] {
                ChatColor.GRAY + "AoE & single target, medium",
                ChatColor.GRAY + "range & medium damage"
        }));
        mageItem.setItemMeta(mageMeta);
        classIcons.put(ClassEnum.MAGE, mageItem);

        ItemStack rogueItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.rogue.material")));
        ItemMeta rogueMeta = rogueItem.getItemMeta();
        rogueMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rogueMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Rogue");
        rogueMeta.setLore(Arrays.asList(new String[] {
                ChatColor.GRAY + "Close range, low defense,",
                ChatColor.GRAY + "very high damage"
        }));
        rogueItem.setItemMeta(rogueMeta);
        classIcons.put(ClassEnum.ROGUE, rogueItem);
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

    public static void openSelectGui(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, "Select Your Character");
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
                            !player.hasPermission("runic.rank.champion") ? creationIcon : onlyChampionCreateIcon);
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
        Inventory inventory = Bukkit.createInventory(null, 9, "Choose Your Class");
        inventory.setItem(0, goBackIcon);
        inventory.setItem(2, classIcons.get(ClassEnum.ARCHER));
        inventory.setItem(3, classIcons.get(ClassEnum.CLERIC));
        inventory.setItem(4, classIcons.get(ClassEnum.WARRIOR));
        inventory.setItem(5, classIcons.get(ClassEnum.MAGE));
        inventory.setItem(6, classIcons.get(ClassEnum.ROGUE));
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            classMenu.remove(player.getUniqueId());
            player.closeInventory();
            player.openInventory(inventory);
            classMenu.put(player.getUniqueId(), CharacterGui.ADD);
        });
    }

    public static void openRemoveGui(Player player, Integer classSlot) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Confirm Character Deletion");
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
                                    RunicCore.getCacheManager().getPlayerCaches().add(characterLoadEvent.getPlayerCache());
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
                                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        PlayerMongoData mongoData = new PlayerMongoData(event.getWhoClicked().getUniqueId().toString());
                                        mongoData.remove("character." + deletingCharacters.get(event.getWhoClicked().getUniqueId()));
                                        mongoData.save();
                                        characterCache.get(event.getWhoClicked().getUniqueId()).removeCharacter(deletingCharacters.get(event.getWhoClicked().getUniqueId()));
                                        deletingCharacters.remove(event.getWhoClicked());
                                        openSelectGui((Player) event.getWhoClicked());
                                    }
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
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), new Runnable() {
            @Override
            public void run() {
                UUID playerUuid = event.getPlayer().getUniqueId();
                try {
                    characterCache.put(playerUuid, new CharacterGuiInfo(new PlayerMongoData(playerUuid.toString())));
                    openSelectGui(event.getPlayer());
                } catch (Exception exception) {
                    exception.printStackTrace();
                    characterCache.remove(playerUuid);
                    classMenu.remove(playerUuid);
                }
            }
        });
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
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        List<String> lore = new ArrayList<String>(3);
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.DARK_GREEN + "" + character.getLevel());
        lore.add(ChatColor.GRAY + "Exp: " + ChatColor.DARK_GREEN + "" + character.getExp());
        lore.add(ChatColor.RED + "Right click to delete");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static Map<UUID, CharacterGuiInfo> getCharacterCache() {
        return characterCache;
    }

}
