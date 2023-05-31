package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.ShieldBreakEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldPayload;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TwilightResurgence extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private final Set<UUID> cooldownPlayerSet = new HashSet<>();
    private double blindDuration;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double effectCooldown;
    private double radius;

    public TwilightResurgence() {
        super("Twilight Resurgence", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Each time a shield you apply is broken by damage, " +
                "reduce the cooldown of your &aCosmic Prism &7by " + duration + "s. " +
                "This effect has a " + effectCooldown + "s cooldown. " +
                "Additionally, release a pulse around the " +
                "player with the broken shield that " +
                "deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "in a " + radius + " block radius and blinds " +
                "enemies for " + blindDuration + "s!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShieldBreak(ShieldBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getBreakReason() != ShieldBreakEvent.BreakReason.DAMAGE) return;
        ShieldPayload shieldPayload = event.getShieldPayload();
        if (shieldPayload == null) return; // Fixes a bug from race condition due to shield removal task
        if (cooldownPlayerSet.contains(shieldPayload.source().getUniqueId())) return;
        if (!hasPassive(shieldPayload.source().getUniqueId(), this.getName()))
            return; // Ensure the caster has this buff
        cooldownPlayerSet.add(shieldPayload.source().getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> cooldownPlayerSet.remove(shieldPayload.source().getUniqueId()), (long) effectCooldown * 20L);
        RunicCore.getSpellAPI().reduceCooldown(shieldPayload.source(), "Cosmic Prism", duration);
        Player player = shieldPayload.player();
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.5f, 1.0f);
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, shieldPayload.source(), this);
            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) blindDuration * 20, 2));
        }
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number blindDuration = (Number) spellData.getOrDefault("blind-duration", 0);
        setBlindDuration(blindDuration.doubleValue());
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number effectCooldown = (Number) spellData.getOrDefault("effect-cooldown", 0);
        setEffectCooldown(effectCooldown.doubleValue());
    }

    public void setBlindDuration(double blindDuration) {
        this.blindDuration = blindDuration;
    }

    public void setEffectCooldown(double effectCooldown) {
        this.effectCooldown = effectCooldown;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }


    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

}

