package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.RangedDamageEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.archer.ChargedEffect;
import com.runicrealms.plugin.spellapi.effect.archer.StaticEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Ultimate passive for Stormshot subclass
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Overcharge extends Spell implements DurationSpell {
    private static final List<Class<?>> SPELL_CLASSES = Arrays.asList(ThunderArrow.class, Jolt.class);
    private double duration;
    private double manaToRestore;
    private double percent;
    private double markedDuration;
    private double maxStacks;
    private double stackDuration;
    private double stacksPerIncrement;

    public Overcharge() {
        super("Overcharge", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("When you deal damage to an enemy with &aThunder Arrow &7or &aJolt&7, " +
                "you mark them with static electricity for " + this.markedDuration + "s. " +
                "When you land a ranged basic attack against a static target, " +
                "consume the mark to gain " + stacksPerIncrement + " stack(s) of &9charged &7and restore " + this.manaToRestore + " mana!" +
                "\n\n&2&lEFFECT &aCharged" +
                "\n&7While &9charged&7, you gain " + (this.percent * 100) + "% attack speed! " +
                "Max " + maxStacks + " stacks. " +
                "Each stack expires after " + stackDuration + "s. Lose all stacks upon exiting combat.");
    }

    /**
     * Charged stacks expire upon combat exit
     */
    @EventHandler
    public void onExitCombat(LeaveCombatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Optional<SpellEffect> effectOptional = this.getSpellEffect(uuid, uuid, SpellEffectType.CHARGED);
        if (effectOptional.isPresent()) {
            ChargedEffect chargedEffect = (ChargedEffect) effectOptional.get();
            chargedEffect.cancel();
        }
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number manaRestore = (Number) spellData.getOrDefault("mana-restore", 10);
        this.manaToRestore = manaRestore.intValue();
        Number percent = (Number) spellData.getOrDefault("percent", .3);
        this.percent = percent.doubleValue();
        Number markedDuration = (Number) spellData.getOrDefault("marked-duration", this.duration);
        this.markedDuration = markedDuration.doubleValue();
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 10);
        this.maxStacks = maxStacks.doubleValue();
        Number stackDuration = (Number) spellData.getOrDefault("stack-duration", 5);
        this.stackDuration = stackDuration.doubleValue();
        Number stacksPerIncrement = (Number) spellData.getOrDefault("stacks-per-increment", 2);
        this.stacksPerIncrement = stacksPerIncrement.doubleValue();
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * When either Thunder Arrow or Jolt hits a target with this passive enabled, make them marked
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        Player player = event.getPlayer();
        if (!this.hasPassive(player.getUniqueId(), this.getName()) ||
                SPELL_CLASSES.stream().noneMatch(spellClass -> spellClass.isInstance(event.getSpell()))) {
            return;
        }
        // Target has already been marked, refresh
        Optional<SpellEffect> effectOptional = this.getSpellEffect(player.getUniqueId(), event.getVictim().getUniqueId(), SpellEffectType.STATIC);
        if (effectOptional.isPresent()) {
            StaticEffect staticEffect = (StaticEffect) effectOptional.get();
            staticEffect.refresh();
            return;
        }
        StaticEffect staticEffect = new StaticEffect(player, event.getVictim(), (int) this.markedDuration);
        staticEffect.initialize();
    }

    /**
     * If an enemy hit while marked, the player is now overcharged and the enemy should be unmarked
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onRangedDamage(RangedDamageEvent event) {
        Player player = event.getPlayer();
        LivingEntity victim = event.getVictim();
        Optional<SpellEffect> effectOptional = this.getSpellEffect(player.getUniqueId(), victim.getUniqueId(), SpellEffectType.STATIC);
        if (effectOptional.isPresent()) {
            StaticEffect staticEffect = (StaticEffect) effectOptional.get();
            staticEffect.cancel();
            RunicCore.getRegenManager().addMana(player, (int) this.manaToRestore);
            addChargedStack(player, victim.getEyeLocation());
        }
    }

    private void addChargedStack(Player player, Location victimLocation) {
        Optional<SpellEffect> effectOptional = this.getSpellEffect(player.getUniqueId(), player.getUniqueId(), SpellEffectType.CHARGED);
        ChargedEffect chargedEffect;
        if (effectOptional.isPresent()) {
            chargedEffect = (ChargedEffect) effectOptional.get();
            chargedEffect.increment(victimLocation, (int) this.stacksPerIncrement);
        } else {
            chargedEffect = new ChargedEffect(player, (int) this.maxStacks, (int) this.stackDuration, (int) this.stacksPerIncrement, victimLocation);
            chargedEffect.initialize();
        }
    }

    /**
     * When a player is overcharged, they gain a cooldown reduction on their primary fire
     * Priority is high, so it runs after RapidFire; it should stack with it
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBasicAttack(BasicAttackEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> chargedOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.CHARGED);
        if (!this.hasPassive(uuid, this.getName()) || chargedOpt.isEmpty()) {
            return;
        }

        double ticksToReduce = calculateCooldownTicksToReduce(event.getOriginalCooldownTicks(), (ChargedEffect) chargedOpt.get());
        // Cooldown cannot drop beneath a certain value
        event.setCooldownTicks(Math.max(event.getUnroundedCooldownTicks() - ticksToReduce, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS)); //apply reduction to current cooldown time
    }

    private double calculateCooldownTicksToReduce(int originalCooldownTicks, ChargedEffect chargedEffect) {
        int stacks = chargedEffect.getStacks().get();
        double ticksToReduce = originalCooldownTicks * this.percent; //reduce the cooldown based on the total cooldown time
        ticksToReduce *= stacks;
        return ticksToReduce;
    }
}
