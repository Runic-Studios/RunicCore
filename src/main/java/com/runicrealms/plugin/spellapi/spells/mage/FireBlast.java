package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
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
public class FireBlast extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMOUNT = 15;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 4;
    private static final int STUN_DURATION = 2;

    public FireBlast() {
        super("Fire Blast",
                "You erupt a powerful blast of fire at " +
                        "your target location that deals (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies within " + RADIUS + " blocks and " +
                        "stuns them for " + STUN_DURATION + "s!",
                ChatColor.WHITE, CharacterClass.MAGE, 12, 30);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location blastLoc = player.getTargetBlock(null, MAX_DIST).getLocation();
        player.getWorld().playSound(blastLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.LAVA, blastLoc, 25, 0.3f, 0.3f, 0.3f, 0);

        for (Entity en : player.getWorld().getNearbyEntities(blastLoc, RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(player, en)) continue;
            LivingEntity le = (LivingEntity) en;
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, le, player, this);
            en.getWorld().spawnParticle(Particle.FLAME, le.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
            addStatusEffect(en, RunicStatusEffect.SILENCE, STUN_DURATION, true);
            addStatusEffect(en, RunicStatusEffect.STUN, STUN_DURATION, true);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

