package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;

public class TwinFangs extends Spell implements PhysicalDamageSpell {
    public static final int MAX_DIST = 3;
    public static final double BEAM_WIDTH = 2;
    private static final int DAMAGE = 30;
    private static final double DAMAGE_PER_LEVEL = 1.5;

    public TwinFangs() {
        super("Twin Fangs",
                "You lash out with two fangs, up to " + MAX_DIST + " blocks in front of you. " +
                        "Each fang deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) physical⚔ " +
                        "damage on-hit!",
                ChatColor.WHITE, CharacterClass.ROGUE, 10, 10);

    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_DEATH, 0.5f, 1.2f);
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
            location.setY(player.getLocation().add(0, 1, 0).getY());
            fangEffect(player);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            fangEffect(player);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target));
            targets.forEach(target -> DamageUtil.damageEntityPhysical(DAMAGE,
                    (LivingEntity) target, player, false, false, this));
        }
    }

    /**
     * Particle for the spell
     *
     * @param player who cast the spell
     */
    private void fangEffect(Player player) {
        SlashEffect.slashVertical(player, Particle.REDSTONE, true, Color.LIME);
        SlashEffect.slashVertical(player, Particle.REDSTONE, false, Color.LIME);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

}

