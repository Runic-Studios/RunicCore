package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell implements DurationSpell, ShieldingSpell, RadiusSpell {
    private static final double UPDATES_PER_SECOND = 4;
    private final Map<Player, Location> judgmentCastersMap = new HashMap<>();
    private double bubbleDuration;
    private double shield;
    private double shieldPerLevel;
    private double knockbackMultiplier;
    private double radius;

    public Judgment() {
        super("Judgment", CharacterClass.WARRIOR);
        this.setDescription("You summon a barrier of magic " +
                "around yourself for " + bubbleDuration + "s, instantly knocking away all enemies! " +
                "Each second, allies within the barrier are shielded for " +
                "(" + shield + " + &f" + shieldPerLevel + "x&7 lvl) health! " +
                "You and allies within the barrier are invulnerable! " +
                "During this time, you are rooted. Sneak to cancel the spell early.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean attemptToExecute(Player player) {
        if (!player.isOnGround()) {
            player.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
            return false;
        }
        return true;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        judgmentCastersMap.put(player, player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        addStatusEffect(player, RunicStatusEffect.ROOT, bubbleDuration, true);

        // Knock targets away
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
            Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-knockbackMultiplier).setY(0.3);
            entity.setVelocity(force);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
        }

        // Shield caster and nearby allies
        BukkitTask healTask = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
                shieldPlayer(player, (Player) entity, shield, this);
            }
        }, 0, 20L);

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // Create visual bubble
                phi += Math.PI / 10;
                Location loc = player.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = radius * cos(theta) * sin(phi);
                    double y = radius * cos(phi) + 1.5;
                    double z = radius * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    player.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }

                // Radius indicator
                new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.VILLAGER_HAPPY, loc, Color.GREEN);

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > bubbleDuration * 1000 || player.isSneaking()) {
                    judgmentCastersMap.remove(player);
                    healTask.cancel();
                    this.cancel();
                    removeStatusEffect(player, RunicStatusEffect.ROOT);
                    return;
                }

                // More effect noises
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 2.0f);

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20 / UPDATES_PER_SECOND));
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        boolean isInsideAllyJudgment = checkForAnyAllyJudgment(event.getVictim(), event.getVictim().getLocation());
        if (isInsideAllyJudgment) {
            event.setCancelled(true);
        }
    }

    private boolean checkForAnyAllyJudgment(Entity victim, Location location) {
        if (judgmentCastersMap.isEmpty()) return false;
        Set<Location> validJudgmentLocations = new HashSet<>();
        for (Player caster : judgmentCastersMap.keySet()) {
            if (isValidAlly(caster, victim)) {
                validJudgmentLocations.add(judgmentCastersMap.get(caster));
            }
        }
        if (validJudgmentLocations.isEmpty()) return false;
        for (Location judgmentLoc : validJudgmentLocations) {
            if (location.distanceSquared(judgmentLoc) <= radius * radius) {
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.25f, 1.0f);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        boolean isInsideAllyJudgment = checkForAnyAllyJudgment(event.getVictim(), event.getVictim().getLocation());
        if (isInsideAllyJudgment) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        boolean isInsideAllyJudgment = checkForAnyAllyJudgment(event.getVictim(), event.getVictim().getLocation());
        if (isInsideAllyJudgment) {
            event.setCancelled(true);
        }
    }

    @Override
    public double getDuration() {
        return bubbleDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.bubbleDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number knockback = (Number) spellData.getOrDefault("knockback", 0);
        setKnockbackMultiplier(knockback.doubleValue());
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setKnockbackMultiplier(double knockbackMultiplier) {
        this.knockbackMultiplier = knockbackMultiplier;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }
}

