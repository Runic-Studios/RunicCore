package us.fortherealm.plugin.skills.skilltypes.runic.defensive;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.skillutil.formats.HorizCircleFrame;

public class Speed extends Skill {

    // global variables
    private static int BUFF_DURATION = 10;
    private static int SPEED_AMPLIFIER = 2;

    // default constructor
    public Speed() {
        super("Speed", "For " + BUFF_DURATION + " seconds, you gain massively increased movement speed!", 6);
    }

    @Override
    public void executeSkill() {

        // apply effects
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION*20, SPEED_AMPLIFIER));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, getPlayer().getLocation());
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, getPlayer().getEyeLocation());
        getPlayer().sendMessage(ChatColor.GREEN + "You gain increased speed!");

        // after the end of the buff
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            getPlayer().sendMessage(ChatColor.GRAY + "Your speed effect has worn off!");
        }, BUFF_DURATION*20);
    }
}
