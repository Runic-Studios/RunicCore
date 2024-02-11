package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
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
    private final Map<UUID, Integer> buffedPlayersMap = new HashMap<>(); // used so both Twin Fangs get buff
    private final Set<UUID> debuffed;
    private double speedDuration;
    private double cocoonDamageIncreaseDuration;
    private double percent;
    private double percentPerDex;

    // TODO: Honestly needs a logic re-write for the empowered spells. This is clunky and hard to read
    public FromTheShadows() {
        super("From The Shadows", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("The next spell you cast after you cast &aUnseen &7is empowered " +
                "until you are able to cast &aUnseen&7 again. " +
                "\n\n&aDash &7- You gain Speed III for " + this.speedDuration + "s!" +
                "\n&aTwin Fangs &7- This spell will critically strike!" +
                "\n&aCocoon &7- Increases all damage your target takes by (" +
                this.percent + " + &f" + this.percentPerDex + "x &eDEX&7)% for the next " + this.cocoonDamageIncreaseDuration + "s!"
        );
        this.debuffed = new HashSet<>();
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number speedDuration = (Number) spellData.getOrDefault("speed-duration", 4);
        this.speedDuration = speedDuration.doubleValue();
        Number cocoonDamageIncreaseDuration = (Number) spellData.getOrDefault("cocoon-damage-increase-duration", 3);
        this.cocoonDamageIncreaseDuration = cocoonDamageIncreaseDuration.doubleValue();
        Number percent = (Number) spellData.getOrDefault("percent", .02);
        this.percent = percent.doubleValue();
        Number percentPerDex = (Number) spellData.getOrDefault("percent-per-dex", .1);
        this.percentPerDex = percentPerDex.doubleValue();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEmpoweredSpell(PhysicalDamageEvent event) {
//        // Check if the player has the relevant passive ability
//        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
//
//        // If the spell is Twin Fangs, mark the event as critical and only reduce 1 stack (so that both fangs get buff)
//        if (event.getSpell() instanceof TwinFangs) {
//            buffedPlayersMap.put(casterId, buffedPlayersMap.get(casterId) - 1);
//            event.setCritical(true);
//        } else { // Must be Cocoon or Dash
//            buffedPlayersMap.put(casterId, buffedPlayersMap.get(casterId) - 2);
//        }
//
//        // Reduce a buff stack (or remove buff) for the player.
//        if (buffedPlayersMap.get(casterId) == 0) {
//            buffedPlayersMap.remove(casterId);
//        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        if (this.hasSpellEffect(event.getPlayer().getUniqueId(), SpellEffectType.SHROUDED)) {
            Bukkit.broadcastMessage("buffed cocoon");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpellCast(SpellCastEvent event) {
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!hasSpellEffect(event.getCaster().getUniqueId(), SpellEffectType.SHROUDED)) return;
        if (event.getSpell() instanceof Combat || event.getSpell() instanceof Potion) {
            return;
        }

        if (event.getSpell() instanceof Dash) {
            this.removeStatusEffect(event.getCaster(), RunicStatusEffect.SLOW_I);
            this.removeStatusEffect(event.getCaster(), RunicStatusEffect.SLOW_II);
            this.removeStatusEffect(event.getCaster(), RunicStatusEffect.SLOW_III);
            this.addStatusEffect(event.getCaster(), RunicStatusEffect.SPEED_III, 4, true);
        } else if (event.getSpell() instanceof TwinFangs) {
            // TODO: fix
            buffedPlayersMap.put(event.getCaster().getUniqueId(), 2);
        }
    }
}

