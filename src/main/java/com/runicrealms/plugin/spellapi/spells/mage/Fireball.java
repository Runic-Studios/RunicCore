package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Fireball extends Spell implements MagicDamageSpell {
    private static final double FIREBALL_SPEED = 2;
    private final Map<UUID, SmallFireball> fireballMap = new HashMap<>();
    private double magicDamage;
    private double magicDamagePerLevel;

    public Fireball() {
        super("Fireball", CharacterClass.MAGE);
        this.setDescription
                (
                        "You launch a projectile fireball that deals " +
                                "(" + this.magicDamage + " + &f" + this.magicDamagePerLevel + "x&7 lvl) " +
                                "magicʔ damage on impact!"
                );
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        fireballMap.put(player.getUniqueId(), player.launchProjectile(SmallFireball.class));
        SmallFireball fireball = fireballMap.get(player.getUniqueId());
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @Override
    public double getMagicDamage() {
        return this.magicDamage;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireballDamage(ProjectileHitEvent event) {
        if (fireballMap.isEmpty()) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!fireballMap.containsKey(player.getUniqueId())) return;
        SmallFireball fireball = fireballMap.get(player.getUniqueId());
        fireball.remove();
        fireballMap.remove(player.getUniqueId());
        event.setCancelled(true);
        if (!(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (!isValidEnemy(player, victim)) return;
        DamageUtil.damageEntitySpell(this.magicDamage, victim, player, this);
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 3, 0.5F, 0.5F, 0.5F, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }

}

