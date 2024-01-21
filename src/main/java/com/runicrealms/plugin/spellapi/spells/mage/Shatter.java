package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.effect.ChilledEffect;
import com.runicrealms.plugin.spellapi.effect.IceBarrierEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * First passive for Cryomancer
 *
 * @author BoBoBalloon
 */
public class Shatter extends Spell implements AttributeSpell, MagicDamageSpell, ShieldingSpell {
    private final Map<UUID, Long> cooldown;
    private double baseValue;
    private double damage;
    private double damagePerLevel;
    private double maxStacks;
    private double multiplier;
    private double shield;
    private double shieldPerLevel;
    private double stackDuration;
    private String statName;

    public Shatter() {
        super("Shatter", CharacterClass.MAGE);
        this.cooldown = new HashMap<>();
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("When you land a basic attack on an enemy that " +
                "is &bchilled&7, you shatter their ice, removing &bchilled " +
                "&7and dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage! " +
                "You also gain a stack of &fice barrier&7. " +
                "\n&2&lEFFECT &fIce Barrier" +
                "\n&fIce Barrier &7stacks reduce mob and physical damage taken by " +
                "(" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7)%! " +
                "Max " + maxStacks + " stacks. " +
                "Stacks expire after " + stackDuration + "s.");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 3);
        setMaxStacks(maxStacks.doubleValue());
        Number stackDuration = (Number) spellData.getOrDefault("stack-duration", 20);
        setStackDuration(stackDuration.doubleValue());
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getShield() {
        return this.shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return this.shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, event.getVictim().getUniqueId(), SpellEffectType.CHILLED);
        if (spellEffectOpt.isEmpty()) return;

        ChilledEffect chilledEffect = (ChilledEffect) spellEffectOpt.get();
        chilledEffect.cancel();

        DamageUtil.damageEntitySpell(damage, event.getVictim(), event.getPlayer(), this);

        Optional<SpellEffect> iceBarrierOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.ICE_BARRIER);
        if (iceBarrierOpt.isPresent()) {
            IceBarrierEffect iceBarrierEffect = (IceBarrierEffect) iceBarrierOpt.get();
            iceBarrierEffect.increment(event.getVictim().getEyeLocation(), 1);
        } else {
            IceBarrierEffect iceBarrierEffect = new IceBarrierEffect(
                    event.getPlayer(),
                    (int) this.maxStacks,
                    (int) this.stackDuration,
                    1,
                    event.getVictim().getEyeLocation()
            );
            iceBarrierEffect.initialize();
        }
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
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

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public void setStackDuration(double stackDuration) {
        this.stackDuration = stackDuration;
    }
}

