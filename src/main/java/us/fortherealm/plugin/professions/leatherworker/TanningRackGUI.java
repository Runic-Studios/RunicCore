package us.fortherealm.plugin.professions.leatherworker;

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

public class TanningRackGUI implements InventoryProvider {

    public static final SmartInventory TANNING_RACK_GUI = SmartInventory.builder()
            .id("tanningRackGUI")
            .provider(new TanningRackGUI())
            .size(1, 9)
            .title(ChatColor.YELLOW + "" + ChatColor.BOLD + "Tanning Rack")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // craft armor
        contents.set(0, 3, ClickableItem.of
                (menuItem(Material.DRIED_KELP, ChatColor.GOLD, "Craft Leather", "Craft useful items from leather!"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            LeatherGUI.CRAFT_LEATHER.open(player);
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
