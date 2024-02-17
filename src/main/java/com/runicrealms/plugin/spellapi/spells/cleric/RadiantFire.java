package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.cleric.RadiantFireEffect;
import com.runicrealms.plugin.spellapi.event.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RadiantFire extends Spell implements AttributeSpell, DurationSpell {
    private double baseValue;
    private double maxStacks;
    private double multiplier;
    private double stackDuration;
    private String statName;
    private double stackThreshold;

    public RadiantFire() {
        super("Radiant Fire", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Each time you land your &aSear &7spell, you gain " +
                "a stack of &eradiant fire&7! " +
                "\n\n&2&lEFFECT &eRadiant Fire" +
                "\n&7For each stack of &eradiant fire&7, you gain " + (multiplier * 100) +
                "% of your total &eWisdom✸ &7as increased healing! " +
                "While above " + stackThreshold + " stacks, your &aRadiant Nova &7has no warmup and cleanses!" +
                "\nMax stacks: " + (int) maxStacks + "\nStacks expiry: " + stackDuration + "s");
    }

    /**
     * If a player has the passive, attempts to add a stack of radiant fire when 'Sear' is cast.
     * Fails if the player is >= max stacks
     *
     * @param event the magic damage event
     */
    private void attemptToStackRadiantFire(MagicDamageEvent event) {
        Player player = event.getPlayer();
        Location hologramLocation = event.getVictim().getEyeLocation();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), player.getUniqueId(), SpellEffectType.RADIANT_FIRE);
        if (spellEffectOpt.isEmpty()) {
            RadiantFireEffect radiantFireEffect = new RadiantFireEffect(
                    player,
                    (int) this.maxStacks,
                    (int) this.stackThreshold,
                    (int) this.stackDuration,
                    1,
                    hologramLocation);
            radiantFireEffect.initialize();
        } else {
            RadiantFireEffect radiantFireEffect = (RadiantFireEffect) spellEffectOpt.get();
            radiantFireEffect.increment(hologramLocation, 1);
        }
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }

    @Override
    public double getDuration() {
        return stackDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.stackDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("stack-duration", 12);
        setDuration(duration.doubleValue());
        Number stacks = (Number) spellData.getOrDefault("max-stacks", 5);
        setMaxStacks(stacks.doubleValue());
        Number threshold = (Number) spellData.getOrDefault("stack-threshold", 4);
        setStackThreshold(threshold.doubleValue());
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public void setStackThreshold(double stackThreshold) {
        this.stackThreshold = stackThreshold;
    }

    @EventHandler
    public void onSpellCast(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof Sear)) return;
        attemptToStackRadiantFire(event);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof HealingSpell)) return;
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.RADIANT_FIRE);
        if (spellEffectOpt.isEmpty()) return;
        int wisdom = RunicCore.getStatAPI().getPlayerWisdom(event.getPlayer().getUniqueId());
        double bonus = (multiplier * wisdom) / 100;
        RadiantFireEffect radiantFireEffect = (RadiantFireEffect) spellEffectOpt.get();
        int stacks = radiantFireEffect.getStacks().get();
        bonus *= stacks;
        event.setAmount((int) (event.getAmount() + (event.getAmount() * bonus)));
    }
}

