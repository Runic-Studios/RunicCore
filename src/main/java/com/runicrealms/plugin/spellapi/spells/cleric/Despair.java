package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;

@SuppressWarnings("FieldCanBeLocal")
public class Despair extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 30;
    private static final int MAX_DIST = 2;
    private static final double BEAM_WIDTH = 2;
    private static final double DAMAGE_PER_LEVEL = 1.2;
    private static final double PERCENT = 2.5;

    public Despair() {
        super("Despair",
                "You conjure a cone of dark energy in front of you. " +
                        "Enemies hit take (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage! If only 1 enemy is struck, " +
                        "they take " + (int) (PERCENT * 100) + "% base damage!",
                ChatColor.WHITE, CharacterClass.CLERIC, 6, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_DEATH, 0.5f, 1.0f);
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
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getEyeLocation().getY());
            new HorizontalCircleFrame((float) BEAM_WIDTH, true).playParticle(player, Particle.SOUL_FIRE_FLAME, location);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            new HorizontalCircleFrame((float) BEAM_WIDTH, true).playParticle(player, Particle.SOUL_FIRE_FLAME, livingEntity.getEyeLocation().setDirection(player.getLocation().getDirection()));
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.25f);
            livingEntity.getWorld().spawnParticle(Particle.SOUL, livingEntity.getEyeLocation(), 8, 0.8f, 0.5f, 0.8f, 0);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target));
            if (targets.size() == 1) {
                targets.forEach(target -> DamageUtil.damageEntitySpell(DAMAGE * PERCENT, (LivingEntity) target, player, this));
            } else {
                targets.forEach(target -> DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) target, player, this));
            }
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

