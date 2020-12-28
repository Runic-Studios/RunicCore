package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlazingSpeed extends Spell {

    public BlazingSpeed() {
        super ("Blazing Speed",
                "While your Fire Aura is active, " +
                        "you gain a boost of speed!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent e) {
        if (!hasPassive(e.getCaster(), this.getName())) return;
        if (!(e.getSpell() instanceof FireAura)) return;
        e.getCaster().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, FireAura.getDuration() * 20, 1));
    }
}

