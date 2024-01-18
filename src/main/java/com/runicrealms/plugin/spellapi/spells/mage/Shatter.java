package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * First passive for Cryomancer
 *
 * @author BoBoBalloon
 */
public class Shatter extends Spell implements AttributeSpell, MagicDamageSpell, ShieldingSpell {
    private final Map<UUID, Long> cooldown;
    private double baseValue;
    private double damage;
    private double damagePerLevel;
    private double maxStacks;
    private double multiplier;
    private double shield;
    private double shieldPerLevel;
    private double stackDuration;
    private String statName;

    public Shatter() {
        super("Shatter", CharacterClass.MAGE);
        this.cooldown = new HashMap<>();
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("When you land a basic attack on an enemy that " +
                "is &bchilled&7, you shatter their ice, removing &bchilled " +
                "&7and dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage! " +
                "You also gain a stack of &fice barrier&7. " +
                "\n&2&lEFFECT &fIce Barrier" +
                "\n&fIce Barrier &7stacks reduce mob and physical damage taken by " +
                "(" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7)%! " +
                "Max " + maxStacks + " stacks. " +
                "Stacks expire after " + stackDuration + "s.");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 3);
        setMaxStacks(maxStacks.doubleValue());
        Number stackDuration = (Number) spellData.getOrDefault("stack-duration", 20);
        setStackDuration(stackDuration.doubleValue());
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getShield() {
        return this.shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return this.shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) ||
                (!this.hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT) && !this.hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.STUN))) {
            return;
        }

        Long cooldown = this.cooldown.get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();

        if (cooldown != null && cooldown + (this.getCooldown() * 1000) > now) { //multiply by 1000 to convert seconds to milliseconds
            return;
        }

        this.cooldown.put(event.getPlayer().getUniqueId(), now);

        this.removeStatusEffect(event.getVictim(), RunicStatusEffect.ROOT);
        this.removeStatusEffect(event.getVictim(), RunicStatusEffect.STUN);

        event.getPlayer().getWorld().playSound(event.getVictim().getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5F, 1);

        DamageUtil.damageEntitySpell(this.damage, event.getVictim(), event.getPlayer(), false, this);
        this.shieldPlayer(event.getPlayer(), event.getPlayer(), this.shield, this);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public void setStackDuration(double stackDuration) {
        this.stackDuration = stackDuration;
    }
}

