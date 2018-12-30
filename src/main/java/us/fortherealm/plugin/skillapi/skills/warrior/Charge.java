package us.fortherealm.plugin.skillapi.skills.warrior;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Charge extends Skill {

    private static final double LAUNCH_PATH_MULT = 2.5;
    private static final double HEIGHT = 0.2;

    // constructor
    public Charge() {
        super("Charge", "You charge fearlessly into battle!",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
    }

    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        // skill variables, vectors
        Vector look = pl.getLocation().getDirection();
        Vector launchPath = new Vector(look.getX(), HEIGHT, look.getZ()).normalize();

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210,180,140), 20));

        // CHARGEE!!
        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));
    }
}
