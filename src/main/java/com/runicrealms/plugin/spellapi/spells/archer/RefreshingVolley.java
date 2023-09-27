package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class RefreshingVolley extends Spell implements HealingSpell, DistanceSpell {
    private double heal;
    private double healingPerLevel;
    private double distance;

    public RefreshingVolley() {
        super("Refreshing Volley", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("While &aRapid Fire&7 is active, each time you land a ranged attack on an enemy" +
                "heal✦ yourself and your closest 2 allies within " + this.distance + " blocks " +
                "for (" + this.heal + " + &f" + this.healingPerLevel + "x&7 lvl) health.");
    }

    @Override
    public double getHeal() {
        return this.heal;
    }

    @Override
    public void setHeal(double heal) {
        this.heal = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return this.healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) // late
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isRanged() || !this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        if (!(RunicCore.getSpellAPI().getSpell("Rapid Fire") instanceof RapidFire rapidFire) || !rapidFire.isUsing(event.getPlayer())) {
            return;
        }

        this.healPlayer(event.getPlayer(), event.getPlayer(), this.heal, this);

        Pair<Player, Double> one = null;
        Pair<Player, Double> two = null;

        for (Entity entity : event.getPlayer().getWorld().getNearbyEntities(event.getPlayer().getLocation(), this.distance, this.distance, this.distance, entity -> !entity.getUniqueId().equals(event.getPlayer().getUniqueId()))) {
            if (!(entity instanceof Player member) || !this.isValidAlly(event.getPlayer(), member)) {
                continue;
            }

            if (one == null) {
                one = new Pair<>(member, distance);
                continue;
            }

            if (two == null) {
                two = new Pair<>(member, distance);
                continue;
            }

            //only executes if both one and two are not null
            double distanceOne = one.second;
            double distanceTwo = two.second;

            if (distanceOne > distanceTwo) {
                one = new Pair<>(member, distance);
            } else if (distanceTwo > distanceOne) {
                two = new Pair<>(member, distance);
            }
        }

        if (one != null) {
            this.healPlayer(event.getPlayer(), one.first, this.heal, this);
        }

        if (two != null) {
            this.healPlayer(event.getPlayer(), two.first, this.heal, this);
        }
    }
}

