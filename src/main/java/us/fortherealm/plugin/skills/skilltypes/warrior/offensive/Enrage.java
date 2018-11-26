package us.fortherealm.plugin.skills.skilltypes.warrior.offensive;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.skillutil.formats.Cone;

public class Enrage extends Skill {

    // instance variables
    private static final int CHANNEL_DURATION = 4;
    private static final int BUFF_DURATION = 10;

    // constructor
    public Enrage() {
        super("Enrage", "channel for 5s, buff for 10s", 8);
    }

    @Override
    public void executeSkill() {

        // apply preliminary particle effects
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION*20, 99));
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, CHANNEL_DURATION*20, 128));
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION*40, 2));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        Cone.coneEffect(getPlayer(), Particle.REDSTONE, CHANNEL_DURATION-1, 0, 3);
        getPlayer().sendMessage(ChatColor.GRAY + "You begin to feel a surge of power!");

        // after the player has channeled the spell
        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayer().sendMessage(ChatColor.GREEN + "You become enraged!");

                // particles, sounds
                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                getPlayer().getWorld().spawnParticle(Particle.REDSTONE, getPlayer().getLocation(),
                        25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 20));

                // potion effects
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION*20, 1));
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, BUFF_DURATION*20, 1));
            }
        }.runTaskLater(Main.getInstance(), CHANNEL_DURATION*20);
    }
}
