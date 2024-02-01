package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CallOfTheDeep extends Spell implements WarmupSpell, PhysicalDamageSpell, DurationSpell {
    private final Map<UUID, Long> harpooned;
    private double dashBuff;
    private double dashBuffDuration;
    private double warmup;
    private double damage;
    private double damagePerLevel;
    private double duration;

    public CallOfTheDeep() {
        super("Call Of The Deep", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("After you LAND &aharpoon &7on an enemy," +
                " refund half of &aHarpoon&7's cooldown! " +
                "Additionally, reduce the cooldown of &aDash&7 by " +
                this.dashBuff + "s for every basic attack on the " +
                "harpooned enemy within the next " + this.dashBuffDuration + "s. " +
                "Applying the &aScurvy&7 debuff on enemies inside &aWhirlpool&7 summons " +
                "a creature from the depths to their location after a " +
                this.warmup + "s delay! This deals (" + this.damage + " + &f" +
                this.damagePerLevel + "x&7 lvl) physical⚔ damage and " +
                "stuns the affected enem(ies) for " + this.duration + "s!");
        this.harpooned = new HashMap<>();
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number dashBuff = (Number) spellData.getOrDefault("dash-buff", 0.5);
        this.dashBuff = dashBuff.doubleValue();
        Number dashBuffDuration = (Number) spellData.getOrDefault("dash-buff-duration", 3.0);
        this.dashBuffDuration = dashBuffDuration.doubleValue();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onHarpoonHit(Harpoon.HarpoonHitEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        Spell harpoon = RunicCore.getSpellAPI().getSpell("Harpoon");

        if (harpoon == null) {
            return;
        }

        RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), harpoon, RunicCore.getSpellAPI().getUserCooldown(event.getCaster(), harpoon) / 2);
        this.harpooned.put(event.getVictim().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        Long time = this.harpooned.get(event.getVictim().getUniqueId());

        if (time == null || System.currentTimeMillis() > time + (this.dashBuffDuration * 1000)) {
            return;
        }

        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Dash", this.dashBuff);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onScurvyDebuff(Scurvy.DebuffEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        Spell spell = RunicCore.getSpellAPI().getSpell("Whirlpool");

        if (!(spell instanceof Whirlpool whirlpool) || !whirlpool.isInWhirlPool(event.getVictim())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            DamageUtil.damageEntityPhysical(this.damage, event.getVictim(), event.getCaster(), false, false, false, this);
            this.addStatusEffect(event.getVictim(), RunicStatusEffect.STUN, this.duration, true);
        }, (long) (this.warmup * 20));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onMythicMobDeath(MythicMobDeathEvent event) {
        this.harpooned.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onRunicDeath(RunicDeathEvent event) {
        this.harpooned.remove(event.getVictim().getUniqueId());
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.harpooned.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public double getWarmup() {
        return this.warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }

    @Override
    public double getPhysicalDamage() {
        return this.damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}

