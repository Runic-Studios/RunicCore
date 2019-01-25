package us.fortherealm.plugin.professions.jeweler;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class BenchGUI implements InventoryProvider {

    public static final SmartInventory BENCH_GUI = SmartInventory.builder()
            .id("benchGUI")
            .provider(new BenchGUI())
            .size(1, 9)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "Gemcutting Bench")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // cut gems
        contents.set(0, 3, ClickableItem.of
                (menuItem(Material.EMERALD,
                        ChatColor.GREEN,
                        "Cut Gems",
                        "Refine raw gems into gemstones!"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            GemGUI.CUT_GEMS.open(player);
                        }));

        // close inventory
        contents.set(0, 5, ClickableItem.of
                (menuItem(Material.BARRIER,
                        ChatColor.RED,
                        "Close",
                        "Leave the workstation"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            player.closeInventory();
                            player.sendMessage(ChatColor.GRAY + "You left the workstation.");
                        }));
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        String[] desc = description.split("\n");
        ArrayList<String> lore = new ArrayList<>();
        for (String line : desc) {
            lore.add(ChatColor.GRAY + line);
        }
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}
