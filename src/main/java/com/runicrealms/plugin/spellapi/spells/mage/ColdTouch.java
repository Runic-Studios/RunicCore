package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Logic for hit found in Fireball.
 */
public class ColdTouch extends Spell {

    public ColdTouch() {
        super("Cold Touch",
                "Your &aFireball &7spell is now &aFrostbolt&7, " +
                        "slowing its target!",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Fireball)) return;
        event.setCancelled(true);
        if (RunicCore.getSpellAPI().isOnCooldown(event.getCaster(), "Frostbolt")) return;
        SpellCastEvent spellCastEvent = new SpellCastEvent(event.getCaster(), RunicCore.getSpellAPI().getSpell("Frostbolt"));
        Bukkit.getPluginManager().callEvent(spellCastEvent);
        if (!spellCastEvent.isCancelled() && spellCastEvent.willExecute())
            spellCastEvent.getSpellCasted().execute(event.getCaster(), SpellItemType.ARTIFACT);
    }
}

