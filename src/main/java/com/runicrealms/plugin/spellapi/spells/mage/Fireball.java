package com.runicrealms.plugin.spellapi.spells.mage;

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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Spell {

    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 25;
    private SmallFireball fireball;

    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, 5, 15);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireballDamage(EntityDamageByEntityEvent e) {

        // only listen for our fireball
        if (!(e.getDamager().equals(this.fireball))) return;

        e.setCancelled(true);

        // grab our variables
        Player player = (Player) fireball.getShooter();
        if (player == null) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) e.getEntity();

        if (verifyEnemy(player, victim)) {
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, false);
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
        }
    }
}

