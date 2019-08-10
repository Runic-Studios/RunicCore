package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Objects;

public class LegendaryManager implements Listener {

    private HashMap<String, ItemStack> legendaries = new HashMap<>();

    public LegendaryManager() {
        registerLegendaries();
    }

    private void registerLegendaries() {
        this.legendaries.put(ChatColor.GOLD + "The Eternal Flame", eternalFlame());
        this.legendaries.put(ChatColor.GOLD + "Frostforged Arrowhead", frostforgedArrowhead());
        this.legendaries.put(ChatColor.GOLD + "Ambrosian Powder", ambrosianPowder());
        this.legendaries.put(ChatColor.GOLD + "Frostforged Bulwark", frostforgedBulwark());
        this.legendaries.put(ChatColor.GOLD + "Frostforged Reaver", frostforgedReaver());
        this.legendaries.put(ChatColor.GOLD + "Tomb of the Frost Lords", tomeOfTheFrostLords());
    }

    /**
     * This method updates legendaries so when we buff them, they auto-update. Saves a lot of headache.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        ItemStack[] inv = player.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            if (player.getInventory().getItem(i) == null) continue;
            ItemStack current = player.getInventory().getItem(i);
            if (Objects.requireNonNull(current).hasItemMeta()
                    && legendaries.containsKey(Objects.requireNonNull(current.getItemMeta()).getDisplayName())) {
                player.getInventory().setItem(i, legendaries.get(current.getItemMeta().getDisplayName()));
            }
        }
    }

    public static ItemStack eternalFlame() {
        ItemStack tomb = new ItemStack(Material.FIRE_CHARGE);
        tomb = AttributeUtil.addGenericStat(tomb, "generic.maxHealth", 50, "offhand");
        tomb = AttributeUtil.addCustomStat(tomb, "custom.magicDamage", 6);
        tomb = AttributeUtil.addCustomStat(tomb, "soulbound", "true");
        LoreGenerator.generateItemLore(tomb, ChatColor.GOLD, "The Eternal Flame", "");
        return tomb;
    }

    public static ItemStack frostforgedArrowhead() {
        ItemStack arrowHead = new ItemStack(Material.FLINT);
        arrowHead = AttributeUtil.addCustomStat(arrowHead, "required.level", 50);
        arrowHead = AttributeUtil.addCustomStat(arrowHead, "custom.attackDamage", 8);
        arrowHead = AttributeUtil.addCustomStat(arrowHead, "custom.magicDamage", 8);
        LoreGenerator.generateItemLore(arrowHead, ChatColor.GOLD, "Frostforged Arrowhead", "");
        return arrowHead;
    }

    public static ItemStack ambrosianPowder() {
        ItemStack powder = new ItemStack(Material.RABBIT_FOOT);
        powder = AttributeUtil.addCustomStat(powder, "required.level", 50);
        powder = AttributeUtil.addCustomStat(powder, "custom.manaBoost", 100);
        powder = AttributeUtil.addCustomStat(powder, "custom.healingBoost", 50);
        LoreGenerator.generateItemLore(powder, ChatColor.GOLD, "Ambrosian Powder", "");
        return powder;
    }

    public static ItemStack frostforgedBulwark() {
        ItemStack shield = new ItemStack(Material.SHIELD);
        shield = AttributeUtil.addCustomStat(shield, "required.level", 50);
        shield = AttributeUtil.addGenericStat(shield, "generic.maxHealth", 125, "offhand");
        shield = AttributeUtil.addCustomStat(shield, "custom.shield", 5);
        LoreGenerator.generateItemLore(shield, ChatColor.GOLD, "Frostforged Bulwark", "");
        return shield;
    }

    public static ItemStack frostforgedReaver() {
        ItemStack reaver = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = reaver.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(2);
        reaver.setItemMeta(meta);
        reaver = AttributeUtil.addCustomStat(reaver, "required.level", 50);
        reaver = AttributeUtil.addCustomStat(reaver, "custom.attackDamage", 15);
        LoreGenerator.generateItemLore(reaver, ChatColor.GOLD, "Frostforged Reaver", "");
        return reaver;
    }

    public static ItemStack tomeOfTheFrostLords() {
        ItemStack tome = new ItemStack(Material.BOOK);
        tome = AttributeUtil.addCustomStat(tome, "required.level", 50);
        tome = AttributeUtil.addCustomStat(tome, "custom.manaBoost", 100);
        tome = AttributeUtil.addCustomStat(tome, "custom.magicDamage", 8);
        LoreGenerator.generateItemLore(tome, ChatColor.GOLD, "Tome of the Frost Lords", "");
        return tome;
    }
}
