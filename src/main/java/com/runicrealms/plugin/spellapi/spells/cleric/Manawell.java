package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;
import java.util.Set;

public class Manawell extends Spell {

    private static final double PERCENT = 15;
    private static final double RADIUS = 100;

    public Manawell() {
        super("Manawell",
                "Spending mana has a " + (int) PERCENT + "% chance " +
                        "to refund the cost to you and your allies!",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        event.getCaster().getWorld().playSound(event.getCaster().getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
        event.getCaster().playSound(event.getCaster().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
        if (RunicCore.getPartyAPI().getParty(event.getCaster().getUniqueId()) == null) {
            restoreMana(event.getCaster(), event.getSpell().getManaCost());
            return;
        }

        Set<Player> allies = RunicCore.getPartyAPI().getParty(event.getCaster().getUniqueId()).getMembersWithLeader();
        for (Player ally : allies) {
            if (isValidAlly(event.getCaster(), ally)) {
                if (!event.getCaster().getWorld().equals(ally.getWorld())) continue;
                if (event.getCaster().getLocation().distanceSquared(ally.getLocation()) > RADIUS * RADIUS) continue;
                restoreMana(ally, event.getSpell().getManaCost());
            }
        }
    }

    private void restoreMana(Player player, int amountOfManaToRestore) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
        player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getEyeLocation(), 3, 0.3F, 0.3F, 0.3F, 0);
        RunicCore.getRegenManager().addMana(player, amountOfManaToRestore);
    }
}

