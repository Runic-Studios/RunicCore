package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SuppressWarnings("FieldCanBeLocal")
public class Cleave extends Spell {

    private static final int DAMAGE_AMT = 10;
    private static final int RADIUS = 4;

    public Cleave() {
        super ("Cleave",
                "You cleave all enemies within" +
                        "\n" + RADIUS + " blocks for " + DAMAGE_AMT + " weapon⚔" +
                        "\ndamage!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 10, 10);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
        pl.getWorld().spawnParticle(Particle.CRIT, pl.getEyeLocation(), 15, 0.75F, 0.5F, 0.75F, 0);

        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (verifyEnemy(pl, en)) {
                DamageUtil.damageEntityWeapon(DAMAGE_AMT, (LivingEntity) en, pl, false, true);
            }
        }
    }
}

