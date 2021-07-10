package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;

@SuppressWarnings("FieldCanBeLocal")
public class SoulLink extends Spell {

    private static final int RADIUS = 50;
    private final HashSet<Player> affectedAllies;

    public SoulLink() {
        super("Soul Link",
                "You bind the souls of all allies within " +
                        RADIUS + " blocks! The link balances the health " +
                        "of yourself and all affected allies, averaging " +
                        "the current health of all allies and setting the " +
                        "current health of each to the average!",
                ChatColor.WHITE, ClassEnum.CLERIC, 60, 40);
        affectedAllies = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.25f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 2.0f);
        affectedAllies.add(pl);
        double totalCurrentHealth = pl.getHealth();
        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyAlly(pl, en)) continue;
            Player ally = (Player) en;
            affectedAllies.add(ally);
            double allyCurrentHealth = ally.getHealth();
            totalCurrentHealth += allyCurrentHealth;
        }
        double averageCurrentHealth = totalCurrentHealth / affectedAllies.size();
        for (Player ally : affectedAllies) {
            double maxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            Cone.coneEffect(ally, Particle.VILLAGER_HAPPY,1, 0, 20, Color.GREEN);
            ally.setHealth(Math.min(averageCurrentHealth, maxHealth));
        }
    }
}
