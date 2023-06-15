package com.runicrealms.plugin.tablist;

import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.TabAPI;
import com.runicrealms.plugin.api.event.TabUpdateEvent;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.party.Party;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TabListManager implements Listener, TabAPI {

    private static final List<String> RANK_COLOR_ORDER = List.of(
            ChatColor.DARK_RED.toString(),
            ChatColor.RED.toString(),
            ChatColor.LIGHT_PURPLE.toString(),
            ChatColor.DARK_GREEN.toString(),
            ChatColor.GREEN.toString(),
            ChatColor.DARK_PURPLE.toString(),
            ChatColor.YELLOW.toString(),
            ChatColor.BLUE.toString(),
            ChatColor.AQUA.toString(),
            ChatColor.GOLD.toString(),
            ChatColor.WHITE.toString(),
            ChatColor.GRAY.toString()
    );

    private final Tabbed tabbed;

    public TabListManager(Plugin plugin) {
        this.tabbed = new Tabbed(plugin);
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, plugin);
        updateTablists();
    }

    public static ChatColor getHealthChatColor(Player player) {
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
        return player.getPing();
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
                                + ChatColor.GREEN + ChatColor.BOLD + "Patch 2.0.3 - The Second Age!",
                        ChatColor.DARK_GREEN + "Our Website: " + ChatColor.GREEN + "www.runicrealms.com" +
                                "\n" + ChatColor.DARK_PURPLE + "Our Discord: " + ChatColor.LIGHT_PURPLE + "discord.gg/5FjVVd4");

        // Columns 1-2 (Online)
        tableTabList.set(0, 0, new TextTabItem
                (ChatColor.YELLOW + "" + ChatColor.BOLD + "  Online [" + Bukkit.getOnlinePlayers().size() + "]", 0, Skins.getDot(ChatColor.YELLOW)));

        // Fill column with online players, stop after second column
        try {

            Iterator<Pair<? extends Player, String>> iterator = sortPlayersByRank(Bukkit.getOnlinePlayers()).iterator();
            for (int j = 0; j < 2; j++) {
                for (int i = 1; i <= 19; i++) {
                    Pair<? extends Player, String> online = null;
                    while (iterator.hasNext() && online == null) {
                        online = iterator.next();
                        if (RunicCore.getVanishAPI().getVanishedPlayers().contains(online.first)) online = null;
                    }
                    if (online != null) {
                        tableTabList.set(j, i, new TextTabItem(online.second, getPing(online.first), Skins.getPlayer(online.first)));
                    } else {
                        tableTabList.remove(j, i);
                    }
                }
            }
            // Call event for guilds
            Bukkit.getPluginManager().callEvent(new TabUpdateEvent(player, tableTabList));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTablistNameColor(Player player) {
        User lpUser = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String nameColor;
        if (lpUser == null) {
            nameColor = ChatColor.WHITE.toString();
        } else {
            nameColor = ColorUtil.format(lpUser.getCachedData().getMetaData().getMetaValue("name_color"));
        }
        if (nameColor.equalsIgnoreCase(ChatColor.GRAY.toString())) nameColor = ChatColor.WHITE.toString();
        return nameColor;
    }

    @Override
    public void refreshAllTabLists() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasMetadata("NPC")) continue;
            RunicCore.getInstance().getServer().getScheduler().runTaskLater(RunicCore.getInstance(), () -> setupTab(online), 1);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refreshAllTabLists();
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
            for (Pair<? extends Player, String> sortedMember : sortPlayersByRank(party.getMembersWithLeader())) {
                if (k > 19) break;
                Player member = sortedMember.first;
                String memberColoredName = sortedMember.second;
                tab.set(2, k + 1, new TextTabItem(memberColoredName + " " + getHealthChatColor(member) + (int) member.getHealth() + "❤", getPing(member), Skins.getPlayer(member)));
                k++;
            }
        }
    }

    @Override
    public List<Pair<? extends Player, String>> sortPlayersByRank(Collection<? extends Player> players) {
        Map<Player, String> playerRankColors = new HashMap<>();
        for (Player player : players) {
            playerRankColors.put(player, getTablistNameColor(player));
        }
        List<? extends Player> playersList = new ArrayList<>(players);
        playersList.sort((playerOne, playerTwo) -> {
            int indexOne = RANK_COLOR_ORDER.indexOf(playerRankColors.get(playerOne));
            int indexTwo = RANK_COLOR_ORDER.indexOf(playerRankColors.get(playerTwo));
            if (indexOne == -1) indexOne = Integer.MAX_VALUE;
            if (indexTwo == -1) indexTwo = Integer.MAX_VALUE;
            return Integer.compare(indexOne, indexTwo);
        });
        List<Pair<? extends Player, String>> finalList = new ArrayList<>(players.size());
        for (Player player : playersList) {
            finalList.add(new Pair<>(player, playerRankColors.get(player) + player.getName()));
        }
        return finalList;
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
