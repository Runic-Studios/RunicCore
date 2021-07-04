package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Discord extends Spell {

    private static final int DELAY = 2;
    private static final int DAMAGE_AMT = 8;
    private static final int DURATION = 3;
    private static final int RADIUS = 8;

    public Discord() {
        super("Discord",
                "You prime yourself with a chaotic magic! After " + DELAY + "s, " +
                        "enemies within " + RADIUS + " blocks are stunned for " +
                        DURATION + "s and suffer " + DAMAGE_AMT + " spellʔ damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 20, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        Cone.coneEffect(pl, Particle.NOTE, DELAY, 0, 20, Color.WHITE);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> discord(pl), DELAY * 20L);
    }

    private void discord(Player player) {
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(verifyEnemy(player, en))) continue;
            causeDiscord(player, (LivingEntity) en);
            addStatusEffect(en, EffectEnum.STUN, DURATION);
            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, player, this);
        }
    }

    private void causeDiscord(Player caster, LivingEntity victim) {
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
        VectorUtil.drawLine(caster, Particle.CRIT_MAGIC, Color.WHITE, caster.getEyeLocation(), victim.getEyeLocation(), 1);
    }
}
