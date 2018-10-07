package us.fortherealm.plugin.skills.skilltypes.cleric.defensive;

import org.bukkit.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;

public class Windstride extends Skill {

    private static int DURATION = 10;

    public Windstride() {
        super("Windstride", "You increase the movement speed of yourself and all party members by 50 units.");
    }

    @Override
    public void executeSkill() {

        // TODO get party and speed boost everyone

        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 0.5F, 0.7F);
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 0.7F);
        getPlayer().sendMessage(ChatColor.GREEN + "You feel the wind at your back!");
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1)); //200 ticks = 10s (Speed I)
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                getPlayer().sendMessage(ChatColor.GRAY + "The strength of the wind leaves you.");
                getPlayer().removePotionEffect(PotionEffectType.SPEED);
            Skill.delActiveSkill(this);
        }, DURATION *20);
    }

}
