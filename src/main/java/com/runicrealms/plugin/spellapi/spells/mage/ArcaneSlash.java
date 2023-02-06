package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
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

public class ArcaneSlash extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 30;
    private static final int MAX_DIST = 2;
    private static final double BEAM_WIDTH = 2;
    private static final double DAMAGE_PER_LEVEL = 0.5;

    public ArcaneSlash() {
        super("Arcane Slash",
                "You slash in a line in front of you, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to all enemies. " +
                        "If you hit at least one enemy, gain a " +
                        "shield equal to (50 + 1.0 x lvl) health!",
                ChatColor.WHITE, CharacterClass.MAGE, 10, 20);

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
            location.setY(player.getLocation().add(0, 1, 0).getY());
            SlashEffect.slashHorizontal(player, Particle.SPELL_WITCH);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            SlashEffect.slashHorizontal(player, Particle.SPELL_WITCH);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
//            livingEntity.getWorld().spawnParticle(Particle.SOUL, livingEntity.getEyeLocation(), 8, 0.8f, 0.5f, 0.8f, 0);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target));
            targets.forEach(target -> DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) target, player, this));
            // todo: shield effect
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }


}

