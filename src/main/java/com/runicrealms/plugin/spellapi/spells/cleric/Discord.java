package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Discord extends Spell implements MagicDamageSpell {

    private static final int DELAY = 2;
    private static final int DAMAGE_AMT = 20;
    private static final double DAMAGE_PER_LEVEL = 2.5;
    private static final int DURATION = 3;
    private static final int RADIUS = 8;

    public Discord() {
        super("Discord",
                "You prime yourself with chaotic magic, slowing yourself for " + DELAY + "s. " +
                        "After, enemies within " + RADIUS + " blocks are stunned for " +
                        DURATION + "s and suffer (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 20, 20);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DELAY * 20, 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.NOTE, DELAY, 0, 20, Color.GREEN);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> discord(player), DELAY * 20L);
    }

    private void discord(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, player.getEyeLocation(), 50, 1.0F, 0.5F, 1.0F, 0);
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(isValidEnemy(player, en))) continue;
            causeDiscord(player, (LivingEntity) en);
            addStatusEffect(en, EffectEnum.STUN, DURATION);
            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, player, this);
        }
    }

    private void causeDiscord(Player caster, LivingEntity victim) {
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
        VectorUtil.drawLine(caster, Particle.CRIT_MAGIC, Color.WHITE, caster.getEyeLocation(), victim.getEyeLocation(), 1);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
