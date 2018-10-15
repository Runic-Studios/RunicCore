package us.fortherealm.plugin.skills.skilltypes.cleric.defensive;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

public class Windstride extends TargetingSkill<Player> {

    private static int BUFF_DURATION = 10;
    private static int SPEED_AMPLIFIER = 1;

    public Windstride() {
        super("Windstride", "You increase the movement speed of yourself and all party members by an amount", true);

        /*
        // TODO: update this effect to not use hashmap, apply to all allies
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = null;
                for(UUID key : ragers.keySet()) {
                    player = ragers.get(key);
                    if (player != null) {
                        player.getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation(), 5, 0.2f,  0.2f, 0.2f, 0.01);
                    }
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 3L);
        */
    }

    @Override
    public void executeSkill() {

        // TODO get party and speed boost everyone
        // TODO: only apply windstride if there are no "stronger" speed effects active on that ally/player

        // Begin sound effects
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 0.5F, 0.7F);
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 0.7F);

        // Check if skill should impact
        SkillImpactEvent event = new SkillImpactEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        // Check if event cancels skill
        if(event.isCancelled())
            return;

        // Send player info message
        getPlayer().sendMessage(ChatColor.GREEN + "You feel the wind at your back!");

        // Add player effects
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION*20, SPEED_AMPLIFIER));

        // Begin system to remove effects
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            getPlayer().sendMessage(ChatColor.GRAY + "The strength of the wind leaves you.");
        }, BUFF_DURATION*20);
    }

}
