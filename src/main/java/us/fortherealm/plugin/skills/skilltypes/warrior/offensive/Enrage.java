package us.fortherealm.plugin.skills.skilltypes.warrior.offensive;

import org.bukkit.ChatColor;
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

        /* TODO: fix this particle effect, add removal message
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = null;
                for(UUID key : ragers.keySet()) {
                    player = ragers.get(key);
                    if (player != null) {
                        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 5, 0.2f,  0.2f, 0.2f, 0.01);
                    }
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 3L);
        */
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
                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION*20, 1));
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, BUFF_DURATION*20, 1));
            }
        }.runTaskLater(Main.getInstance(), CHANNEL_DURATION*20);
    }

    //@Override
    //public void onRemoval() {

        // end of the buff
        //getPlayer().sendMessage(ChatColor.GRAY + "You no longer feel enraged.");
    //}
}
