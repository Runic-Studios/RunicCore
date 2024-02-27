package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.ChilledEffect;
import com.runicrealms.plugin.spellapi.event.ModeledStandCollideEvent;
import com.runicrealms.plugin.spellapi.modeled.CollisionCause;
import com.runicrealms.plugin.spellapi.modeled.ModeledStand;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;

/**
 * @author BoBoBalloon, Skyfallin
 */
public class Icebolt extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell {
    private static final int ICEBOLT_MODEL_DATA = 2244;
    private static final double HITBOX_SCALE = 0.5;
    private static final double SPEED = 2.5;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;
    private double empoweredDamage;
    private double empoweredDamagePerLevel;
    private double slowDuration;

    public Icebolt() {
        super("Icebolt", CharacterClass.MAGE);
        this.setDescription("You fire a bolt of ice that deals (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) " +
                "and applies &bchilled &7to enemies hit for " + duration + "s. " +
                "If enemies are already &bchilled&7, remove the effect, " +
                "then this spell instead deals (" + this.empoweredDamage + " + &f" + this.empoweredDamagePerLevel +
                "x&7 lvl) magicʔ damage and slows them for " + slowDuration + "s! " +
                "\n\n&2&lEFFECT &bChilled" +
                "\n&bChilled &7enemies are fraught with cold! " +
                "Some spells are empowered against chilled targets.");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);

        Number empoweredDamage = (Number) spellData.getOrDefault("empowered-damage", 75);
        setEmpoweredDamage(empoweredDamage.doubleValue());

        Number empoweredDamagePerLevel = (Number) spellData.getOrDefault("empowered-damage-per-level", 3.0);
        setEmpoweredDamagePerLevel(empoweredDamagePerLevel.doubleValue());

        Number slowDuration = (Number) spellData.getOrDefault("slow-duration", 2.0);
        setSlowDuration(slowDuration.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        final Vector vector = player.getLocation().getDirection().normalize().multiply(SPEED);
        ModeledStand icebolt = new ModeledStand(
                player,
                player.getEyeLocation(),
                vector,
                ICEBOLT_MODEL_DATA,
                4.0,
                HITBOX_SCALE,
                entity -> TargetUtil.isValidEnemy(player, entity)
        );
        EntityTrail.entityTrail(icebolt.getArmorStand(), Color.fromRGB(178, 216, 216), 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5F, 1);
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
        return this.damage;
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIceboltHit(ModeledStandCollideEvent event) {
        if (event.getModeledStand().getCustomModelData() != ICEBOLT_MODEL_DATA) return;
        if (event.getCollisionCause() != CollisionCause.ENTITY) return;
        Player player = event.getModeledStand().getPlayer();
        LivingEntity livingEntity = event.getEntity();
        // Apply or consume chilled, deal damage
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), livingEntity.getUniqueId(), SpellEffectType.CHILLED);
        if (spellEffectOpt.isPresent()) {
            ChilledEffect chilledEffect = (ChilledEffect) spellEffectOpt.get();
            chilledEffect.cancel();
            double damagePerLevel = this.empoweredDamagePerLevel * player.getLevel();
            DamageUtil.damageEntitySpell(this.empoweredDamage + damagePerLevel, livingEntity, player); // No spell scaling to apply custom
        } else {
            ChilledEffect chilledEffect = new ChilledEffect(player, livingEntity, this.duration);
            chilledEffect.initialize();
            DamageUtil.damageEntitySpell(this.damage, livingEntity, player, this);
        }
    }

    public void setSlowDuration(double slowDuration) {
        this.slowDuration = slowDuration;
    }

    public void setEmpoweredDamage(double empoweredDamage) {
        this.empoweredDamage = empoweredDamage;
    }

    public void setEmpoweredDamagePerLevel(double empoweredDamagePerLevel) {
        this.empoweredDamagePerLevel = empoweredDamagePerLevel;
    }
}

