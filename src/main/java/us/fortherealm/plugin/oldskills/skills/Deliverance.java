package us.fortherealm.plugin.oldskills.skills;

import us.fortherealm.plugin.oldskills.skilltypes.Skill;
import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;
import us.fortherealm.plugin.oldskills.skilltypes.skillutil.Bubble;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

// TODO: party check for knockback, dont knockback allies (let them enter bubble)
public class Deliverance extends Skill {

    public Deliverance() {
        super("Deliverance", "summon a barrier of holy power around yourself for 8 seconds." +
                        "the barrier repels all enemies. party members may pass through the barrier freely. during this time you may not move",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 9);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {

        // player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 99));
        player.setWalkSpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 140, 128));
        Bubble.bubbleEffect(player.getLocation(), Particle.FIREWORKS_SPARK, 10 /* 5 oscillations */, 0, 1, 5);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getLocation().getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        long startTime = System.currentTimeMillis() / 1000;
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis() / 1000;
                long timePassed = currentTime - startTime;
                if (timePassed >= 7) { // 8 seconds actually, cuz math
                    this.cancel();
                    player.setWalkSpeed(0.2f);
                }
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 0.01F, 0.5F);
                for (Entity entity : player.getLocation().getChunk().getEntities()) {
                    if (entity.getLocation().distance(player.getLocation()) <= 5) {
                        if (entity != (player)) {
                            if (entity.getType().isAlive()) {
                                Vector force = (player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.75).setY(0.3));
                                entity.setVelocity(force);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1L); // every tick
    }
}