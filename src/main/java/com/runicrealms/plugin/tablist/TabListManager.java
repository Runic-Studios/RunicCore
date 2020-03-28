package com.runicrealms.plugin.tablist;

import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.runicguilds.api.RunicGuildsAPI;
import com.runicrealms.runicguilds.config.GuildUtil;
import com.runicrealms.runicguilds.guilds.Guild;
import com.runicrealms.runicguilds.guilds.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

// TODO: fix flickering, fix pings in text component always being '0'
public class TabListManager implements Listener {

    private Tabbed tabbed;

    // constructor
    public TabListManager(Plugin plugin) {
        this.tabbed = new Tabbed(plugin);
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, plugin);
        updateTablists();
    }

    /**
     * Keeps party column updated w/ player health.
     */
    private void updateTablists() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (tabbed.getTabList(online) == null) continue;
                    updatePartyColumn(online);
                }
                //Bukkit.getOnlinePlayers().forEach(p -> updatePartyColumn(p));
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 200L, 5L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasMetadata("NPC")) continue;
            RunicCore.getInstance().getServer().getScheduler().runTaskLaterAsynchronously
                    (RunicCore.getInstance(), () -> setupTab(online), 1);
        }
    }

    public void setupTab(Player pl) {

        // make sure we're starting with a clean slate
        if (tabbed.getTabList(pl) != null) { tabbed.destroyTabList(pl); }

        // build new tablist
        TableTabList tab = tabbed.newTableTabList(pl);

        // header, footer
        tab.setHeaderFooter
                (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Runic Realms\n"
                                + ChatColor.WHITE + ChatColor.BOLD + "A New Adventure Begins!",
                ChatColor.DARK_GREEN + "Visit our website: " + ChatColor.GREEN + "www.runicrealms.com");

        // Column 1 (Online)
        tab.set(0, 0, new TextTabItem
                (ChatColor.YELLOW + "" + ChatColor.BOLD + "  Online [" + Bukkit.getOnlinePlayers().size() + "]",0, Skins.getDot(ChatColor.YELLOW)));

        // fill column with online players, stop after first column
        int i = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (i > 19) break;
            if (online.hasMetadata("NPC")) continue;
            tab.set(0, i + 1, new TextTabItem(online.getName(), 0, Skins.getPlayer(online)));
            i++;
        }

        // Column 2 (Guild)
        Guild guild = RunicGuildsAPI.getGuild(pl.getUniqueId());
        if (guild == null) {
            tab.set(1, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [0]", 0, Skins.getDot(ChatColor.GOLD)));
        } else {
            tab.set(1, 0, new TextTabItem
                    (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [" + GuildUtil.getOnlineMembersWithOwner(guild).size() + "]", 0, Skins.getDot(ChatColor.GOLD))); // +1 for owner
            int j = 0;
            for (GuildMember guildy : GuildUtil.getOnlineMembersWithOwner(guild)) {
                if (j > 19) break;
                Player plMem = Bukkit.getPlayer(guildy.getUUID());
                if (plMem == null) continue;
                tab.set(1, j + 1, new TextTabItem(plMem.getName(), 0, Skins.getPlayer(plMem)));
                j++;
            }
        }

        // Column 4 (Friends)
        tab.set(3, 0, new TextTabItem
                (ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "  Friends [0]", 0, Skins.getDot(ChatColor.DARK_GREEN)));
    }

    /**
     * Used in the running task to keep party health displays accurate.
     */
    private void updatePartyColumn(Player pl) {
        TableTabList tab = (TableTabList) tabbed.getTabList(pl);
        if (RunicCore.getPartyManager().getPlayerParty(pl) == null) {
            tab.set(2, 0, new TextTabItem
                    (ChatColor.GREEN + "" + ChatColor.BOLD + "  Party [0]", 0, Skins.getDot(ChatColor.GREEN)));
        } else {
            Party party = RunicCore.getPartyManager().getPlayerParty(pl);
            tab.set(2, 0, new TextTabItem
                    (ChatColor.GREEN + "" + ChatColor.BOLD + "  Party [" + party.getPartySize() + "]", 0, Skins.getDot(ChatColor.GREEN)));
            int k = 0;
            for (Player member : party.getPlayerMembers()) {
                if (k > 19) break;
                tab.set(2, k + 1, new TextTabItem(member.getName() + " " + ChatColor.RED + (int) member.getHealth() + "❤", 0, Skins.getPlayer(member)));
                k++;
            }
        }
    }

    // update tablist on player quit
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.hasMetadata("NPC")) continue;
                    setupTab(online);
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 1);
    }
}
