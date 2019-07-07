package com.runicrealms.plugin.dungeons;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.utilities.ChatUtils;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BossKillListener implements Listener {

    @EventHandler
    public void onBossKill(MythicMobDeathEvent e) {

        if (!e.getMobType().hasFaction()) return;
        if (e.getMobType().getFaction().toLowerCase().equals("boss")) {

            if (!(e.getKiller() instanceof Player)) return;
            Player pl = (Player) e.getKiller();
            boolean inDungeonPl = HearthstoneListener.checkForDungeon(pl, e.getKiller().getLocation());
            if (inDungeonPl) {
                pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "DUNGEON COMPLETE!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.RED + "Sunken Library");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "+ 2 Uncommon");
                ChatUtils.sendCenteredMessage(pl, ChatColor.AQUA + "+ 1 Rare");
            }
            if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
                for (Player member : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {
                    if (member == pl) continue;
                    boolean inDungeon = HearthstoneListener.checkForDungeon(member, member.getLocation());
                    if (inDungeon) {
                        ChatUtils.sendCenteredMessage(member, ChatColor.GREEN + "" + ChatColor.BOLD + "DUNGEON COMPLETE: SUNKEN LIBRARY");
                        ChatUtils.sendCenteredMessage(member, ChatColor.GRAY + "+ Experience");
                        ChatUtils.sendCenteredMessage(member, ChatColor.GREEN + "+ 2 Uncommon");
                        ChatUtils.sendCenteredMessage(member, ChatColor.AQUA + "+ 1 Rare");
                    }
                }
            }
        }
    }
}
