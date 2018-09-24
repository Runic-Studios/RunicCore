package us.fortherealm.plugin.skills.skilltypes.offensive;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.util.KnockbackUtil;

// TODO: if hit enemy is a player, check if enemy player is in OUTLAW MODE for damage check
public class Fireball extends TargetingSkill<Player> {

    public Fireball() {
        super ("Fireball", "Shoots a fireball.");
    }

    @EventHandler
    public void executeSkill(Player player) {
        SmallFireball fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(2);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof SmallFireball) {
            SmallFireball smfb = (SmallFireball) event.getDamager();
            if (smfb.getShooter() instanceof Player) {
                Player player = (Player) smfb.getShooter();
                LivingEntity victim = (LivingEntity) event.getEntity();
                event.setCancelled(true);
                victim.damage(20, player);
                victim.setLastDamageCause(event);
                KnockbackUtil.knockback(player, victim);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                victim.getWorld().spigot().playEffect(victim.getEyeLocation(),
                        Effect.FLAME, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);
            }
        }
    }
}
