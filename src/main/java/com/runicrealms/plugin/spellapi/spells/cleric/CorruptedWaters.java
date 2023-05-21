package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class CorruptedWaters extends Spell {

    public CorruptedWaters(DefiledFont defiledFont) {
        super("Corrupted Waters", CharacterClass.CLERIC);
        this.setIsPassive(true);
        double damage = ((MagicDamageSpell) defiledFont).getMagicDamage();
        double damagePerLevel = ((MagicDamageSpell) defiledFont).getMagicDamagePerLevel();
        double duration = ((DurationSpell) defiledFont).getDuration();
        double healingReduction = defiledFont.getHealingReduction();
        this.setDescription("Your &aSacred Spring &7spell is now &aDefiled Font&7. " +
                "Defiled Font no longer heals your allies, " +
                "and instead deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage over " + duration + "s " +
                "to all affected enemies. Additionally, " +
                "enemies suffer " + (healingReduction * 100) + "% reduced healing from all " +
                "sources for the duration!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof SacredSpring)) return;
        event.setCancelled(true);
        if (RunicCore.getSpellAPI().isOnCooldown(event.getCaster(), "Defiled Font")) return;
        SpellCastEvent spellCastEvent = new SpellCastEvent(event.getCaster(), RunicCore.getSpellAPI().getSpell("Defiled Font"));
        Bukkit.getPluginManager().callEvent(spellCastEvent);
        if (!spellCastEvent.isCancelled() && spellCastEvent.willExecute())
            spellCastEvent.getSpellCasted().execute(event.getCaster(), SpellItemType.ARTIFACT);
    }
}

