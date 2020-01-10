package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
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
                ChatColor.WHITE, 10, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
        pl.getWorld().spawnParticle(Particle.CRIT, pl.getEyeLocation(), 15, 0.75F, 0.5F, 0.75F, 0);

        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

            // skip non-living, armor stands
            if (!(en instanceof LivingEntity)) continue;
            if (en instanceof ArmorStand) continue;

            LivingEntity le = (LivingEntity) en;

            // heal party members and the caster
            DamageUtil.damageEntityWeapon(DAMAGE_AMT, le, pl, false, true);
        }
    }
}

