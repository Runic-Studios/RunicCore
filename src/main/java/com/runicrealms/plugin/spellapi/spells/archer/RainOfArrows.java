package com.runicrealms.plugin.spellapi.spells.archer;

import com.google.common.util.concurrent.AtomicDouble;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * New marksman ult spell
 *
 * @author BoBoBalloon
 */
public class RainOfArrows extends Spell implements RadiusSpell, DurationSpell, PhysicalDamageSpell {
    private static final int HEIGHT = 10;
    private static final double INTERVAL = 0.5; //time in seconds
    private final Map<UUID, Location> casting;
    private double radius;
    private double duration;
    private double damage;
    private double damagePerLevel;

    public RainOfArrows() {
        super("Rain of Arrows", CharacterClass.ARCHER);
        this.setDescription("You fire a torrent of arrows into the sky, bombarding targets in a " + this.radius + " block radius for the next " + this.duration + "s.\n" +
                "Arrows strike the area for (" + this.damage + " +&f " + this.damagePerLevel + "x&7 lvl) physical⚔ damage every " + INTERVAL + "s.\n" +
                "Any movement breaks your concentration and ends the spell early.");
        this.casting = new ConcurrentHashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        if (!player.getLocation().isWorldLoaded()) {
            return;
        }

        this.casting.put(player.getUniqueId(), player.getLocation());

        AtomicDouble duration = new AtomicDouble(this.duration);

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), task -> {
            Location location = this.casting.get(player.getUniqueId());

            if (duration.get() <= 0 || location == null || player.getLocation().getX() != location.getX() || player.getLocation().getY() != location.getY() || player.getLocation().getZ() != location.getZ()) {
                this.casting.remove(player.getUniqueId());
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.AMBIENT, 1, 1);
                task.cancel();
                return;
            }

            for (int i = 0; i < 6; i++) {
                double offsetX = (ThreadLocalRandom.current().nextDouble() * 2 * radius) - radius;
                double offsetZ = (ThreadLocalRandom.current().nextDouble() * 2 * radius) - radius;

                Location randomLocation = location.clone().add(offsetX, 0, offsetZ);

                location.getWorld().playSound(randomLocation, Sound.ENTITY_ARROW_HIT, 3.0F, 1.0F);
                VectorUtil.drawLine(player, Particle.CRIT, Color.WHITE, randomLocation.clone().add(0, HEIGHT, 0), randomLocation.clone().subtract(0, 20, 0), 2.0D, 5);
            }

            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                for (Entity entity : player.getNearbyEntities(this.radius, this.radius, this.radius)) {
                    if (!(entity instanceof LivingEntity target) || !TargetUtil.isValidEnemy(player, target)) {
                        continue;
                    }

                    DamageUtil.damageEntityPhysical(this.damage, target, player, false, true, this);
                }
            });

            duration.set(duration.get() - INTERVAL);
        }, 0, (long) (INTERVAL * 20));
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.casting.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
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
}
