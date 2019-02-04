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

import java.util.ArrayList;

public class ArtifactGUI implements InventoryProvider {

    /**
     * CHANGE THE ID FOR EACH NEW GUI
     */
    public static final SmartInventory CUSTOMIZE_ARTIFACT = SmartInventory.builder()
            .id("artifactCustomization")
            .provider(new us.fortherealm.plugin.item.artifact.ArtifactGUI())
            .size(1, 9)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "Artifact Editor")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // grab player's artifact
        ItemStack artifact = player.getInventory().getItem(0);
        ItemMeta meta = artifact.getItemMeta();

        // skin artifact
        contents.set(0, 2, ClickableItem.of
                (menuItem(artifact.getType(),
                        ChatColor.YELLOW,
                        "Skin Editor",
                        "Click to customize your artifact skin",
                        ((Damageable) meta).getDamage()),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            SkinsGUI.ARTIFACT_SKINS.open(player);
                        }));

        // spell artifact
        contents.set(0, 4, ClickableItem.of
                (menuItem(Material.FIRE_CHARGE,
                        ChatColor.GREEN,
                        "Spell Editor",
                        "Click to customize your artifact abilities",
                        0),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            SpellsGUI.ARTIFACT_SPELLS.open(player);
                        }));

        // close inventory
        contents.set(0, 6, ClickableItem.of
                (menuItem(Material.BARRIER,
                        ChatColor.RED,
                        "Close",
                        "Close the editor",
                        0),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            player.closeInventory();
                            player.sendMessage(ChatColor.GRAY + "You closed the editor.");
                        }));
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor dispColor, String displayName, String desc, int durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(dispColor + displayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + desc);
        meta.setLore(lore);
        ((Damageable) meta).setDamage(durability);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}

