package us.fortherealm.plugin.item.artifact;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;

import java.util.ArrayList;

public class SkinsGUI implements InventoryProvider {

    /**
     * CHANGE THE ID FOR EACH NEW GUI
     */
    public static final SmartInventory ARTIFACT_SKINS = SmartInventory.builder()
            .id("artifactSkins")
            .provider(new SkinsGUI())
            .size(4, 9)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "Available Skins")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // determine the player's class
        String className = Main.getInstance().getConfig().get(player.getUniqueId() + ".info.class.name").toString();

        // grab player's artifact
        ItemStack artifact = player.getInventory().getItem(0);
        ItemMeta meta = artifact.getItemMeta();

        // skin artifact
        contents.set(1, 4, ClickableItem.of
                (menuItem(artifact.getType(),
                        ChatColor.YELLOW,
                        meta.getDisplayName(),
                        "Click an appearance to change your skin!",
                        "Click here to return to the editor",
                        ((Damageable) meta).getDamage()),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player);
                        }));

        // skin displays are class-specific
        switch (className) {
            case "Archer":
                displaySkinsArcher(player, contents);
                break;
            case "Cleric":
                displaySkinsCleric(player, contents);
                break;
            case "Mage":
                displaySkinsMage(player, contents);
                break;
            case "Rogue":
                displaySkinsRogue(player, contents);
                break;
            case "Warrior":
                displaySkinsWarrior(player, contents);
                break;
        }
    }

    private void displaySkinsArcher(Player player, InventoryContents contents) {
        displaySkin(player, contents, 2, 2, Material.BOW, "Stiff Oaken Shortbow", "", 0, "");
        displaySkin(player, contents, 2, 3, Material.BOW, "Worn Stone Shortbow", "Unlock by reaching lv. 10!", 5, "stone_bow");
        displaySkin(player, contents, 2, 4, Material.BOW, "Polished Silver Shortbow", "Unlock by reaching lv. 20!", 10, "WOODEN_bow");
        displaySkin(player, contents, 2, 5, Material.BOW, "Victorious Gilded Shortbow", "Unlock by reaching lv. 30!", 15, "golden_bow");
        displaySkin(player, contents, 2, 6, Material.BOW, "Ancient Crystal Shortbow", "Unlock by reaching lv. 40!", 20, "diamond_bow");
    }

    private void displaySkinsCleric(Player player, InventoryContents contents) {
        displaySkin(player, contents, 2, 2, Material.WOODEN_SHOVEL, "Initiate's Oaken Mace", "", 0, "");
        displaySkin(player, contents, 2, 3, Material.WOODEN_SHOVEL, "Worn Stone Club", "Unlock by reaching lv. 10!", 1, "stone_mace");
        displaySkin(player, contents, 2, 4, Material.WOODEN_SHOVEL, "Polished Silver Hammer", "Unlock by reaching lv. 20!", 2, "WOODEN_mace");
        displaySkin(player, contents, 2, 5, Material.WOODEN_SHOVEL, "Victorious Gilded Mace", "Unlock by reaching lv. 30!", 3, "golden_mace");
        displaySkin(player, contents, 2, 6, Material.WOODEN_SHOVEL, "Ancient Crystal Maul", "Unlock by reaching lv. 40!", 4, "diamond_mace");
    }

    private void displaySkinsMage(Player player, InventoryContents contents) {
        displaySkin(player, contents, 2, 2, Material.WOODEN_HOE, "Sturdy Oaken Branch", "", 0, "");
        displaySkin(player, contents, 2, 3, Material.WOODEN_HOE, "Worn Stone Cane", "Unlock by reaching lv. 10!", 1, "stone_staff");
        displaySkin(player, contents, 2, 4, Material.WOODEN_HOE, "Polished Silver Scepter", "Unlock by reaching lv. 20!", 2, "WOODEN_staff");
        displaySkin(player, contents, 2, 5, Material.WOODEN_HOE, "Victorious Gilded Staff", "Unlock by reaching lv. 30!", 3, "guilded_staff");
        displaySkin(player, contents, 2, 6, Material.WOODEN_HOE, "Ancient Crystal Greatstaff", "Unlock by reaching lv. 40!", 4, "diamond_staff");
        displaySkin(player, contents, 2, 7, Material.WOODEN_HOE, "Primal Arcane Rod", "§8Unlocked by becoming an alpha backer.", 5, "arcane_staff");
    }

    private void displaySkinsRogue(Player player, InventoryContents contents) {
        displaySkin(player, contents, 2, 2, Material.WOODEN_SWORD, "Oaken Sparring Sword", "", 0, "");
        displaySkin(player, contents, 2, 3, Material.WOODEN_SWORD, "Worn Stone Sword", "Unlock by reaching lv. 10!", 1, "stone_sword");
        displaySkin(player, contents, 2, 4, Material.WOODEN_SWORD, "Polished Silver Broadsword", "Unlock by reaching lv. 20!", 2, "WOODEN_sword");
        displaySkin(player, contents, 2, 5, Material.WOODEN_SWORD, "Victorious Gilded Longsword", "Unlock by reaching lv. 30!", 3, "golden_sword");
        displaySkin(player, contents, 2, 6, Material.WOODEN_SWORD, "Ancient Crystal Greatsword", "Unlock by reaching lv. 40!", 4, "diamond_sword");
    }

    private void displaySkinsWarrior(Player player, InventoryContents contents) {
        displaySkin(player, contents, 2, 2, Material.WOODEN_AXE, "Worn Oaken Battleaxe", "", 0, "");
        displaySkin(player, contents, 2, 3, Material.WOODEN_AXE, "Crumbling Stone Axe", "Unlock by reaching lv. 10!", 1, "stone_axe");
        displaySkin(player, contents, 2, 4, Material.WOODEN_AXE, "Polished Silver Broadaxe", "Unlock by reaching lv. 20!", 2, "WOODEN_axe");
        displaySkin(player, contents, 2, 5, Material.WOODEN_AXE, "Victorious Gilded Reaver", "Unlock by reaching lv. 30!", 3, "golden_axe");
        displaySkin(player, contents, 2, 6, Material.WOODEN_AXE, "Ancient Crystal Battleaxe", "Unlock by reaching lv. 40!", 4, "diamond_axe");
    }

    // display for each skin
    private void displaySkin(Player player, InventoryContents contents, int row, int slot,
                             Material material, String name, String desc, int durab, String perm) {

        if (perm.equals("") || player.hasPermission("ftr.skins." + perm)) {
            contents.set(row, slot, ClickableItem.of
                    (menuItem(material,
                            ChatColor.YELLOW,
                            name, ChatColor.GREEN + "Unlocked!", "", durab),
                            e -> {
                                updateSkin(player, material, name, durab);
                                SkinsGUI.ARTIFACT_SKINS.open(player);
                            }));
        } else {
            contents.set(row, slot, ClickableItem.of
                    (menuItem(material,
                            ChatColor.RED,
                            name, ChatColor.RED + desc, "", durab),
                            e -> {
                                player.closeInventory();
                            }));
        }
    }

    // transforms the player's artifact skin
    private void updateSkin(Player player, Material material, String name, int durab) {

        // grab the player's artifact
        ItemStack artifact = player.getInventory().getItem(0);

        // update the display material, name, and durability of the artifact
        ItemMeta meta = artifact.getItemMeta();
        artifact.setType(material);
        meta.setDisplayName(ChatColor.YELLOW + name);
        ((Damageable) meta).setDamage(durab);
        artifact.setItemMeta(meta);

        // update the artifact!
        player.getInventory().setItem(0, artifact);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        player.sendMessage(ChatColor.GREEN + "Enjoy your new skin!");
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor dispColor, String displayName, String desc, String desc2, int durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(dispColor + displayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + desc);
        if (!desc2.equals("")) lore.add(ChatColor.DARK_GRAY + desc2);
        meta.setLore(lore);
        ((Damageable) meta).setDamage(durability);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}

