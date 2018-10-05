package us.fortherealm.plugin.skills.skilltypes.defensive;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;

public class Speed extends Skill {

    private static int DURATION = 5;
    private static int SPEED = 2;

    public Speed() {
        super("Speed","Increase your own move speed by 100 units.");
    }

    @Override
    public void executeSkill() {
        PotionEffectType effectType = PotionEffectType.SPEED;
        getPlayer().addPotionEffect(new PotionEffect(effectType, DURATION, SPEED)); //100 ticks = 5s (Speed III)
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_FIREWORK_BLAST, 0.5F, 1.0F);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            getPlayer().getPotionEffect(effectType);
            getPlayer().sendMessage(ChatColor.GRAY + "Your speed effect has worn off!");
            Skill.delActiveSkill(this);
        }, DURATION*20);
    }
}
