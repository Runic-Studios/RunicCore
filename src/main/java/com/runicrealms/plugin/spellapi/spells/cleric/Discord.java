package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
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
        Location boltLoc;
        if (spawnBoltAtEntity)
            boltLoc = livingEntity.getEyeLocation();
        else
            boltLoc = livingEntity.getTargetBlock(null, MAX_DIST).getLocation();
        //livingEntity.getWorld().spigot().strikeLightning(boltLoc);
        boltLoc.getWorld().strikeLightning(boltLoc);
        // livingEntity.getWorld().spigot().strikeLightningEffect(boltLoc, true);
        //livingEntity.getWorld().strikeLightning(boltLoc, false);
        livingEntity.getWorld().playSound(boltLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);
    }
}
