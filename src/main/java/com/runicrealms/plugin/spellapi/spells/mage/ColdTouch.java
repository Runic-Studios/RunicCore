package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

/**
 * Logic for hit found in Fireball.
 */
public class ColdTouch extends Spell {

    public ColdTouch() {
        super ("Cold Touch",
                "Your &aFireball &7spell is now &aFrostbolt&7, " +
                        "slowing its target!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent e) {
        if (!hasPassive(e.getCaster(), this.getName())) return;
        if (!(e.getSpell() instanceof Fireball)) return;
        e.setCancelled(true);
        SpellCastEvent spellCastEvent = new SpellCastEvent(e.getCaster(), RunicCore.getSpellManager().getSpellByName("Frostbolt"));
        Bukkit.getPluginManager().callEvent(spellCastEvent);
        if (!spellCastEvent.isCancelled() && spellCastEvent.willExecute())
            spellCastEvent.getSpellCasted().execute(e.getCaster(), SpellItemType.ARTIFACT);
    }
}

