package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class IcyAffinity extends Spell {

    private static final double PERCENT = .35;

    public IcyAffinity() {
        super ("Icy Affinity",
                "Your &aIceblock &7spell now restores✦ " +
                        (int) (PERCENT * 100) + "% of your health!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent e) {
        if (!hasPassive(e.getCaster(), this.getName())) return;
        if (!(e.getSpell() instanceof IceBlock)) return;
        Player pl = e.getCaster();
        HealUtil.healPlayer((int) (pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT),
                pl, pl, false, this);
    }
}

