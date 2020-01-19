package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.ManaRegenEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.*;
import org.bukkit.event.EventHandler;

public class Absolution extends Spell {

    private static final int AMOUNT = RunicCore.getManaManager().getManaRegenAmt();

    public Absolution() {
        super ("Absolution",
                "Your mana regeneration is doubled!",
                ChatColor.WHITE, 10, 15);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onManaRegen(ManaRegenEvent e) {
        if (getRunicPassive(e.getPlayer()) == null) return;
        if (!getRunicPassive(e.getPlayer()).equals(this)) return;
        RunicCore.getManaManager().addMana(e.getPlayer(), AMOUNT, false);
    }
}

