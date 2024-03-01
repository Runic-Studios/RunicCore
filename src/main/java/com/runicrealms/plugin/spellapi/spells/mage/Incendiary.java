package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.api.event.StaffAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.IncendiaryEffect;
import com.runicrealms.plugin.spellapi.event.ModeledSpellCollideEvent;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.modeled.CollisionCause;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpellProjectile;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

/**
 * New pyromancer passive 1
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Incendiary extends Spell implements MagicDamageSpell, DistanceSpell, DurationSpell {
    private static final double HITBOX_SCALE = 1.0;
    private static final double SPEED = 1.0;
    private static final String MODEL_ID = "meteor_storm_meteor";
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;

    public Incendiary() {
        super("Incendiary", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Each time you cast a &6Pyromancer&7 spell, " +
                "you become &cincendiary &7for the next " + duration + "s!" +
                "\n\n&2&lEFFECT &cIncendiary" +
                "\n&7Your first basic attack while &cincendiary &7releases a slow-moving " +
                "ball of fire, dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) " +
                "magicʔ damage to enemies it passes through!");
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
        player.getWorld().playSound(castLocation, Sound.ITEM_FIRECHARGE_USE, 0.25f, 2.0f);
        final Vector vector = castLocation.getDirection().normalize().multiply(SPEED);
        ModeledSpellProjectile projectile = new ModeledSpellProjectile(
                player,
                MODEL_ID,
                castLocation,
                vector,
                HITBOX_SCALE,
                -1,
                3.0,
                target -> TargetUtil.isValidEnemy(player, target)
        );
        projectile.initialize();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(ModeledSpellCollideEvent event) {
        if (!event.getModeledSpell().getModelId().equals(MODEL_ID)) return;
        if (event.getCollisionCause() != CollisionCause.ENTITY) return;
        Player player = event.getModeledSpell().getPlayer();
        LivingEntity livingEntity = event.getEntity();
        DamageUtil.damageEntitySpell(this.damage, livingEntity, player, this);
        livingEntity.getWorld().spawnParticle(Particle.FLAME, livingEntity.getEyeLocation(), 3, 0.5F, 0.5F, 0.5F, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // last
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

