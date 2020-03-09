package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.ManaRegenEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

public class Absolution extends Spell {

    public Absolution() {
        super ("Absolution",
                "Your mana regeneration is doubled!",
                ChatColor.WHITE, ClassEnum.RUNIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onManaRegen(ManaRegenEvent e) {
        if (getRunicPassive(e.getPlayer()) == null) return;
        if (!getRunicPassive(e.getPlayer()).equals(this)) return;
        e.setAmount(2*e.getAmount());
    }
}

