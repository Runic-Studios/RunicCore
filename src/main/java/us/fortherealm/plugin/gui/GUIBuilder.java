package us.fortherealm.plugin.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GUIBuilder implements InventoryProvider {

    public static final SmartInventory CLASS_SELECTION = SmartInventory.builder()
            .id("classSelection")
            .provider(new GUIBuilder())
            .size(1, 9)
            .title(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Choose Your Class!")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // select archer
        contents.set(0, 0, ClickableItem.of
                (menuItem(Material.BOW,
                        ChatColor.GREEN,
                        "Archer",
                        "test",
                        "Barrage",
                        "Parry",
                        "Frostbolt",
                        "Speed"),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

        // select cleric
        contents.set(0, 2, ClickableItem.of
                (menuItem(Material.IRON_SHOVEL,
                        ChatColor.AQUA,
                        "Cleric",
                        "test",
                        "Windstride",
                        "Rejuvenate",
                        "Frostbolt",
                        "Heal"),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

        // select mage
        contents.set(0, 4, ClickableItem.of
                (menuItem(Material.IRON_HOE,
                        ChatColor.LIGHT_PURPLE,
                        "Mage",
                        "test",
                        "Ice Nova",
                        "Discharge",
                        "Fireball",
                        ChatColor.LIGHT_PURPLE + "Blink"),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

        // select rogue
        contents.set(0, 6, ClickableItem.of
                (menuItem(Material.IRON_SWORD,
                        ChatColor.YELLOW,
                        "Rogue",
                        "test",
                        "Backstab",
                        "Cloak",
                        "Frostbolt",
                        "Speed"),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));

        // select warrior
        contents.set(0, 8, ClickableItem.of
                (menuItem(Material.IRON_AXE,
                        ChatColor.RED,
                        "Warrior",
                        "test5",
                        "Deliverance",
                        "Enrage",
                        "Fireball",
                        "Frostbolt"),
                e -> player.sendMessage(ChatColor.GOLD + "You clicked on a potato.")));
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description,
                               String artLeft, String artRight, String runeLeft, String runeRight) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "-> Select this class");
        lore.add(ChatColor.GRAY + ""); // blank line
        lore.add(ChatColor.GRAY + "Info:");
        lore.add(description);
        lore.add(ChatColor.GRAY + ""); // blank line
        lore.add(ChatColor.GRAY + "Default Spells:");
        lore.add(ChatColor.GRAY + " - " + color + artLeft);
        lore.add(ChatColor.GRAY + " - " + color + artRight);
        lore.add(ChatColor.GRAY + " - " + color + runeLeft);
        lore.add(ChatColor.GRAY + " - " + color + runeRight);
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}
