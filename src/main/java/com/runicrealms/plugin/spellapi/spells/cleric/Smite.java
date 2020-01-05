package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Smite extends Spell {

    private static final int DAMAGE_AMT = 15;
    private static final float RADIUS = 5f;

    public Smite() {
        super("Smite",
                "You smite all enemies within" +
                        "\n" + (int) RADIUS + " blocks, dealing " + DAMAGE_AMT + " spellʔ" +
                        "\ndamage to them.",
                ChatColor.WHITE, 8, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        pl.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, pl.getEyeLocation(), 15, 0.75F, 0.5F, 0.75F, 0);

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

            // skip non-living, armor stands
            if (!(en instanceof LivingEntity)) continue;
            if (en instanceof ArmorStand) continue;

            LivingEntity le = (LivingEntity) en;

            // heal party members and the caster
            DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, false);
        }
    }
}
