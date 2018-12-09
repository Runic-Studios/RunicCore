package us.fortherealm.plugin.skillapi.skills.warrior;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skillutil.formats.Cone;

public class Enrage extends Skill {

    // instance variables
    private static final int CHANNEL_DURATION = 4;
    private static final int BUFF_DURATION = 10;

    // constructor
    public Enrage() {
        super("Enrage", "channel for 5s, buff for 10s", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
    }

    // skill execute code
    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        // apply preliminary particle effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION * 20, 99));
        pl.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, CHANNEL_DURATION * 20, 128));
        pl.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION * 40, 2));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        Cone.coneEffect(pl, Particle.REDSTONE, CHANNEL_DURATION - 1, 0, 3);
        pl.sendMessage(ChatColor.GRAY + "You begin to feel a surge of power!");

        // after the player has channeled the spell
        new BukkitRunnable() {
            @Override
            public void run() {
                pl.sendMessage(ChatColor.GREEN + "You become enraged!");

                // particles, sounds
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                        25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 20));

                // potion effects
                pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, 1));
                pl.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, BUFF_DURATION * 20, 1));
            }
        }.runTaskLater(Main.getInstance(), CHANNEL_DURATION * 20);
    }
}

