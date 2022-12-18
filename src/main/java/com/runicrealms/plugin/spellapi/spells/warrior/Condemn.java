package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

public class Condemn extends Spell {

    private static final double PERCENT = .25;

    public Condemn() {
        super("Condemn",
                "You deal an additional " + (int) (PERCENT * 100) + "% damage " +
                        "to silenced enemies!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSilencingHit(MagicDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!RunicCore.getSpellAPI().isSilenced(event.getVictim())) return;
        // particle
        event.setAmount((int) (event.getAmount() + (event.getAmount() * PERCENT)));
    }

    @EventHandler
    public void onSilencingHit(PhysicalDamageEvent event) {
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!RunicCore.getSpellAPI().isSilenced(event.getVictim())) return;
        // particle
        event.setAmount((int) (event.getAmount() + (event.getAmount() * PERCENT)));
    }
}

