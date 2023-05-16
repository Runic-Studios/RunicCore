package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sprint extends Spell implements DurationSpell, PhysicalDamageSpell {
    private final Map<UUID, BukkitTask> sprintTasks = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private int level;

    public Sprint() {
        super("Sprint", CharacterClass.ROGUE);
        this.setDescription("For " + duration + "s, you gain a " +
                "massive boost of speed! While the speed persists, your first melee attack against " +
                "an enemy deals (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        switch (level) {
            case 1 -> addStatusEffect(player, RunicStatusEffect.SPEED_I, duration, false);
            case 3 -> addStatusEffect(player, RunicStatusEffect.SPEED_III, duration, false);
            default -> addStatusEffect(player, RunicStatusEffect.SPEED_II, duration, false);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getLocation(), Color.FUCHSIA);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getEyeLocation(), Color.FUCHSIA);
        BukkitTask sprintDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> sprintTasks.remove(player.getUniqueId()), (int) duration * 20L);
        sprintTasks.put(player.getUniqueId(), sprintDamageTask);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number level = (Number) spellData.getOrDefault("level", 0);
        setLevel(level.intValue());
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!sprintTasks.containsKey(event.getPlayer().getUniqueId())) return;
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, event.getVictim().getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
        DamageUtil.damageEntityPhysical(damage, event.getVictim(), player, false, false, this);
        sprintTasks.remove(player.getUniqueId());
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

