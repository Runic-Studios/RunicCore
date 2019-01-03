package us.fortherealm.plugin.professions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;

import java.util.ArrayList;

public class ProfGUI implements InventoryProvider {

    // globals
    public static final SmartInventory PROF_SELECTION = SmartInventory.builder()
            .id("profSelection")
            .provider(new ProfGUI())
            .size(1, 9)
            .title(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Choose Your Profession!")
            .build();

    private ScoreboardHandler sbh = Main.getScoreboardHandler();

    @Override
    public void init(Player player, InventoryContents contents) {

        // select blacksmith
        contents.set(0, 0, ClickableItem.of
                (menuItem(Material.ANVIL,
                        ChatColor.GREEN,
                        "Blacksmith",
                        "An agile, long-range artillary.",
                        "Barrage"),
                        e -> {
                            setConfig(player, "Blacksmith");
                            player.closeInventory();
                        }));

        // select miner
        contents.set(0, 1, ClickableItem.of
                (menuItem(Material.IRON_PICKAXE,
                        ChatColor.GREEN,
                        "Miner",
                        "An agile, long-range artillary.",
                        "Barrage"),
                        e -> {
                            setConfig(player, "Miner");
                            player.closeInventory();
                        }));
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description, String spell) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "-> Select this class");
        lore.add(ChatColor.GRAY + ""); // blank line
        lore.add(ChatColor.GRAY + "Info:");
        lore.add(ChatColor.GOLD + description);
        lore.add(ChatColor.GRAY + ""); // blank line
        lore.add(ChatColor.GRAY + "Starter Spell:");
        lore.add(ChatColor.GRAY + " - " + color + spell);
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    private void setConfig(Player player, String profName) {
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.name", profName);
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.level", 0);
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.exp", 0);
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
        sbh.updatePlayerInfo(player);
        sbh.updateSideInfo(player);
    }
}
