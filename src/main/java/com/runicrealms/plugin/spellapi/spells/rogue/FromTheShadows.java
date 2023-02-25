package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FromTheShadows extends Spell {
    private static final Set<UUID> buffedPlayers = new HashSet<>();

    public FromTheShadows() {
        super("From the Shadows",
                "The next spell you cast after you cast &aUnseen &7is empowered!\n\n" +
                        "&aSprint &7- You lunge forward on cast!\n" +
                        "&aTwin Fangs &7- Instantly refresh this spell’s cooldown!\n" +
                        "&aCocoon &7- Your web now roots your target!",
                ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCocoon(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof Cocoon)) return;
        if (!buffedPlayers.contains(event.getPlayer().getUniqueId())) return;
        addStatusEffect(event.getVictim(), RunicStatusEffect.ROOT, Cocoon.DURATION, true);
        buffedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (event.getSpell() instanceof Unseen) {
            buffedPlayers.add(event.getCaster().getUniqueId());
        } else if (event.getSpell() instanceof Sprint) {
            if (!buffedPlayers.contains(event.getCaster().getUniqueId())) return;
            Lunge.lunge(event.getCaster());
            buffedPlayers.remove(event.getCaster().getUniqueId());
        } else if (event.getSpell() instanceof TwinFangs) {
            if (!buffedPlayers.contains(event.getCaster().getUniqueId())) return;
            Spell spell = RunicCore.getSpellAPI().getSpell("Twin Fangs");
            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                    () -> RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), spell,
                            spell.getCooldown()));
            buffedPlayers.remove(event.getCaster().getUniqueId());
        }
    }


}

