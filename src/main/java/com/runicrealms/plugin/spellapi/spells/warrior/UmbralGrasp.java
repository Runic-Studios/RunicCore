package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UmbralGrasp extends Spell implements DurationSpell, MagicDamageSpell {
    private final Map<UUID, WitherSkull> witherSkullMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double speedMultiplier;

    public UmbralGrasp() {
        super("Umbral Grasp", CharacterClass.WARRIOR);
        this.setDescription("You conjure a spectral skull and launch it forwards! " +
                "Hitting an enemy deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage, " +
                "pulls you to the target, and slows them for " + duration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        witherSkullMap.put(player.getUniqueId(), player.launchProjectile(WitherSkull.class));
        WitherSkull witherSkull = witherSkullMap.get(player.getUniqueId());
        EntityTrail.entityTrail(witherSkull, Color.fromRGB(185, 251, 185));
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(speedMultiplier);
        witherSkull.setVelocity(velocity);
        witherSkull.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireballDamage(ProjectileHitEvent event) {
        if (witherSkullMap.isEmpty()) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!witherSkullMap.containsKey(player.getUniqueId())) return;
        WitherSkull witherSkull = witherSkullMap.get(player.getUniqueId());
        witherSkull.remove();
        witherSkullMap.remove(player.getUniqueId());
        event.setCancelled(true);
        if (!(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (!isValidEnemy(player, victim)) return;
        DamageUtil.damageEntitySpell(this.damage, victim, player, this);
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 3, 0.5F, 0.5F, 0.5F, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
        // todo:
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number speedMultiplier = (Number) spellData.getOrDefault("speed-multiplier", 0);
        setSpeedMultiplier(speedMultiplier.doubleValue());
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
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

}

