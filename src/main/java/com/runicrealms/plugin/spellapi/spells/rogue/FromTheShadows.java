package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FromTheShadows extends Spell {
    private final Set<UUID> buffedPlayers = new HashSet<>();
    private final Set<UUID> debuffed;
    private double speedDuration;
    private double cocoonDamageIncreaseDuration;
    private double percent;
    private double percentPerDex;

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
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;

        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        boolean isDebuffed = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);
        UUID caster = cocoon.getCaster(event.getVictim().getUniqueId());

        if (isDebuffed && caster != null && this.hasPassive(caster, this.getName())) {
            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), caster));
            Cone.coneEffect(event.getVictim(), Particle.REDSTONE, 1, 0, 20, Color.LIME);
        }

        if (event.getSpell() == null || !this.buffedPlayers.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        buffedPlayers.remove(event.getPlayer().getUniqueId());

        if (event.getSpell() instanceof TwinFangs) {
            // TODO: needs to work for both strikes
            event.setCritical(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        boolean isDebuffed = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);

        if (isDebuffed && this.debuffed.contains(event.getVictim().getUniqueId())) {
            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), cocoon.getCaster(event.getVictim().getUniqueId())));
            Cone.coneEffect(event.getVictim(), Particle.REDSTONE, 1, 0, 20, Color.LIME);
        } else if (!isDebuffed) {
            this.debuffed.remove(event.getVictim().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpellCast(SpellCastEvent event) {
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (event.getSpell() instanceof Combat || event.getSpell() instanceof Potion) {
            return;
        }
        // Apply buff
        if (event.getSpell() instanceof Unseen unseen) {
            buffedPlayers.add(event.getCaster().getUniqueId());
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                    () -> buffedPlayers.remove(event.getCaster().getUniqueId()), (long) (unseen.getCooldown() * 20));
            return;
        }

        // Remove potential buff
        if (!buffedPlayers.contains(event.getCaster().getUniqueId())) return;

        if (event.getSpell() instanceof Dash) {
            this.addStatusEffect(event.getCaster(), RunicStatusEffect.SPEED_III, 4, true);
        } else if (event.getSpell() instanceof TwinFangs) {
            buffedPlayers.add(event.getCaster().getUniqueId());
        } else if (event.getSpell() instanceof Cocoon cocoon) {
            buffedPlayers.add(event.getCaster().getUniqueId());
            UUID target = cocoon.getTarget(event.getCaster().getUniqueId());
            if (target != null) {
                this.debuffed.add(target);
            }
        }
    }
}

