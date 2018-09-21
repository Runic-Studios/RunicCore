package me.skyfallin.plugin.skill.skills;

import me.skyfallin.plugin.skill.skilltypes.skillutil.HealUtil;
import me.skyfallin.plugin.skill.skilltypes.Skill;
import me.skyfallin.plugin.skill.skilltypes.SkillItemType;
import me.skyfallin.plugin.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Heal extends Skill {

    ScoreboardUtil boardUtil = new ScoreboardUtil();


    public Heal() {
        super("Heal", "You heal for an amount equal to 25", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 8);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {

        if (player.getHealth() == player.getMaxHealth()) {
            this.doCooldown = false;
            player.sendMessage(ChatColor.GRAY + "You are currently at full health.");
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
        } else {
            this.doCooldown = true;
            HealUtil.healPlayer(25, player, "");
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    boardUtil.updateSideScoreboard(player);
                    boardUtil.updateHealthBar(player);
                }
            }, 1);
        }
    }
}
