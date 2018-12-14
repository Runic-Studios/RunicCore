package us.fortherealm.plugin.artifact;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
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

        // grab player's artifact
        ItemStack artifact = player.getInventory().getItem(0);
        ItemMeta meta = artifact.getItemMeta();

        // skin artifact
        contents.set(0, 4, ClickableItem.of
                (menuItem(artifact.getType(),
                        ChatColor.YELLOW,
                        meta.getDisplayName(),
                        "Click an appearance to change your skin!",
                        "Click here to return to the artifact",
                        ((Damageable) meta).getDamage()),
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
                        // todo: only do this if the skin is unlocked
                                updateSkin(player, material, name, durab);
                                SkinsGUI.ARTIFACT_SKINS.open(player);
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
    private void updateSkin(Player player, Material material, String name, int durab) {

        // grab the player's artifact
        ItemStack artifact = player.getInventory().getItem(0);

        // retrieve the artfiact's old NBT tags (attributes)
        NBTItem nbti = new NBTItem(artifact);
        NBTList oldAttributes = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);

        // update the display material, name, and durability of the artifact
        ItemMeta meta = artifact.getItemMeta();
        artifact.setType(material);
        meta.setDisplayName(ChatColor.YELLOW + name);
        ((Damageable) meta).setDamage(durab);
        artifact.setItemMeta(meta);

        // determine the player's class
        //String className = Main.getInstance().getConfig().get(player.getUniqueId() + ".info.class").toString();
        //LoreGenerator.generateArtifactLore(artifact, ChatColor.YELLOW, artifact.getItemMeta().getDisplayName(), className, durab);

        // create a new list of attributes
        NBTItem nbtiNew = new NBTItem(artifact);
        NBTList newAttributes = nbtiNew.getList("AttributeModifiers", NBTType.NBTTagCompound);

        // add all the attributes back
        for (int i = 0; i < oldAttributes.size(); i++) {
            NBTListCompound oldStat = oldAttributes.getCompound(i);
            // generic attributes are automatically saved, so this prevent duplicates
            if (!(oldStat.getString("Name").startsWith("generic"))) {
                NBTListCompound newStat = newAttributes.addCompound();
                newStat.setDouble("Amount", oldStat.getDouble("Amount"));
                newStat.setString("AttributeName", oldStat.getString("AttributeName"));
                newStat.setString("Name", oldStat.getString("Name"));
                newStat.setInteger("Operation", 0);
                newStat.setInteger("UUIDLeast", 59764);
                newStat.setInteger("UUIDMost", 31483);
                artifact = nbtiNew.getItem();
            }
        }

        // update the artifact!
        player.getInventory().setItem(0, artifact);
    }
}

