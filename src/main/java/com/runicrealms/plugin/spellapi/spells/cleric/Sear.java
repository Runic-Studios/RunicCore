package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("FieldCanBeLocal")
public class Sear extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int MAX_DIST = 10;
    private final double BEAM_WIDTH = 1.5;
    private final Map<UUID, BukkitTask> atoningEntitiesMap = new ConcurrentHashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double radius;

    public Sear() {
        super("Sear", CharacterClass.CLERIC);
        this.setDescription("You fire a beam of light, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to the first enemy hit " +
                "and applying &oAtone &7for " + duration + "s. " +
                "If the atoning enemy suffers damage from any source other than Sear, " +
                "&7&oAtone &7detonates, dealing this spell's damage " +
                "again in a " + radius + " block radius around the enemy!");
    }

    private void applyAtonement(Player caster, LivingEntity victim) {
        atoningEntitiesMap.put(victim.getUniqueId(), new BukkitRunnable() {
            double count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                    atoningEntitiesMap.remove(victim.getUniqueId());
                } else {
                    count += 1;
                    new HorizontalCircleFrame((float) 0.5, false).playParticle(caster, Particle.FLAME, victim.getEyeLocation(), 30, Color.YELLOW);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L));
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
            player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 8, 0.5f, 0.5f, 0.5f, 0);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.75D, 1, 0.15f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
            livingEntity.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, livingEntity.getEyeLocation(), 8, 0.8f, 0.5f, 0.8f, 0);
            applyAtonement(player, livingEntity);
            DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
        }
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

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.getSpell() != null && event.getSpell() instanceof Sear) return;
        if (!atoningEntitiesMap.containsKey(event.getVictim().getUniqueId())) return;
        atoningEntitiesMap.get(event.getVictim().getUniqueId()).cancel();
        atoningEntitiesMap.remove(event.getVictim().getUniqueId());
        Player caster = event.getPlayer();
        LivingEntity victim = event.getVictim();
        for (Entity entity : victim.getWorld().getNearbyEntities(victim.getLocation(), radius, radius, radius, target -> isValidEnemy(caster, target))) {
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, caster, this);
        }
    }

    @EventHandler
    public void onMobDeath(MythicMobDeathEvent event) {
        if (event.getEntity().getCustomName() != null) {
            Bukkit.broadcastMessage(event.getEntity().getCustomName());
        }
        if (!atoningEntitiesMap.containsKey(event.getEntity().getUniqueId())) return;
        atoningEntitiesMap.get(event.getEntity().getUniqueId()).cancel();
        atoningEntitiesMap.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!atoningEntitiesMap.containsKey(event.getVictim().getUniqueId())) return;
        atoningEntitiesMap.get(event.getVictim().getUniqueId()).cancel();
        atoningEntitiesMap.remove(event.getVictim().getUniqueId());
        Player caster = event.getPlayer();
        LivingEntity victim = event.getVictim();
        for (Entity entity : victim.getWorld().getNearbyEntities(victim.getLocation(), radius, radius, radius, target -> isValidEnemy(caster, target))) {
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, caster, this);
        }
    }

    @EventHandler
    public void onPlayerDeath(RunicDeathEvent event) {
        if (!atoningEntitiesMap.containsKey(event.getVictim().getUniqueId())) return;
        atoningEntitiesMap.get(event.getVictim().getUniqueId()).cancel();
        atoningEntitiesMap.remove(event.getVictim().getUniqueId());
    }
}

