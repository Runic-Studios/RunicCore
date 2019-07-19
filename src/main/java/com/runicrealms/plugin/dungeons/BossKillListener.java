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
                pl.sendMessage("");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "DUNGEON COMPLETE: SUNKEN LIBRARY!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "+ Experience");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "+ 3 Uncommon");
                pl.sendMessage("");
            }
            if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
                for (Player member : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {
                    if (member == pl) continue;
                    boolean inDungeon = HearthstoneListener.checkForDungeon(member, member.getLocation());
                    if (inDungeon) {
                        member.sendMessage("");
                        ChatUtils.sendCenteredMessage(member, ChatColor.GREEN + "" + ChatColor.BOLD + "DUNGEON COMPLETE: SUNKEN LIBRARY!");
                        ChatUtils.sendCenteredMessage(member, ChatColor.GRAY + "+ Experience");
                        ChatUtils.sendCenteredMessage(member, ChatColor.GREEN + "+ 3 Uncommon");
                        member.sendMessage("");
                    }
                }
            }
        }
    }
}
