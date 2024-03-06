package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.modeled.ModeledStandAnimated;
import com.runicrealms.plugin.spellapi.modeled.StandSlot;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;

public class ArcaneSlash extends Spell implements DistanceSpell, MagicDamageSpell, ShieldingSpell {
    public static final double BEAM_WIDTH = 2;
    private static final int MODEL_DATA = 2702;
    private static final int[] MODEL_DATA_ARRAY = new int[]{
            MODEL_DATA,
            2703,
            2704,
            2705,
            2706,
            2707,
            2708,
    };
    private static final int[] ARM_ROTATION_ARRAY = new int[]{
            75,
            -35,
            -75
    };
    private static final int[] MODEL_DATA_ARRAY_REVERSED;
    private static final Random random = new Random();

    static {
        MODEL_DATA_ARRAY_REVERSED = reversedArray();
    }

    public double distance;
    private double damage;
    private double shield;
    private double damagePerLevel;
    private double shieldPerLevel;

    public ArcaneSlash() {
        super("Arcane Slash", CharacterClass.MAGE);
        this.setDescription("You slash in a line in front of you, " +
                "dealing (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) magicʔ damage to all enemies. " +
                "If you hit at least one enemy, gain a " +
                "&eshield &7equal to (" + shield + " + &f" + shieldPerLevel +
                "x&7 lvl) health!" +
                "\n\n&2&lEFFECT &eShield" +
                "\n&7Shields absorb damage and appear as yellow hearts!");

    }

    public static void spawnParticle(Player player) {
        Vector direction = player.getLocation().getDirection();
        Vector forward = direction.multiply(3); // Adjust the multiplier to set the distance in front of the player
        Location spawnLocation = player.getLocation().add(forward);
        int angle = ARM_ROTATION_ARRAY[random.nextInt(ARM_ROTATION_ARRAY.length)];
        ModeledStandAnimated modeledStandAnimated = new ModeledStandAnimated(
                player,
                spawnLocation,
                new Vector(0, 0, 0),
                MODEL_DATA,
                4.0,
                1.0,
                StandSlot.ARM,
                null,
                angle == 75 ? MODEL_DATA_ARRAY : MODEL_DATA_ARRAY_REVERSED
        );
        modeledStandAnimated.getArmorStand().setRightArmPose(new EulerAngle(0, 0, angle));
    }

    private static int[] reversedArray() {
        int[] result = new int[MODEL_DATA_ARRAY.length];
        for (int i = 0; i < MODEL_DATA_ARRAY.length; i++) {
            result[i] = MODEL_DATA_ARRAY[MODEL_DATA_ARRAY.length - i - 1];
        }
        return result;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> TargetUtil.isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getLocation().add(0, 1, 0).getY());
            spawnParticle(player);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            spawnParticle(player);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> TargetUtil.isValidEnemy(player, target));
            targets.forEach(target -> DamageUtil.damageEntitySpell(damage, (LivingEntity) target, player, this));
            if (targets.size() > 0) {
                shieldPlayer(player, player, shield, this);
            }
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
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = (int) magicDamage;
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

