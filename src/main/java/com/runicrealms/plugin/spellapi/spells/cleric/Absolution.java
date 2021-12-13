package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Absolution extends Spell {

    public Absolution() {
        super("Absolution",
                "Your &aRejuvenate &7spell is now &aPurify &7and removes silences!",
                ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH) // fires LAST, but before use listener 
    public void onSpellCast(SpellCastEvent e) {
        if (!hasPassive(e.getCaster(), this.getName())) return;
        if (!(e.getSpell() instanceof Rejuvenate)) return;
        e.setCancelled(true);
        SpellCastEvent spellCastEvent = new SpellCastEvent(e.getCaster(), RunicCore.getSpellManager().getSpellByName("Purify"));
        Bukkit.getPluginManager().callEvent(spellCastEvent);
        if (!spellCastEvent.isCancelled() && spellCastEvent.willExecute())
            spellCastEvent.getSpellCasted().execute(e.getCaster(), SpellItemType.ARTIFACT);
    }
}

