package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings("FieldCanBeLocal")
public class IceBlock extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 25;
    private static final double DAMAGE_PER_LEVEL = 2.75;
    private static final int DURATION = 5;
    private static final int RADIUS = 5;

    public IceBlock() {
        super("Ice Block",
                "You entomb yourself in ice for " + DURATION +
                        "s, rooting you and granting you invulnerability! " +
                        "After, the ice block explodes, dealing (" + DAMAGE_AMT +
                        " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) spellʔ damage " +
                        "to enemies within " + RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.MAGE, 18, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        // on-use
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
        Location toBeTrapped = pl.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        addStatusEffect(pl, EffectEnum.ROOT, DURATION);
        addStatusEffect(pl, EffectEnum.INVULN, DURATION);
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20, Color.AQUA);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> pl.teleport(toBeTrapped), 2L);
        // after duration
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.5f);
            pl.getWorld().spawnParticle(Particle.SNOWBALL, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
            for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                if (!verifyEnemy(pl, en)) continue;
                en.getWorld().spawnParticle(Particle.SNOWBALL, en.getLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
                DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, pl, this);
            }
        }, DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

