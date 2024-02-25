package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.ThreatUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Devour extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    public static final double BEAM_WIDTH = 2;
    private final Set<UUID> debuffedEntities = new HashSet<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double percent;
    private double radius;

    public Devour() {
        super("Devour", CharacterClass.WARRIOR);
        this.setDescription("You cleave in front of you, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "to enemies within " + radius + " blocks and lowering their damage " +
                "dealt by " + (percent * 100) + "% for " + duration + "s! " +
                "This spell also taunts monsters, causing them to attack you!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.25f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        radius,
                        BEAM_WIDTH,
                        entity -> TargetUtil.isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) radius).getLocation();
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getLocation().add(0, 1, 0).getY());
            SlashEffect.slashHorizontal(player.getLocation(), true, true, Particle.REDSTONE, 0.04f, Color.fromRGB(185, 251, 185));
            SlashEffect.slashHorizontal(player.getLocation().add(0, 0.5, 0), true, true, Particle.REDSTONE, 0.04f, Color.fromRGB(185, 251, 185));
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            SlashEffect.slashHorizontal(player.getLocation(), true, true, Particle.REDSTONE, 0.04f, Color.fromRGB(185, 251, 185));
            SlashEffect.slashHorizontal(player.getLocation().add(0, 0.5, 0), true, true, Particle.REDSTONE, 0.04f, Color.fromRGB(185, 251, 185));
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            for (Entity entity : player.getWorld().getNearbyEntities(livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> TargetUtil.isValidEnemy(player, target))) {
                debuffedEntities.add(entity.getUniqueId());
                Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                        () -> debuffedEntities.remove(entity.getUniqueId()), (long) duration * 20L);
                DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
                ThreatUtil.generateThreat(player, entity);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobDamage(MobDamageEvent event) {
        if (debuffedEntities.isEmpty()) return;
        if (event.isCancelled()) return;
        if (!debuffedEntities.contains(event.getMob().getUniqueId())) return;
        double reducedAmount = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reducedAmount));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (debuffedEntities.isEmpty()) return;
        if (event.isCancelled()) return;
        if (!debuffedEntities.contains(event.getVictim().getUniqueId())) return;
        double reducedAmount = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reducedAmount));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (debuffedEntities.isEmpty()) return;
        if (event.isCancelled()) return;
        if (!debuffedEntities.contains(event.getVictim().getUniqueId())) return;
        double reducedAmount = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reducedAmount));
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
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

}

