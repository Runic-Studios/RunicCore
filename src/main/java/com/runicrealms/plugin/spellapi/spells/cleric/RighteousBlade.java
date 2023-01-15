package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class RighteousBlade extends Spell implements HealingSpell {

    private static final int HEAL_AMOUNT = 50;
    private static final double HEALING_PER_LEVEL = 2.5;
    private static final int RADIUS = 10;
    private static final double PERCENT = 10;

    public RighteousBlade() {
        super("Righteous Blade",
                "Your basic attacks have a " + (int) PERCENT + "% chance " +
                        "to heal✦ yourself and allies within " + RADIUS + " blocks " +
                        "for (" + HEAL_AMOUNT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) health!",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public int getHeal() {
        return HEAL_AMOUNT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    private void healAllies(Player player) {
        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;
        HealUtil.healPlayer(HEAL_AMOUNT, player, player, false, this);
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidAlly(player, en)) continue;
            HealUtil.healPlayer(HEAL_AMOUNT, (Player) en, player, false, this);
        }
    }

    @EventHandler
    public void onHealingHit(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!event.isBasicAttack()) return;
        healAllies(event.getPlayer());
    }
}

