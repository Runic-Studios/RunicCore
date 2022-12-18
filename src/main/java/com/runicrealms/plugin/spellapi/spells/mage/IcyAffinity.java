package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class IcyAffinity extends Spell {

    private static final double PERCENT = .25;

    public IcyAffinity() {
        super("Icy Affinity",
                "Your &aIceblock &7spell now restores✸ " +
                        (int) (PERCENT * 100) + "% of your health!",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH) // toward the end
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof IceBlock)) return;
        Player player = event.getCaster();
        HealUtil.healPlayer((int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT),
                player, player, false, this);
    }
}

