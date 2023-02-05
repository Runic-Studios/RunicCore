package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class CorruptedWaters extends Spell {
//    private static final int DAMAGE = 30;
//    private static final int DURATION = 4;
//    private static final double HEALING_REDUCTION = 0.5D;
//    private static final double DAMAGE_PER_LEVEL = 1.0D;

    public CorruptedWaters() {
        super("Corrupted Waters",
                "Your &aHoly Water &7spell is now &aUnholy Water&7. " +
                        "Unholy Water no longer heals your allies, " +
                        "and instead deals (" + UnholyWater.DAMAGE + " + &f" + UnholyWater.DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage over " + UnholyWater.DURATION + "s " +
                        "to all affected enemies. Additionally, " +
                        "enemies suffer " + (int) (UnholyWater.HEALING_REDUCTION * 100) + "% reduced healing from all " +
                        "sources for the duration!",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof HolyWater)) return;
        event.setCancelled(true);
        if (RunicCore.getSpellAPI().isOnCooldown(event.getCaster(), "Unholy Water")) return;
        SpellCastEvent spellCastEvent = new SpellCastEvent(event.getCaster(), RunicCore.getSpellAPI().getSpell("Unholy Water"));
        Bukkit.getPluginManager().callEvent(spellCastEvent);
        if (!spellCastEvent.isCancelled() && spellCastEvent.willExecute())
            spellCastEvent.getSpellCasted().execute(event.getCaster(), SpellItemType.ARTIFACT);
    }
}

