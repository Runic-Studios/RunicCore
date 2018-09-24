package us.fortherealm.plugin.skills;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.oldskills.skilltypes.Skill;
import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;

public class SkillUseEvent implements Listener {

    private Main plugin = Main.getInstance();

    @EventHandler
    public void onSkill(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();
        SkillItemType skillItemType = SkillItemType.NONE;

        for (SkillItemType type : SkillItemType.values()) {
            if (type.getSlot() == slot) {
                skillItemType = type;
                break;
            }
        }

        Skill skillCasted = null;
        for (Skill skill : Main.getSkillManager().getSkills()) {
            if (skill.isItem(player.getInventory().getItemInMainHand())) {
                skillCasted = skill;
                break;
            }
        }
        if (skillCasted != null) {
            if (skillItemType == SkillItemType.NONE) {
                player.sendMessage(ChatColor.RED + "You must place this in your " + SkillItemType.ARTIFACT.getName()
                        + " or " + SkillItemType.RUNE.getName() + " slot to use this skills!");
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
                return;
            }


            skillCasted.execute(player, e.getAction(), skillItemType);
        }
    }
}

