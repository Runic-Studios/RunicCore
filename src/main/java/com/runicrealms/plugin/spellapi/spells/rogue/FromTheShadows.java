package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FromTheShadows extends Spell {
    private final int DURATION = 1; //in seconds
    private final Set<UUID> buffedPlayers = new HashSet<>();
    private final Set<UUID> cocooned;
    private double speedDuration;
    private double cocoonDamageIncreaseDuration;
    private double percent;
    private double percentPerDex;

    public FromTheShadows() {
        super("From The Shadows", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("The next spell you cast after you cast &aUnseen &7is empowered!\n" +
                "Your empowerment is removed " + DURATION + "s after reappearing.\n" +
                "&aDash &7- Gain Speed III for " + this.speedDuration + "s!\n" +
                "&aTwin Fangs &7- This spell will critically strike!\n" +
                "&aCocoon &7- Your web now roots your target (duration halved)!\n" +
                "Additionally, &aCocoon&7 increases all damage an enemy takes by (" + this.percent + " + &f" + this.percentPerDex + "x &eDEX&7)% for the next " + this.cocoonDamageIncreaseDuration + "s!"
        );
        this.cocooned = new HashSet<>();
    }

    private int cocoonDamageCalculation(double damage, @Nullable UUID caster) {
        if (caster == null) {
            return (int) damage;
        }

        int dex = RunicCore.getStatAPI().getPlayerDexterity(caster);
        return (int) (damage + ((this.percent + (this.percentPerDex * dex)) * damage));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEmpoweredSpell(PhysicalDamageEvent event) {
        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        boolean cocooned = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);
        UUID caster = cocoon.getCaster(event.getVictim().getUniqueId());

        if (cocooned && caster != null && this.hasPassive(caster, this.getName())) {
            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), caster));
        }

        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName()) || event.getSpell() == null || !this.buffedPlayers.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        buffedPlayers.remove(event.getPlayer().getUniqueId());

        if (event.getSpell() instanceof Cocoon) {
            Bukkit.getScheduler().runTask(plugin, () -> addStatusEffect(event.getVictim(), RunicStatusEffect.ROOT, cocoon.getDuration() / 2, true));
        } else if (event.getSpell() instanceof TwinFangs) {
            event.setCritical(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        boolean cocooned = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);

        if (cocooned && this.cocooned.contains(event.getVictim().getUniqueId())) {
            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), cocoon.getCaster(event.getVictim().getUniqueId())));
        } else if (!cocooned) {
            this.cocooned.remove(event.getVictim().getUniqueId());
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
                    () -> buffedPlayers.remove(event.getCaster().getUniqueId()), (long) ((unseen.getDuration() + DURATION) * 20));
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
                this.cocooned.add(target);
            }
        }
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
}

