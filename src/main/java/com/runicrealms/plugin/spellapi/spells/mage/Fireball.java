package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class Fireball extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMOUNT = 10;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final double FIREBALL_SPEED = 2;
    private SmallFireball fireball;

    public Fireball() {
        super("Fireball",
                "You launch a projectile fireball " +
                        "that deals (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage on impact!",
                ChatColor.WHITE, CharacterClass.MAGE, 4, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireballDamage(ProjectileHitEvent event) {
        if (!event.getEntity().equals(fireball)) return;
        fireball.remove();
        event.setCancelled(true);
        Player player = (Player) fireball.getShooter();
        if (player == null) return;
        if (!(event.getHitEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getHitEntity();
        if (!isValidEnemy(player, victim)) return;
        DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, this);
        victim.getWorld().spawnParticle(Particle.SNOWBALL, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }
}

