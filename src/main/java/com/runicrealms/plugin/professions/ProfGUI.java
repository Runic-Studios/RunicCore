package com.runicrealms.plugin.professions;

import com.runicrealms.plugin.utilities.ColorUtil;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;

public class ProfGUI implements InventoryProvider {

    // globals
    public static final SmartInventory PROF_SELECTION = SmartInventory.builder()
            .id("profSelection")
            .provider(new ProfGUI())
            .size(4, 9)
            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Choose your profession!")
            .closeable(false)
            .build();

    private ScoreboardHandler sbh = RunicCore.getScoreboardHandler();

    @Override
    public void init(Player player, InventoryContents contents) {

        // select alchemist
        contents.set(1, 2, ClickableItem.of
                (menuItem(Material.POTION,
                        ChatColor.GREEN,
                        "Alchemist",
                        "&6Brew useful potions for your journey!"),
                        e -> {
                            setConfig(player, "Alchemist");
                            sbh.updatePlayerInfo(player);
                            sbh.updateSideInfo(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
                            player.sendTitle(
                                    ChatColor.GREEN + "You've Chosen",
                                    ChatColor.WHITE + "Alchemist!", 10, 100, 10);
                            contents.inventory().close(player);
                        }));

        // select blacksmith
        contents.set(1, 4, ClickableItem.of
                (menuItem(Material.IRON_INGOT,
                        ChatColor.GREEN,
                        "Blacksmith",
                        "&6Forge mail, gilded or plate armor!\n&7(Worn by Archers, Clerics, and Warriors)"),
                        e -> {
                            setConfig(player, "Blacksmith");
                            sbh.updatePlayerInfo(player);
                            sbh.updateSideInfo(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
                            player.sendTitle(
                                    ChatColor.GREEN + "You've Chosen",
                                    ChatColor.WHITE + "Blacksmith!", 10, 100, 10);
                            contents.inventory().close(player);
                        }));

        // select jeweler
        contents.set(1, 6, ClickableItem.of
                (menuItem(Material.REDSTONE,
                        ChatColor.GREEN,
                        "Jeweler",
                        "&6Cut gemstones and enhance equipment!"),
                        e -> {
                            setConfig(player, "Jeweler");
                            sbh.updatePlayerInfo(player);
                            sbh.updateSideInfo(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
                            player.sendTitle(
                                    ChatColor.GREEN + "You've Chosen",
                                    ChatColor.WHITE + "Jeweler!", 10, 100, 10);
                            contents.inventory().close(player);
                        }));

        // select leatherworker
        contents.set(2, 3, ClickableItem.of
                (menuItem(Material.RABBIT_HIDE,
                        ChatColor.GREEN,
                        "Leatherworker",
                        "&6Tan hides and create leather goods!\n&7(Worn by Rogues)"),
                        e -> {
                            setConfig(player, "Leatherworker");
                            sbh.updatePlayerInfo(player);
                            sbh.updateSideInfo(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
                            player.sendTitle(
                                    ChatColor.GREEN + "You've Chosen",
                                    ChatColor.WHITE + "Leatherworker!", 10, 100, 10);
                            contents.inventory().close(player);
                        }));

        // select tailor
        contents.set(2, 5, ClickableItem.of
                (menuItem(Material.PAPER,
                        ChatColor.GREEN,
                        "Tailor",
                        "&6Weave cloth and linen goods!\n&7(Worn by Mages)"),
                        e -> {
                            setConfig(player, "Tailor");
                            sbh.updatePlayerInfo(player);
                            sbh.updateSideInfo(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
                            player.sendTitle(
                                    ChatColor.GREEN + "You've Chosen",
                                    ChatColor.WHITE + "Tailor!", 10, 100, 10);
                            contents.inventory().close(player);
                        }));
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description) {

        ItemStack item = new ItemStack(material);

        if (material == Material.POTION) {
            PotionMeta pMeta = (PotionMeta) item.getItemMeta();
            if (pMeta != null) {
                pMeta.setDisplayName(color + displayName);
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "-> Select this class");
                lore.add(ChatColor.GRAY + "");
                lore.add(ChatColor.GRAY + "Info:");
                lore.add(ChatColor.GOLD + description);
                pMeta.setLore(lore);
                pMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                pMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                pMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                item.setItemMeta(pMeta);
            }
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "-> Select this class");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "Info:");
        for (String s : description.split("\n")) {
            lore.add(ColorUtil.format(s));
        }
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static void setConfig(Player player, String profName) {
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.name", profName);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.level", 0);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.exp", 0);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
    }
}
