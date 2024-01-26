package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectManager;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.warrior.BlessedBladeEffect;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BlessedBlade extends Spell implements DurationSpell, HealingSpell, MagicDamageSpell, RadiusSpell {
    private double duration;
    private double heal;
    private double healingPerLevel;
    private double magicDamage;
    private double magicDamagePerLevel;
    private int maxCharges;
    private double maxTargets;
    private double radius;

    public BlessedBlade() {
        super("Blessed Blade", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Each time you cast a spell, your next " + maxCharges + " basic attacks " +
                "deal an additional (" + magicDamage + " + &f" + magicDamagePerLevel + "x&7 lvl) " +
                "magicʔ damage! They also heal you and up to " + maxTargets + " allies " +
                "within " + radius + " blocks for (" + heal + " + &f" + healingPerLevel + "x&7 lvl) health! " +
                "Your empowered attacks expire after " + duration + "s.");
    }

    @Override
    public double getHeal() {
        return heal;
    }

    @Override
    public void setHeal(double heal) {
        this.heal = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return this.healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

    @Override
    public double getMagicDamage() {
        return magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @Override
    public void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number magicDamagePerLevel = (Number) spellData.getOrDefault("magic-damage-per-level", 0);
        setMagicDamagePerLevel(magicDamagePerLevel.doubleValue());
        Number maxCharges = (Number) spellData.getOrDefault("max-charges", 0);
        setMaxCharges(maxCharges.intValue());
        Number maxTargets = (Number) spellData.getOrDefault("max-targets", 0);
        setMaxTargets(maxTargets.doubleValue());
    }

    private void setMaxCharges(int charges) {
        this.maxCharges = charges;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.BLESSED_BLADE);
        if (spellEffectOpt.isEmpty()) return;
        BlessedBladeEffect blessedBladeEffect = (BlessedBladeEffect) spellEffectOpt.get();
        blessedBladeEffect.decrement(event.getVictim().getEyeLocation(), 1);
        // Additional damage
        DamageUtil.damageEntitySpell(magicDamage, event.getVictim(), player, this);
        // Heal caster and allies
        healPlayer(player, player, heal, this);
        int alliesHealed = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            if (entity.equals(player)) continue;
            healPlayer(player, (Player) entity, heal, this);
            alliesHealed++;
            if (alliesHealed >= maxTargets)
                break;
        }
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpell() instanceof Combat || event.getSpell() instanceof Potion) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        UUID uuid = event.getCaster().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.BLESSED_BLADE);
        if (spellEffectOpt.isEmpty()) {
            BlessedBladeEffect blessedBladeEffect = new BlessedBladeEffect(
                    event.getCaster(),
                    this.maxCharges,
                    (int) this.duration,
                    this.maxCharges,
                    event.getCaster().getEyeLocation()
            );
            blessedBladeEffect.initialize();
        } else {
            BlessedBladeEffect blessedBladeEffect = (BlessedBladeEffect) spellEffectOpt.get();
            SpellEffectManager spellEffectManager = (SpellEffectManager) RunicCore.getSpellEffectAPI();
            blessedBladeEffect.refresh(event.getCaster().getEyeLocation(), spellEffectManager.getGlobalCounter());
        }
    }

    public void setMaxTargets(double maxTargets) {
        this.maxTargets = maxTargets;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}
