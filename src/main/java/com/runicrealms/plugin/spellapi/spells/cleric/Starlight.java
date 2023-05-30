package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Starlight extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final double PERIOD = 0.5;
    private static final int BEAM_RADIUS = 1;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>();
    private double distance;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double radius;

    public Starlight() {
        super("Starlight", CharacterClass.CLERIC);
        this.setDescription("You release a crescent wave of starlight in front of you! " +
                "Enemies suffer (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "and are silenced for " + duration + "s! When Starlight hits an enemy, " +
                "all allies within " + radius + " blocks of you have the " +
                "duration of their shields refreshed.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getEyeLocation();
        starlightEffect(player, castLocation);
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {
                if (count > distance) {
                    this.cancel();
                    damageMap.remove(player.getUniqueId());
                } else {
                    count += 1 * PERIOD;
                    castLocation.add(castLocation.getDirection());
                    starlightEffect(player, castLocation);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
    }

    private void starlightEffect(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        // Particles
        player.getWorld().spawnParticle(Particle.REDSTONE, location,
                2, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.YELLOW, 1));
        new HorizontalCircleFrame(BEAM_RADIUS, true).playParticle(player, Particle.BLOCK_CRACK, location, Color.YELLOW);
        player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.25f, 2.0f);
        player.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.25f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, BEAM_RADIUS, BEAM_RADIUS, BEAM_RADIUS, target -> isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.SILENCE, duration, true);
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
            // todo: shield refresh
        }
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
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
}

