package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

@SuppressWarnings("FieldCanBeLocal")
public class Sear extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 20;
    private static final double DAMAGE_PER_LEVEL = 0.4;
    private static final int MAX_DIST = 10;
    private final double BEAM_WIDTH = 1.5;

    public Sear() {
        super("Sear",
                "You launch a ripple of magic, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to the first enemy hit!",
                ChatColor.WHITE, CharacterClass.CLERIC, 8, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
            player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 8, 0.5f, 0.5f, 0.5f, 0);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.75D, 1, 0.15f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
            livingEntity.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, livingEntity.getEyeLocation(), 8, 0.8f, 0.5f, 0.8f, 0);
            DamageUtil.damageEntitySpell(DAMAGE, livingEntity, player, this);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

