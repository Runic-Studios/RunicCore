package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.Random;

public class Manawell extends Spell {

    private static final int PERCENT = 7;

    public Manawell() {
        super ("Manawell",
                "Spending mana has a " + (int) PERCENT + "% chance" +
                        "\nto heal allies for the amount spent!",
                ChatColor.WHITE, ClassEnum.RUNIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent e) {

        if (getRunicPassive(e.getCaster()) == null) return;
        if (!getRunicPassive(e.getCaster()).equals(this)) return;

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        if (RunicCore.getPartyManager().getPlayerParty(e.getCaster()) == null) return;
        List<Player> allies = RunicCore.getPartyManager().getPlayerParty(e.getCaster()).getPlayerMembers();

        for (Player ally : allies) {
            if (verifyAlly(e.getCaster(), ally)) {
                e.getCaster().getWorld().playSound(e.getCaster().getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
                e.getCaster().playSound(e.getCaster().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
                ally.getWorld().playSound(ally.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.25f, 2f);
                ally.getWorld().spawnParticle(Particle.SPELL_WITCH, ally.getEyeLocation(), 3, 0.3F, 0.3F, 0.3F, 0);
                HealUtil.healPlayer(e.getSpell().getManaCost(), ally, e.getCaster(), false, false, false);
            }
        }
    }
}

