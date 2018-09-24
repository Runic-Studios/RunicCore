package us.fortherealm.plugin.skill.skills;

import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skill.skilltypes.Skill;
import us.fortherealm.plugin.skill.skilltypes.SkillItemType;
import us.fortherealm.plugin.skill.skilltypes.skillutil.KnockbackUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

// TODO: if hit enemy is a player, check if enemy player is in OUTLAW MODE for damage check
public class Frostbolt extends Skill {

    private int radius;

    public Frostbolt() {
        super("Frostbolt", "Shoots a Frostbolt", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 6);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        Snowball snowball = player.launchProjectile(Snowball.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(2);
        snowball.setVelocity(velocity);
        snowball.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5f, 1);

        new BukkitRunnable() {

            public void run() {
                playParticle(Particle.SNOW_SHOVEL, snowball.getLocation());
                if (snowball.isOnGround() || snowball.isDead()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
    }
    @EventHandler
    public void onFrostboltDamage(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();
            if (snowball.getShooter() instanceof Player) {

                Player player = (Player) snowball.getShooter();
                LivingEntity victim = (LivingEntity) event.getEntity();
                event.setCancelled(true);

                victim.damage(10, player);
                KnockbackUtil.knockback(player, victim);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1)); //100 ticks = 5s (Slowness II)
                if (victim instanceof Player) {
                    victim.sendMessage(ChatColor.RED + "You are slowed by " + ChatColor.WHITE + player.getName() + "§c's frostbolt!");
                }
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
                victim.getWorld().spigot().playEffect(victim.getEyeLocation(), Effect.SNOWBALL_BREAK, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);
            }
        }
    }

    public void playParticle(Particle particle, Location location) {
        for (double b = 0; b <= 360; b++) {
            Location loc = location.clone();
            int radius = 100;

            double theta = Math.toRadians(b);
            Vector vector = new Vector (this.radius * Math.cos(theta), 0D, this.radius * Math.sin(0));

            loc.getWorld().spawnParticle(particle, location.add(vector), 1, 0, 0, 0, 0);
            loc.subtract(vector);
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}

