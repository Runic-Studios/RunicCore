package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * Big boy class. Used to generate armor, weapons, gems, artifacts, etc.
 */
public class RunicItem {

    /*
    Info to build item in constructor.
     */
    private Material mat;
    private ChatColor tier;
    private String name;
    private int durability;

    /*
    Required level to equip/use item
     */
    private int reqLevel;

    /*
    Stat bonuses of item
     */
    private int healthBonus;
    private int manaBonus;
    private int damageBonus;
    private int healingBonus;
    private int magicBonus;
    private int shield;

    /*
    Damage range for item (if weapon)
     */
    private int minDamage;
    private int maxDamage;

    /*
    Spell on item (if artifact)
     */
    private String spell;

    /*
     Used to get the final itemstack
     */
    private ItemStack item;

    public RunicItem(Material mat, ChatColor tier, String name, int durability, int reqLevel,
                     int healthBonus, int manaBonus, int damageBonus, int healingBonus, int magicBonus, int shield,
                     int minDamage, int maxDamage, String spell) {
        this.mat = mat;
        this.tier = tier;
        this.name = name;
        this.durability = durability;
        this.reqLevel = reqLevel;
        this.healthBonus = healthBonus;
        this.manaBonus = manaBonus;
        this.damageBonus = damageBonus;
        this.healingBonus = healingBonus;
        this.magicBonus = magicBonus;
        this.shield = shield;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.spell = spell;
    }

    /**
     * Method to build item from instanced variables.
     */
    private void buildItem() {
        item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        item.setItemMeta(meta);
        item = applyStats(item);
        LoreGenerator.generateItemLore(item, tier, name, "");
        setItem(item);
    }

    private ItemStack applyStats(ItemStack item) {
        if (reqLevel != 0) item = AttributeUtil.addCustomStat(item, "required.level", reqLevel);
        if (healthBonus != 0) item = AttributeUtil.addCustomStat(item, "custom.maxHealth", healthBonus);
        if (manaBonus != 0) item = AttributeUtil.addCustomStat(item, "custom.manaBoost", healthBonus);
        if (damageBonus != 0) item = AttributeUtil.addCustomStat(item, "custom.attackDamage", healthBonus);
        if (healingBonus != 0) item = AttributeUtil.addCustomStat(item, "custom.healingBoost", healthBonus);
        if (magicBonus != 0) item = AttributeUtil.addCustomStat(item, "custom.magicDamage", healthBonus);
        if (shield != 0) item = AttributeUtil.addCustomStat(item, "custom.shield", healthBonus);
        if (minDamage != 0) item = AttributeUtil.addCustomStat(item, "custom.minDamage", minDamage);
        if (maxDamage != 0) item = AttributeUtil.addCustomStat(item, "custom.maxDamage", maxDamage);
        if (!spell.equals("")) item = AttributeUtil.addSpell(item, "custom.maxDamage", spell);
        return item;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
