package com.runicrealms.plugin.tablist;

import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.TabAPI;
import com.runicrealms.plugin.party.Party;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TabListManager implements Listener, TabAPI {

    private final Tabbed tabbed;

    public TabListManager(Plugin plugin) {
        this.tabbed = new Tabbed(plugin);
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, plugin);
        updateTablists();
    }

    private ChatColor getHealthChatColor(Player player) {
        int healthToDisplay = (int) (player.getHealth());
        int maxHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthPercent = (double) healthToDisplay / maxHealth;
        ChatColor chatColor;
        if (healthPercent >= .75) {
            chatColor = ChatColor.GREEN;
        } else if (healthPercent >= .5) {
            chatColor = ChatColor.YELLOW;
        } else if (healthPercent >= .25) {
            chatColor = ChatColor.RED;
        } else {
            chatColor = ChatColor.DARK_RED;
        }
        return chatColor;
    }

    @Override
    public int getPing(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return entityPlayer.ping;
    }

    @Override
    public TableTabList getPlayerTabList(Player player) {
        return (TableTabList) tabbed.getTabList(player);
    }

    @Override
    public void setupTab(Player player) {

        // make sure we're starting with a clean slate
        if (tabbed.getTabList(player) != null) tabbed.destroyTabList(player);

        // build new tablist
        TableTabList tableTabList = tabbed.newTableTabList(player);

        // header, footer
        tableTabList.setHeaderFooter
                (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Runic Realms\n"
                                + ChatColor.GREEN + ChatColor.BOLD + "Patch 2.0 - The Second Age!",
                        ChatColor.DARK_GREEN + "Our Website: " + ChatColor.GREEN + "www.runicrealms.com" +
                                "\n" + ChatColor.DARK_PURPLE + "Our Discord: " + ChatColor.LIGHT_PURPLE + "discord.gg/5FjVVd4");

        // Column 1 (Online)
        tableTabList.set(0, 0, new TextTabItem
                (ChatColor.YELLOW + "" + ChatColor.BOLD + "  Online [" + Bukkit.getOnlinePlayers().size() + "]", 0, Skins.getDot(ChatColor.YELLOW)));

        // fill column with online players, stop after first column
        try {
            int i = 0;
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (i > 19) break;
                tableTabList.set(0, i + 1, new TextTabItem(online.getName(), getPing(online), Skins.getPlayer(online)));
                i++;
            }

            // Column 4 (Friends)
            tableTabList.set(3, 0, new TextTabItem
                    (ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "  Friends [0]", 0, Skins.getDot(ChatColor.DARK_GREEN)));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        // return tableTabList;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasMetadata("NPC")) continue;
            RunicCore.getInstance().getServer().getScheduler().runTaskLaterAsynchronously
                    (RunicCore.getInstance(), () -> setupTab(online), 1);
        }
    }

    /**
     * Update tablist on player quit
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    setupTab(online);
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 1);
    }

    /**
     * Used in the running task to keep party health displays accurate.
     */
    private void updatePartyColumn(Player player) {
        TableTabList tab = (TableTabList) tabbed.getTabList(player);
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) {
            tab.set(2, 0, new TextTabItem
                    (ChatColor.GREEN + "" + ChatColor.BOLD + "  Party [0]", 0, Skins.getDot(ChatColor.GREEN)));
        } else {
            Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
            tab.set(2, 0, new TextTabItem
                    (ChatColor.GREEN + "" + ChatColor.BOLD + "  Party [" + party.getSize() + "]", 0, Skins.getDot(ChatColor.GREEN)));
            int k = 0;
            for (Player member : party.getMembersWithLeader()) {
                if (k > 19) break;
                tab.set(2, k + 1, new TextTabItem(member.getName() + " " + getHealthChatColor(member) + (int) member.getHealth() + "❤", getPing(member), Skins.getPlayer(member)));
                k++;
            }
        }
    }

    /**
     * Keeps party column updated w/ player health.
     */
    private void updateTablists() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (tabbed.getTabList(online) == null) continue;
                updatePartyColumn(online);
            }
        }, 200L, 5L);
    }

}
