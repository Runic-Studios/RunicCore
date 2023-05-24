package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
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
        super("Righteous Blade", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Your basic attacks have a " + (int) PERCENT + "% chance " +
                "to heal✦ yourself and allies within " + RADIUS + " blocks " +
                "for (" + HEAL_AMOUNT + " + &f" + HEALING_PER_LEVEL +
                "x&7 lvl) health!");
    }

    @Override
    public double getHeal() {
        return HEAL_AMOUNT;
    }

    @Override
    public void setHeal(double heal) {

    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {

    }

    private void healAllies(Player player) {
        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;
        healPlayer(player, player, HEAL_AMOUNT, this);
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidAlly(player, en)) continue;
            healPlayer(player, (Player) en, HEAL_AMOUNT, this);
        }
    }

    @EventHandler
    public void onHealingHit(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!event.isBasicAttack()) return;
        healAllies(event.getPlayer());
    }
}

