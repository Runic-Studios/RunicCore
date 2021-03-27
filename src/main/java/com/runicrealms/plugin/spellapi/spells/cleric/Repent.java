package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings("FieldCanBeLocal")
public class Repent extends Spell {

    private static final int DAMAGE_AMOUNT = 25;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 4;
    private static final int STUN_DURATION = 2;

    public Repent() {
        super ("Repent",
                "You erupt a powerful blast of holy magic at " +
                        "your target location that deals " + DAMAGE_AMOUNT + " spellʔ " +
                        "damage to enemies within " + RADIUS + " blocks and " +
                        "stuns them for " + STUN_DURATION + "s!",
                ChatColor.WHITE, ClassEnum.CLERIC, 12, 15);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location blastLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
        pl.getWorld().playSound(blastLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, blastLoc, 25, 0.3f, 0.3f, 0.3f, 0);

        for (Entity en : pl.getWorld().getNearbyEntities(blastLoc, RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(pl, en)) continue;
            LivingEntity le = (LivingEntity) en;
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, le, pl, 100);
            en.getWorld().spawnParticle(Particle.FLAME, le.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
            addStatusEffect(en, EffectEnum.SILENCE, STUN_DURATION);
            addStatusEffect(en, EffectEnum.STUN, STUN_DURATION);
        }
    }
}

