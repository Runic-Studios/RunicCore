package us.fortherealm.plugin.skill.skills;

import us.fortherealm.plugin.skill.skilltypes.Skill;
import us.fortherealm.plugin.skill.skilltypes.SkillItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed extends Skill {

    public Speed() {
        super("Speed","Increase your own move speed by 100 units.", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY,10);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2)); //100 ticks = 5s (Speed III)
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_BLAST, 0.5F, 1.0F);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                player.sendMessage(ChatColor.GRAY + "Your speed effect has worn off!");
            }
        }, 100);//5 secs
    }
}
