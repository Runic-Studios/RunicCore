package com.runicrealms.plugin.player;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class StatsGUI implements InventoryHolder {

    private final int dexterity;
    private final int intelligence;
    private final int strength;
    private final int wisdom;
    private final int vitality;
    private static final int[] STAT_ITEM_SLOTS = new int[]{20, 22, 24, 30, 32};

    private final Inventory inventory;
    private final Player player;
    private final ItemStack[] statMenuItems;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public StatsGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&eCharacter Stats"));
        this.player = player;
        this.dexterity = RunicCoreAPI.getPlayerDexterity(player.getUniqueId());
        this.intelligence = RunicCoreAPI.getPlayerIntelligence(player.getUniqueId());
        this.strength = RunicCoreAPI.getPlayerStrength(player.getUniqueId());
        this.wisdom = RunicCoreAPI.getPlayerWisdom(player.getUniqueId());
        this.vitality = RunicCoreAPI.getPlayerVitality(player.getUniqueId());
        statMenuItems = new ItemStack[]{dexterityMenuItem(), intelligenceMenuItem(), strengthMenuItem(), wisdomMenuItem(), vitalityMenuItem()};
        openMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns a formatted string of the player's combat stats
     *
     * @param name  name of the stat (dexterity)
     * @param value value of the stat (RunicCoreAPI)
     * @return a formatted string for use in the player menu
     */
    private String formattedStat(String name, int value) {
        Stat stat = Stat.getFromName(name);
        if (stat == null) {
            Bukkit.getLogger().info("Base stat enum not found!");
            return "";
        }
        return stat.getChatColor() +
                stat.getIcon() +
                " (" + stat.getPrefix() +
                "): " + statPrefix(value) + value;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.closeButton());
        this.inventory.setItem(4, statInfoItem());
        for (int i = 0; i < statMenuItems.length; i++) {
            this.inventory.setItem(STAT_ITEM_SLOTS[i], statMenuItems[i]);
        }
    }

    private String statPrefix(int stat) {
        return stat > 0 ? "&a+" : "&7+";
    }

    private ItemStack statInfoItem() {
        AttributeInstance playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        assert playerMaxHealth != null;
        int healthBonus = (int) playerMaxHealth.getValue() -
                PlayerLevelUtil.calculateHealthAtLevel(player.getLevel(), RunicCoreAPI.getRedisValue(player, "classType"));
        return GUIUtil.dispItem(
                Material.PAPER,
                ChatColor.YELLOW + "Character Stats",
                new String[]{
                        "",
                        ChatColor.GRAY + "Your character stats improve",
                        ChatColor.GRAY + "your potency in battle!",
                        ChatColor.GRAY + "Earn them from your ",
                        ChatColor.LIGHT_PURPLE + "Skill Tree " + ChatColor.GRAY + "or from items!",
                        "",
                        ChatColor.RED + "&c❤ (Health): " + statPrefix(healthBonus) + healthBonus,
                        formattedStat("Dexterity", dexterity),
                        formattedStat("Intelligence", intelligence),
                        formattedStat("Strength", strength),
                        formattedStat("Vitality", vitality),
                        formattedStat("Wisdom", wisdom)
                }
        );
    }

    private ItemStack dexterityMenuItem() {
        double dodgeChancePercent = (Stat.getDodgeChance() * 100) * dexterity;
        double moveSpeedPercent = (Stat.getMovementSpeedMult() * 100) * dexterity;
        if (moveSpeedPercent > Stat.getMovementSpeedCap())
            moveSpeedPercent = Stat.getMovementSpeedCap();
        double rangedDamagePercent = (Stat.getRangedDmgMult() * 100) * dexterity;
        String dodgeChanceString = dodgeChancePercent > 0 ? DECIMAL_FORMAT.format(dodgeChancePercent) : "0";
        String moveSpeedString = moveSpeedPercent > 0 ? DECIMAL_FORMAT.format(moveSpeedPercent) : "0";
        String rangedDamageString = rangedDamagePercent > 0 ? DECIMAL_FORMAT.format(rangedDamagePercent) : "0";
        return GUIUtil.dispItem(Material.QUARTZ, ChatColor.YELLOW + "Dexterity: " + dexterity, new String[]
                {
                        "",
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Combat bonuses:",
                        ChatColor.GRAY + "Dodge chance: " + statPrefix(dexterity) + dodgeChanceString + "%",
                        ChatColor.GRAY + "Movespeed: " + statPrefix(dexterity) + moveSpeedString + "%" + (moveSpeedPercent >= Stat.getMovementSpeedCap() ? " (Cap Reached)" : ""),
                        ChatColor.GRAY + "Ranged Damage: " + statPrefix(dexterity) + rangedDamageString + "%"
                });
    }

    private ItemStack intelligenceMenuItem() {
        double manaRegenPercent = (Stat.getManaRegenMult() * 100) * intelligence;
        double spellDamagePercent = (Stat.getMagicDmgMult() * 100) * intelligence;
        String manaRegenString = manaRegenPercent > 0 ? DECIMAL_FORMAT.format(manaRegenPercent) : "0";
        String spellDamageString = manaRegenPercent > 0 ? DECIMAL_FORMAT.format(spellDamagePercent) : "0";
        return GUIUtil.dispItem(Material.LAPIS_LAZULI, ChatColor.YELLOW + "Intelligence: " + intelligence, new String[]
                {
                        "",
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Combat bonuses:",
                        ChatColor.GRAY + "Mana Regen: " + statPrefix(intelligence) + manaRegenString + "%",
                        ChatColor.GRAY + "Spell Damage: " + statPrefix(intelligence) + spellDamageString + "%"
                });
    }

    private ItemStack strengthMenuItem() {
        double critChancePercent = (Stat.getCriticalChance() * 100) * strength;
        double weaponDamagePercent = (Stat.getMeleeDmgMult() * 100) * strength;
        String critChanceString = critChancePercent > 0 ? DECIMAL_FORMAT.format(critChancePercent) : "0";
        String weaponDamageString = weaponDamagePercent > 0 ? DECIMAL_FORMAT.format(weaponDamagePercent) : "0";
        return GUIUtil.dispItem(Material.REDSTONE, ChatColor.YELLOW + "Strength: " + strength, new String[]
                {
                        "",
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Combat bonuses:",
                        ChatColor.GRAY + "Crit Chance: " + statPrefix(strength) + critChanceString + "%",
                        ChatColor.GRAY + "Weapon Damage: " + statPrefix(strength) + weaponDamageString + "%"
                });
    }

    private ItemStack wisdomMenuItem() {
        double maxManaPercent = (Stat.getMaxManaMult() * 100) * wisdom;
        double spellHealingPercent = (Stat.getSpellHealingMult() * 100) * wisdom;
        String maxManaString = maxManaPercent > 0 ? DECIMAL_FORMAT.format(maxManaPercent) : "0";
        String spellHealingString = spellHealingPercent > 0 ? DECIMAL_FORMAT.format(spellHealingPercent) : "0";
        return GUIUtil.dispItem(Material.EMERALD, ChatColor.YELLOW + "Wisdom: " + wisdom, new String[]
                {
                        "",
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Combat bonuses:",
                        ChatColor.GRAY + "Max Mana: " + statPrefix(wisdom) + maxManaString + "%",
                        ChatColor.GRAY + "Spell Healing: " + statPrefix(wisdom) + spellHealingString + "%"
                });
    }

    private ItemStack vitalityMenuItem() {
        double defensePercent = (Stat.getDamageReductionMult() * 100) * vitality;
        if (defensePercent > (Stat.getDamageReductionCap()))
            defensePercent = Stat.getDamageReductionCap(); // cap it
        double healthRegenPercent = (Stat.getHealthRegenMult() * 100) * vitality;
        String defenseString = defensePercent > 0 ? DECIMAL_FORMAT.format(defensePercent) : "0";
        String healthRegenString = healthRegenPercent > 0 ? DECIMAL_FORMAT.format(healthRegenPercent) : "0";
        return GUIUtil.dispItem(Material.DIAMOND, ChatColor.YELLOW + "Vitality: " + vitality, new String[]
                {
                        "",
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Combat bonuses:",
                        ChatColor.GRAY + "Defense: " + statPrefix(vitality) + defenseString + "%" + (defensePercent >= Stat.getDamageReductionCap() ? " (Cap Reached)" : ""),
                        ChatColor.GRAY + "Health Regen: " + statPrefix(vitality) + healthRegenString + "%"
                });
    }
}