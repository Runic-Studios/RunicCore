package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class FireBlast extends Spell implements MagicDamageSpell {
    private static final int DAMAGE_AMOUNT = 15;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 4;
    private static final double KNOCKUP_MULTIPLIER = 1.0;
    private static final double RAY_SIZE = 2.5D;

    public FireBlast() {
        super("Fire Blast",
                "You erupt a powerful blast of fire at " +
                        "your target enemy or location that deals " +
                        "(" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies within " + RADIUS + " blocks and " +
                        "knocks them up them up!",
                ChatColor.WHITE, CharacterClass.MAGE, 12, 30);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );

        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }

        fireBlast(player, location);
    }

    /**
     * Erupts a column of flame at the given location and knocks up all enemies in the radius
     *
     * @param player        who cast the spell
     * @param blastLocation to erupt the flame
     */
    private void fireBlast(Player player, Location blastLocation) {
        player.getWorld().playSound(blastLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.LAVA, blastLocation, 25, 0.3f, 0.3f, 0.3f, 0);

        for (Entity entity : player.getWorld().getNearbyEntities(blastLocation, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            LivingEntity livingEntity = (LivingEntity) entity;
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, livingEntity, player, this);
            entity.getWorld().spawnParticle(Particle.FLAME, livingEntity.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            entity.setVelocity(new Vector(0, 1, 0).normalize().multiply(KNOCKUP_MULTIPLIER));
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

