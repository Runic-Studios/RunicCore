package us.fortherealm.plugin.skillapi.skills.runic;

import org.bukkit.Particle;
import us.fortherealm.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skillutil.formats.HorizCircleFrame;

@SuppressWarnings("FieldCanBeLocal")
public class Sprint extends Skill {

    // global variables
    private static int BUFF_DURATION = 10;
    private static int SPEED_AMPLIFIER = 2;

    // constructor
    public Sprint() {
        super("Sprint",
                "For " + BUFF_DURATION + " seconds, you gain a" +
                        "\nmassive boost to your speed!", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY,10);
    }

    // skill execute code
    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        // apply effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION*20, SPEED_AMPLIFIER));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getLocation());
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getEyeLocation());
        pl.sendMessage(ChatColor.GREEN + "You gain increased speed!");

        // after the end of the buff
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            pl.sendMessage(ChatColor.GRAY + "Your speed effect has worn off!");
        }, BUFF_DURATION*20);
    }
}

