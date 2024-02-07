package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
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

    /**
     * Calculates damage for the cocoon effect, taking into account the dexterity of the caster.
     *
     * @param damage Base damage to be dealt.
     * @param caster UUID of the caster, can be null.
     * @return The calculated damage as an integer.
     */
    private int cocoonDamageCalculation(double damage, UUID caster) {
        // If there's no caster, return the base damage as an integer.
        if (caster == null) {
            return (int) damage;
        }

        // Retrieve the dexterity of the caster.
        int dexterity = RunicCore.getStatAPI().getPlayerDexterity(caster);

        // Calculate the additional damage based on dexterity.
        double additionalDamage = (this.percent + (this.percentPerDex * dexterity)) * damage;

        // Return the total damage as an integer.
        return (int) (damage + additionalDamage);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEmpoweredSpell(PhysicalDamageEvent event) {
//        // Check if the player has the relevant passive ability
//        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
//
//        // Get the Cocoon spell and check if it is properly registered
//        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
//        if (cocoon == null) {
//            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
//        }
//
//        // Verify it is a relevant spell and is not null
////        if (event.getSpell() == null || (not a b or c)) return;
//
//        // Check if the player is not buffed
//        if (!this.buffedPlayersMap.containsKey(event.getPlayer().getUniqueId())) {
//            return;
//        }
//
//        // Check if the victim is debuffed with Cocoon and get the caster of the cocoon
//        boolean isDebuffed = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);
//        UUID casterId = cocoon.getCaster(event.getVictim().getUniqueId());
//
//        // Apply damage calculation and visual effects if the victim is debuffed
//        if (isDebuffed && casterId != null && this.hasPassive(casterId, this.getName())) {
//            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), casterId));
//            Cone.coneEffect(event.getVictim(), Particle.REDSTONE, 1, 0, 20, Color.LIME);
//        }
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
//        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
//        if (cocoon == null) {
//            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
//        }
//
//        boolean isDebuffed = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);
//
//        if (isDebuffed && this.debuffed.contains(event.getVictim().getUniqueId())) {
//            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), cocoon.getCaster(event.getVictim().getUniqueId())));
//            Cone.coneEffect(event.getVictim(), Particle.REDSTONE, 1, 0, 20, Color.LIME);
//        } else if (!isDebuffed) {
//            this.debuffed.remove(event.getVictim().getUniqueId());
//        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpellCast(SpellCastEvent event) {
//        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
//        if (event.getSpell() instanceof Combat || event.getSpell() instanceof Potion) {
//            return;
//        }
//        // Apply buff
//        if (event.getSpell() instanceof Unseen unseen) {
//            buffedPlayersMap.put(event.getCaster().getUniqueId(), 2);
//            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
//                    () -> buffedPlayersMap.remove(event.getCaster().getUniqueId()), (long) (unseen.getCooldown() * 20));
//            return;
//        }
//
//        // Remove potential buff
//        if (!buffedPlayersMap.containsKey(event.getCaster().getUniqueId())) return;
//
//        if (event.getSpell() instanceof Dash) {
//            this.addStatusEffect(event.getCaster(), RunicStatusEffect.SPEED_III, 4, true);
//        } else if (event.getSpell() instanceof TwinFangs) {
//            buffedPlayersMap.put(event.getCaster().getUniqueId(), 2);
//        } else if (event.getSpell() instanceof Cocoon cocoon) {
//            buffedPlayersMap.put(event.getCaster().getUniqueId(), 2);
//            UUID target = cocoon.getTarget(event.getCaster().getUniqueId());
//            if (target != null) {
//                this.debuffed.add(target);
//            }
//        }
    }
}

