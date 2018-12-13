package us.fortherealm.plugin.editor;

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
            .size(2, 9)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "Available Skins")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // determine the player's class
        String className = Main.getInstance().getConfig().get(player.getUniqueId() + ".info.class").toString();

        // skin editor
        contents.set(0, 4, ClickableItem.of
                (menuItem(player.getInventory().getItem(0).getType(),
                        ChatColor.YELLOW,
                        player.getInventory().getItem(0).getItemMeta().getDisplayName(),
                        "Click an appearance to change your skin!",
                        "Click here to return to the editor", 0),
                        e -> {
                            ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player);
                        }));

        // skin displays are class-specific
        switch (className) {
            case "Archer":
                break;
            case "Cleric":
                break;
            case "Mage":
                displaySkinsMage(player, contents);
                break;
            case "Rogue":
                break;
            case "Warrior":
                break;
        }
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

    // displays an entire class' skin screen, with booleans for unlocked skins
    private void displaySkinsMage(Player player, InventoryContents contents) {

        // todo: make permissions
        // if (!they have permission) {
        displaySkin(player, contents, 1, 0, Material.STONE_HOE, "Worn Stone Cane", "Unlock by reaching lv. 10!", 0, false);
        // else
        // displaySkin(player, contents, 1, 0, Material.STONE_HOE, "Worn Stone Cane", "Unlock by...", 0, true);


        displaySkin(player, contents, 1, 1, Material.IRON_HOE, "Polished Silver Scepter", "Unlock by reaching lv. 20!", 0, false);
        displaySkin(player, contents, 1, 2, Material.GOLDEN_HOE, "Victorious Gilded Staff", "Unlock by reaching lv. 30!", 0, false);
        displaySkin(player, contents, 1, 3, Material.DIAMOND_HOE, "Ancient Crystal Greatstaff", "Unlock by reaching lv. 40!", 0, false);
        displaySkin(player, contents, 1, 4, Material.IRON_HOE, "Primal Arcane Rod", "§8Unlocked by becoming an alpha backer.", 5, false);
    }

    // display for each skin
    private void displaySkin(Player player, InventoryContents contents, int row, int slot,
                             Material material, String name, String desc, int durab, boolean isUnlocked) {
        if (!isUnlocked) {
            contents.set(row, slot, ClickableItem.of
                    (menuItem(material,
                            ChatColor.RED,
                            name, desc, "", durab),
                            e -> {
                                // transform artifact
                                // refresh the screen, don't close the inventory
                                updateSkin(player);
                                SkinsGUI.ARTIFACT_SKINS.open(player);
                                //player.closeInventory();
                            }));
        } else {
            contents.set(row, slot, ClickableItem.of
                    (menuItem(material,
                            ChatColor.YELLOW,
                            name, ChatColor.GREEN + "Unlocked!", "", durab),
                            e -> {
                                player.closeInventory();
                            }));
        }
    }

    // transforms the player's artifact skin
    private void updateSkin(Player player) {

        // grab the item in slot 0
        ItemStack oldItem = player.getInventory().getItem(0);

        ItemStack newItem = new ItemStack(oldItem.getType());

    }
}

