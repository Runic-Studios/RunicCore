package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.event.ModeledStandCollideEvent;
import com.runicrealms.plugin.spellapi.modeled.CollisionCause;
import com.runicrealms.plugin.spellapi.modeled.ModeledStand;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

public class Fireball extends Spell implements MagicDamageSpell {
    private static final int FIREBALL_MODEL_DATA = 2251;
    private static final double HITBOX_SCALE = 0.5;
    private static final double SPEED = 3.5;
    private double magicDamage;
    private double magicDamagePerLevel;

    public Fireball() {
        super("Fireball", CharacterClass.MAGE);
        this.setDescription
                (
                        "You launch a projectile fireball that deals " +
                                "(" + this.magicDamage + " + &f" + this.magicDamagePerLevel + "x&7 lvl) " +
                                "magicʔ damage on impact!"
                );
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        final Vector vector = player.getEyeLocation().getDirection().normalize().multiply(SPEED);
        ModeledStand fireball = new ModeledStand(
                player,
                player.getEyeLocation(),
                vector,
                FIREBALL_MODEL_DATA,
                4.0,
                HITBOX_SCALE,
                entity -> TargetUtil.isValidEnemy(player, entity)
        );
        EntityTrail.entityTrail(
                fireball.getArmorStand(),
                Particle.FLAME,
                1.0f
        );
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @Override
    public double getMagicDamage() {
        return this.magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireballHit(ModeledStandCollideEvent event) {
        if (event.getModeledStand().getCustomModelData() != FIREBALL_MODEL_DATA) return;
        if (event.getCollisionCause() != CollisionCause.ENTITY) return;
        Player player = event.getModeledStand().getPlayer();
        LivingEntity livingEntity = event.getEntity();
        DamageUtil.damageEntitySpell(this.magicDamage, livingEntity, player, this);
        livingEntity.getWorld().spawnParticle(Particle.FLAME, livingEntity.getEyeLocation(), 3, 0.5F, 0.5F, 0.5F, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }
}

