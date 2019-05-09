package us.fortherealm.plugin.tablist;

import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TabList;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skins;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.parties.Party;

// TODO: fix flickering, fix pings in text component always being '0'
public class TabListManager implements Listener {

    // globals
    private Tabbed tabbed;

    // constructor
    public TabListManager(Plugin plugin) {
        this.tabbed = new Tabbed(plugin);
        FTRCore.getInstance().getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasMetadata("NPC")) continue;
            TabList test = tabbed.getTabList(online);
            FTRCore.getInstance().getServer().getScheduler().runTaskLaterAsynchronously
                    (FTRCore.getInstance(), () -> setupTab(online), 1);
        }
    }

    public void setupTab(Player player) {

        // make sure we're starting with a clean slate
        if (tabbed.getTabList(player) != null) { tabbed.destroyTabList(player); }

        // build new tablist
        TableTabList tab = tabbed.newTableTabList(player);

        // header, footer
        tab.setHeaderFooter
                (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "HavenRealms\n"
                                + ChatColor.WHITE + ChatColor.BOLD + "A New Kind of MMORPG",
                ChatColor.RED + "Visit our website: " + ChatColor.GOLD + "www.havenrealms.com");

        // Column 1 (Online)
        tab.set(0, 0, new TextTabItem
                (ChatColor.YELLOW + "" + ChatColor.BOLD + "  Online [" + Bukkit.getOnlinePlayers().size() + "]",0, Skins.getDot(ChatColor.YELLOW)));

        // fill column with online players
        int i = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasMetadata("NPC")) continue;
            String storedName = FTRCore.getInstance().getConfig().get(online.getUniqueId() + ".info.name").toString();
            if (storedName != null) {
                tab.set(0, i + 1, new TextTabItem(storedName, 0, Skins.getPlayer(online)));
            } else {
                tab.set(0, i + 1, new TextTabItem(online.getName(), 0, Skins.getPlayer(online)));
            }
            i++;
        }

        // Column 2 (Guild)
        tab.set(1, 0, new TextTabItem
                (ChatColor.GOLD + "" + ChatColor.BOLD + "  Guild [0]" , 0, Skins.getDot(ChatColor.GOLD)));

        // Column 3 (Party)
        if (FTRCore.getPartyManager().getPlayerParty(player) == null) {
            tab.set(2, 0, new TextTabItem
                    (ChatColor.GREEN + "" + ChatColor.BOLD + "  Party [0]", 0, Skins.getDot(ChatColor.GREEN)));
        } else {
            Party party = FTRCore.getPartyManager().getPlayerParty(player);
            tab.set(2, 0, new TextTabItem
                    (ChatColor.GREEN + "" + ChatColor.BOLD + "  Party [" + party.getPartySize() + "]", 0, Skins.getDot(ChatColor.GREEN)));
            int j = 0;
            for (Player member : party.getPlayerMembers()) {
                String storedName = FTRCore.getInstance().getConfig().get(member.getUniqueId() + ".info.name").toString();
                tab.set(2, j+1, new TextTabItem(storedName, 0, Skins.getPlayer(member)));
                j++;
            }
        }

        // Column 4 (Friends)
        tab.set(3, 0, new TextTabItem
                (ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "  Friends [0]", 0, Skins.getDot(ChatColor.DARK_GREEN)));
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
        }.runTaskLater(FTRCore.getInstance(), 1);
    }
}
