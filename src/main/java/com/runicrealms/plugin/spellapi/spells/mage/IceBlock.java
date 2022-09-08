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

    private static final int DAMAGE_AMT = 15;
    private static final int DAMAGE_PER_LEVEL = 2;
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
    public void executeSpell(Player player, SpellItemType type) {
        // on-use
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
        Location castLocation = player.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
        addStatusEffect(player, EffectEnum.ROOT, DURATION);
        addStatusEffect(player, EffectEnum.INVULN, DURATION);
        Cone.coneEffect(player, Particle.REDSTONE, DURATION, 0, 20, Color.AQUA);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> player.teleport(castLocation), 2L);
        // after duration
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.5f);
            player.getWorld().spawnParticle(Particle.SNOWBALL, player.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
            for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                if (!verifyEnemy(player, entity)) continue;
                entity.getWorld().spawnParticle(Particle.SNOWBALL, entity.getLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
                DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) entity, player, this);
            }
        }, DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

