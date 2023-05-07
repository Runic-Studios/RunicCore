package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SnapFreeze extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell {
    private static final double PERIOD = 0.5;
    private static final int BEAM_RADIUS = 1;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>();
    private double distance;
    private double damage;
    private double damagePerLevel;
    private double duration;

    public SnapFreeze() {
        super("Snap Freeze", CharacterClass.MAGE);
        this.setDescription("You cast a wave of frost in a forward line, up to " + distance + " blocks away. " +
                "Enemies hit by the spell take (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage and are rooted for " + duration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getEyeLocation();
        freeze(player, castLocation);
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
                    freeze(player, castLocation);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
    }

    private void freeze(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        new HorizontalCircleFrame(BEAM_RADIUS, true).playParticle(player, Particle.BLOCK_CRACK, location);
        player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, BEAM_RADIUS, BEAM_RADIUS, BEAM_RADIUS, target -> isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.ROOT, duration, true);
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
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
}

