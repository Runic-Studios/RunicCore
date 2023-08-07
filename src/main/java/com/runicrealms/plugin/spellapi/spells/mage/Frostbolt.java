package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @deprecated {@link ColdTouch} was replaced
 */
@Deprecated
public class Frostbolt extends Spell implements MagicDamageSpell {
    private static final double SPEED_MULT = 2.5;
    private final Map<UUID, Snowball> snowballMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;

    public Frostbolt() {
        super("Frostbolt", CharacterClass.MAGE);
        this.setDescription("You launch a projectile bolt of ice " +
                "that deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage on " +
                "impact and slows its target!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        snowballMap.put(player.getUniqueId(), player.launchProjectile(Snowball.class));
        Snowball snowball = snowballMap.get(player.getUniqueId());
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SPEED_MULT);
        snowball.setVelocity(velocity);
        snowball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        EntityTrail.entityTrail(snowball, Particle.SNOWBALL);
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireballDamage(ProjectileHitEvent event) {
        if (snowballMap.isEmpty()) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!snowballMap.containsKey(player.getUniqueId())) return;
        Snowball snowball = snowballMap.get(player.getUniqueId());
        snowball.remove();
        snowballMap.remove(player.getUniqueId());
        event.setCancelled(true);
        if (!(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (!isValidEnemy(player, victim)) return;
        DamageUtil.damageEntitySpell(damage, victim, player, this);
        addStatusEffect(victim, RunicStatusEffect.SLOW_III, 5, false);
        victim.getWorld().spawnParticle(Particle.BLOCK_DUST, victim.getEyeLocation(),
                5, 0.5F, 0.5F, 0.5F, 0, Material.PACKED_ICE.createBlockData());
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
    }

}

