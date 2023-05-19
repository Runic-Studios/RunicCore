package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FromTheShadows extends Spell {
    private final Set<UUID> potentialBuffedPlayers = new HashSet<>();
    private final Map<UUID, Spell> actuallyBuffedPlayers = new HashMap<>();

    public FromTheShadows() {
        super("From The Shadows", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("""
                The next spell you cast after you cast &aUnseen &7is empowered!
                Your empowerment is removed 1s after reappearing.

                &aSprint &7- You lunge forward on cast!
                &aTwin Fangs &7- This spell will critically strike!
                &aCocoon &7- Your web now stuns your target (duration halved)!
                """);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEmpoweredSpell(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!actuallyBuffedPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.getSpell() instanceof Cocoon) {
            actuallyBuffedPlayers.remove(event.getPlayer().getUniqueId());
            Spell spell = RunicCore.getSpellAPI().getSpell("Cocoon");
            addStatusEffect(event.getVictim(), RunicStatusEffect.STUN, ((DurationSpell) spell).getDuration() / 2, true);
            potentialBuffedPlayers.remove(event.getPlayer().getUniqueId());
        } else if (event.getSpell() instanceof TwinFangs) {
            event.setCritical(true);
        }
        actuallyBuffedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        // Apply buff
        if (event.getSpell() instanceof Unseen) {
            potentialBuffedPlayers.add(event.getCaster().getUniqueId());
            double duration = ((DurationSpell) RunicCore.getSpellAPI().getSpell("Unseen")).getDuration();
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                    () -> potentialBuffedPlayers.remove(event.getCaster().getUniqueId()), (long) ((duration + 1) * 20L));
            return;
        }
        // Remove actual buff
        if (actuallyBuffedPlayers.containsKey(event.getCaster().getUniqueId())) {
            actuallyBuffedPlayers.remove(event.getCaster().getUniqueId());
            return;
        }
        // Remove potential buff
        if (!potentialBuffedPlayers.contains(event.getCaster().getUniqueId())) return;
        potentialBuffedPlayers.remove(event.getCaster().getUniqueId());
        if (event.getSpell() instanceof Sprint) {
            Lunge lunge = (Lunge) RunicCore.getSpellAPI().getSpell("Lunge");
            double duration = lunge.getDuration();
            double launchMultiplier = lunge.getLaunchMultiplier();
            double verticalPower = lunge.getVerticalPower();
            Lunge.lunge(event.getCaster(), duration, launchMultiplier, verticalPower);
        } else if (event.getSpell() instanceof TwinFangs twinFangs) {
            actuallyBuffedPlayers.put(event.getCaster().getUniqueId(), twinFangs);
        } else if (event.getSpell() instanceof Cocoon cocoon) {
            actuallyBuffedPlayers.put(event.getCaster().getUniqueId(), cocoon);
        }
    }

}

