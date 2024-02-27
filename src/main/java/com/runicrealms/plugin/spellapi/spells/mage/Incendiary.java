package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.StaffAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.IncendiaryEffect;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * New pyromancer passive 1
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Incendiary extends Spell implements MagicDamageSpell, DistanceSpell, DurationSpell {
    private static final double PERIOD = 0.5;
    private static final int BEAM_RADIUS = 1;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>(); // Prevents repeat hits on same target
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;

    public Incendiary() {
        super("Incendiary", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Each time you cast a &6Pyromancer&7 spell, " +
                "you become &oincendiary &7for the next " + duration + "s, engulfing you in flame! " +
                "Your first basic attack while incendiary releases a wave of fire " +
                "up to " + this.distance + " blocks away, " +
                "dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage " +
                "to enemies it passes through!");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onStaffAttack(StaffAttackEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.INCENDIARY);
        if (spellEffectOpt.isEmpty()) return;
        IncendiaryEffect incendiaryEffect = (IncendiaryEffect) spellEffectOpt.get();
        incendiaryEffect.cancel();
        startEffect(event.getPlayer());
    }

    private void startEffect(Player player) {
        Location castLocation = player.getEyeLocation();
        fireWaveEffect(player, castLocation);
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
                    fireWaveEffect(player, castLocation);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
    }

    private void fireWaveEffect(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        // Particles
        player.getWorld().spawnParticle(Particle.REDSTONE, location,
                2, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.YELLOW, 1));
        new HorizontalCircleFrame(BEAM_RADIUS, true).playParticle(player, Particle.FLAME, location, 50.0f);
        player.getWorld().playSound(location, Sound.ITEM_FIRECHARGE_USE, 0.25f, 2.0f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, BEAM_RADIUS, BEAM_RADIUS, BEAM_RADIUS, target -> TargetUtil.isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    private void onSpellCast(SpellCastEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        if (!(event.getSpell() instanceof Fireball
                || event.getSpell() instanceof DragonsBreath
                || event.getSpell() instanceof Erupt
                || event.getSpell() instanceof Meteor)) {
            return;
        }
        Bukkit.broadcastMessage("incendiary");

        UUID uuid = event.getCaster().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.INCENDIARY);
        if (spellEffectOpt.isPresent()) {
            IncendiaryEffect incendiaryEffect = (IncendiaryEffect) spellEffectOpt.get();
            incendiaryEffect.refresh();
        } else {
            IncendiaryEffect incendiaryEffect = new IncendiaryEffect(event.getCaster(), this.duration);
            incendiaryEffect.initialize();
        }
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
    public double getDistance() {
        return this.distance;
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
}

