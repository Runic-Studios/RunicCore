package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.event.ModeledSpellCollideEvent;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpellProjectile;
import com.runicrealms.plugin.spellapi.modeled.ProjectileType;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

public class HealingSprite extends Spell implements DistanceSpell, HealingSpell, MagicDamageSpell, RadiusSpell {
    private static final double HITBOX_SCALE = 1.0;
    private static final double SPEED = 1.25;
    private static final String MODEL_ID = "dawnbringer_angel";
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double healAmt;
    private double healingPerLevel;
    private double radius;

    public HealingSprite() {
        super("Healing Sprite", CharacterClass.CLERIC);
        this.setDescription("You launch a magical sprite that descends toward the ground! " +
                "Upon reaching its destination or hitting an enemy, " +
                "the sprite releases a burst of magic! " +
                "Allies within " + radius + " blocks of the sprite " +
                "are healed✦ for (" + healAmt + " + &f" +
                healingPerLevel + "x&7 lvl) health! " +
                "Against enemies, the burst deals (" + damage + " + &f" +
                damagePerLevel + "x&7 lvl) magicʔ damage!");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHit(ModeledSpellCollideEvent event) {
        if (!event.getModeledSpell().getModelId().equals(MODEL_ID)) return;
        Player player = event.getModeledSpell().getPlayer();
        Entity sprite = event.getModeledSpell().getEntity();
        sprite.getWorld().playSound(sprite.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 0.75f, 2.0f);
        sprite.getWorld().playSound(sprite.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.75F, 0.5F);
        sprite.getWorld().playSound(sprite.getLocation(), Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.75F, 1.0F);
        sprite.getWorld().playSound(sprite.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.75F, 12.0f);
        sprite.getWorld().spawnParticle(
                Particle.FIREWORKS_SPARK,
                sprite.getLocation(),
                25,
                2.0f,
                2.0f,
                2.0f,
                0
        );
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.FIREWORKS_SPARK, sprite.getLocation().add(0, 0.5f, 0));

        for (Entity entity : sprite.getWorld().getNearbyEntities(sprite.getLocation(), radius, radius, radius)) {
            if (TargetUtil.isValidAlly(player, entity))
                healPlayer(player, (Player) entity, healAmt, this);
            if (TargetUtil.isValidEnemy(player, entity))
                DamageUtil.damageEntitySpell(damage, ((LivingEntity) entity), player, this);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getEyeLocation();
        player.getWorld().playSound(castLocation, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 0.5f, 2.0f);
        player.getWorld().playSound(castLocation, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 4.0f);
        final Vector vector = castLocation.getDirection().normalize().multiply(SPEED);
        ModeledSpellProjectile projectile = new ModeledSpellProjectile(
                player,
                MODEL_ID,
                ProjectileType.THROWN,
                castLocation,
                vector,
                HITBOX_SCALE,
                this.distance,
                10.0, // max before projectile is force-removed
                target -> TargetUtil.isValidEnemy(player, target)
        );
        EntityTrail.entityTrail(projectile.getEntity(), Particle.END_ROD);
        projectile.initialize();
    }

    @Override
    public double getHeal() {
        return healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
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

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }
}

