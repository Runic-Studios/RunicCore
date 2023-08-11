package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FromTheShadows extends Spell {
    private final Set<UUID> potentialBuffedPlayers = new HashSet<>();
    private final Map<UUID, Spell> actuallyBuffedPlayers = new HashMap<>();
    private final Set<UUID> cocoon;
    private double speedDuration;
    private double cocoonDamageIncreaseDuration;
    private double damage;
    private double damagePerDex;

    public FromTheShadows() {
        super("From The Shadows", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("""
                The next spell you cast after you cast &aUnseen &7is empowered!
                Your empowerment is removed 1s after reappearing.
                """ +
                "&aDash &7- Gain Speed III for " + this.speedDuration + "s!\n" +
                "&aTwin Fangs &7- This spell will critically strike!\n" +
                "&aCocoon &7- Your web now roots your target (duration halved)!\n" +
                "Additionally, &aCocoon&7 increases all damage an enemy takes by 2 + (0.1 x DEX)% for the next " + this.cocoonDamageIncreaseDuration + "s!"
        );
        this.cocoon = new HashSet<>();
    }

    private int cocoonDamageCalculation(double damage, @NotNull UUID caster) {
        int dex = RunicCore.getStatAPI().getPlayerDexterity(caster);
        return (int) (damage + 2 + (.1 * damage * dex));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEmpoweredSpell(PhysicalDamageEvent event) {
        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        boolean cocooned = cocoon.isCocooned(event.getVictim().getUniqueId(), this.cocoonDamageIncreaseDuration);

        if (cocooned && this.cocoon.contains(event.getVictim().getUniqueId())) {
            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), cocoon.getCaster(event.getVictim().getUniqueId())));
        } else if (!cocooned) {
            this.cocoon.remove(event.getVictim().getUniqueId());
        }

        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName()) || event.getSpell() == null || !this.actuallyBuffedPlayers.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        actuallyBuffedPlayers.remove(event.getPlayer().getUniqueId());

        if (event.getSpell() instanceof Cocoon) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                addStatusEffect(event.getVictim(), RunicStatusEffect.ROOT, cocoon.getDuration() / 2, true);
                potentialBuffedPlayers.remove(event.getPlayer().getUniqueId());
            });
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

        if (cocooned && this.cocoon.contains(event.getVictim().getUniqueId())) {
            event.setAmount(this.cocoonDamageCalculation(event.getAmount(), cocoon.getCaster(event.getVictim().getUniqueId())));
        } else if (!cocooned) {
            this.cocoon.remove(event.getVictim().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpellCast(SpellCastEvent event) {
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (event.getSpell() instanceof Combat || event.getSpell() instanceof Potion) {
            return;
        }
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
        if (event.getSpell() instanceof Dash) {
            this.addStatusEffect(event.getCaster(), RunicStatusEffect.SPEED_III, 4, true);
        } else if (event.getSpell() instanceof TwinFangs twinFangs) {
            actuallyBuffedPlayers.put(event.getCaster().getUniqueId(), twinFangs);
        } else if (event.getSpell() instanceof Cocoon cocoon) {
            actuallyBuffedPlayers.put(event.getCaster().getUniqueId(), cocoon);
            UUID target = cocoon.getTarget(event.getCaster().getUniqueId());
            if (target != null) {
                this.cocoon.add(target);
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
        Number damage = (Number) spellData.getOrDefault("damage", 2);
        this.damage = damage.doubleValue();
        Number damagePerDex = (Number) spellData.getOrDefault("damage-per-dex", .1);
        this.damagePerDex = damagePerDex.doubleValue();
    }
}

