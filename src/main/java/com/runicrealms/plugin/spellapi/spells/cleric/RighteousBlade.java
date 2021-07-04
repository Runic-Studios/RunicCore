package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class RighteousBlade extends Spell {

    private static final int HEAL_AMOUNT = 50;
    private static final int RADIUS = 10;
    private static final double PERCENT = 10;

    public RighteousBlade() {
        super ("Righteous Blade",
                "Your melee weapon⚔ attacks have a " + (int) PERCENT + "% chance " +
                        "to heal✦ yourself and allies within " + RADIUS + " blocks " +
                        "for " + HEAL_AMOUNT + " health!",
                ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onHealingHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        if (!e.isAutoAttack()) return;
        healAllies(e.getPlayer());
    }

    private void healAllies(Player pl) {
        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;
        HealUtil.healPlayer(HEAL_AMOUNT, pl, pl, false, this);
        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyAlly(pl, en)) continue;
            HealUtil.healPlayer(HEAL_AMOUNT, (Player) en, pl, false, this);
        }
    }
}

