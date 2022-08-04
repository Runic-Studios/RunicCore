package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Condemn extends Spell {

    private static final double PERCENT = .25;

    public Condemn() {
        super("Condemn",
                "You deal an additional " + (int) (PERCENT * 100) + "% damage " +
                        "to silenced enemies!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSilencingHit(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        if (!RunicCoreAPI.isSilenced(e.getVictim())) return;
        // particle
        e.setAmount((int) (e.getAmount() + (e.getAmount() * PERCENT)));
    }

    @EventHandler
    public void onSilencingHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        if (!RunicCoreAPI.isSilenced(e.getVictim())) return;
        // particle
        e.setAmount((int) (e.getAmount() + (e.getAmount() * PERCENT)));
    }
}

