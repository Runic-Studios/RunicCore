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

import java.util.*;

public class FromTheShadows extends Spell {
    private final Set<UUID> buffedPlayers = new HashSet<>();
    private final Map<UUID, Cocoon> empoweredCocoons = new HashMap<>();

    public FromTheShadows() {
        super("From The Shadows", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("The next spell you cast after you cast &aUnseen &7is empowered!\n\n" +
                "&aSprint &7- You lunge forward on cast!\n" +
                "&aTwin Fangs &7- Instantly refresh this spell’s cooldown!\n" +
                "&aCocoon &7- Your web now stuns your target!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCocoon(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof Cocoon)) return;
        // todo: this cocoon should be empowered
        if (!empoweredCocoons.containsKey(event.getPlayer().getUniqueId())) return;
        if (!empoweredCocoons.get(event.getPlayer().getUniqueId()).equals(event.getSpell())) return;
        Bukkit.broadcastMessage("this cocoon was empowered");
        empoweredCocoons.remove(event.getPlayer().getUniqueId());
        Spell spell = RunicCore.getSpellAPI().getSpell("Cocoon");
        addStatusEffect(event.getVictim(), RunicStatusEffect.STUN, ((DurationSpell) spell).getDuration(), true);
        buffedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        // Apply buff
        if (event.getSpell() instanceof Unseen) {
            buffedPlayers.add(event.getCaster().getUniqueId());
            return;
        }
        // Remove buff
        if (!buffedPlayers.contains(event.getCaster().getUniqueId())) return;
        buffedPlayers.remove(event.getCaster().getUniqueId());
        Bukkit.broadcastMessage("unseen buff is active");
        if (event.getSpell() instanceof Sprint) {
            Lunge lunge = (Lunge) RunicCore.getSpellAPI().getSpell("Lunge");
            double duration = lunge.getDuration();
            double launchMultiplier = lunge.getLaunchMultiplier();
            double verticalPower = lunge.getVerticalPower();
            Lunge.lunge(event.getCaster(), duration, launchMultiplier, verticalPower);
        } else if (event.getSpell() instanceof TwinFangs) {
            Spell spell = RunicCore.getSpellAPI().getSpell("Twin Fangs");
            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                    () -> RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), spell,
                            spell.getCooldown()));
        } else if (event.getSpell() instanceof Cocoon cocoon) {
            empoweredCocoons.put(event.getCaster().getUniqueId(), cocoon);
//            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
//                    () -> buffedPlayers.remove(event.getCaster().getUniqueId()));
            // todo: make cocoon buffed
        }
    }


}

