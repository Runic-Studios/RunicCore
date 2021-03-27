package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Discord extends Spell {

    private static final int DELAY = 1;
    private static final int DAMAGE_AMT = 8;
    private static final int DURATION = 2;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 8;

    public Discord() {
        super("Discord",
                "You target a location within " + MAX_DIST + " blocks, " +
                        "marking it for chaos and discord! After " + DELAY + "s, " +
                        "enemies within " + RADIUS + " blocks are stunned for " +
                        DURATION + "s and suffer " + DAMAGE_AMT + " spellʔ damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 8, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        summonLightning(pl, false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> discord(pl), DELAY * 20L);
    }

    private void discord(Player player) {
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(verifyEnemy(player, en))) continue;
            summonLightning((LivingEntity) en, true);
            addStatusEffect(en, EffectEnum.STUN, DURATION);
            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, player, 100);
        }
    }

    private void summonLightning(LivingEntity livingEntity, boolean spawnBoltAtEntity) {
        int particleAmount;
        Location boltLoc;
        if (spawnBoltAtEntity) {
            particleAmount = 5;
            boltLoc = livingEntity.getEyeLocation();
        } else {
            particleAmount = 25;
            boltLoc = livingEntity.getTargetBlock(null, MAX_DIST).getRelative(BlockFace.UP).getLocation();
        }
        livingEntity.getWorld().spigot().strikeLightningEffect(boltLoc, true);
        livingEntity.getWorld().spawnParticle(Particle.CRIT_MAGIC, boltLoc, particleAmount, 0.3f, 0.3f, 0.3f, 0);
        livingEntity.getWorld().playSound(boltLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);
    }
}
