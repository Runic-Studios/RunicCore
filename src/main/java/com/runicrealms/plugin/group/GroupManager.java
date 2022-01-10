package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class GroupManager implements Listener {

    private static final String PREFIX = "&2[Group Finder] &6»";

    private final Map<IGroupFinderItem, List<Player>> queues;
    private final GroupFinderMainUI ui;
    private final GroupFinderMiniBossUI miniBossUI;
    private final GroupFinderGrindingUI grindingUI;
    private final GroupFinderDungeonUI dungeonUI;

    public static final Map<IGroupFinderItem, Integer> MAX_MEMBERS = new HashMap<>(getValues());

    public GroupManager() {
        this.queues = new HashMap<>();
        this.ui = new GroupFinderMainUI();
        this.miniBossUI = new GroupFinderMiniBossUI();
        this.grindingUI = new GroupFinderGrindingUI();
        this.dungeonUI = new GroupFinderDungeonUI();

        Set<IGroupFinderItem> reasons = new HashSet<>(Arrays.asList(GroupFinderItem.values()));
        for (IGroupFinderItem iGroupFinderItem : reasons) {
            this.queues.put(iGroupFinderItem, new ArrayList<>());
        }

        this.registerEvents(this, this.ui, this.miniBossUI, this.grindingUI, this.dungeonUI);
    }

    public GroupFinderMainUI getUI() {
        return this.ui;
    }

    public GroupFinderMiniBossUI getMiniBossUI() {
        return this.miniBossUI;
    }

    public GroupFinderGrindingUI getGrindingUI() {
        return this.grindingUI;
    }

    public GroupFinderDungeonUI getDungeonUI() {
        return this.dungeonUI;
    }

    public void addToQueue(IGroupFinderItem reason, Player player) {
        List<Player> list = this.queues.get(reason);
        int max = MAX_MEMBERS.get(reason);
        list.add(player);

        for (Player member : list) {
            member.sendMessage(ColorUtil.format(PREFIX + "&r&a [" + list.size() + "/" + max + "]"));
        }

        if (list.size() >= max) {
            Player leader = list.get(0);
            Party party = new Party(leader);
            leader.sendMessage(ColorUtil.format(PREFIX + "&r&a You have been made the leader of the party."));
            RunicCore.getPartyManager().getParties().add(party);
            RunicCore.getPartyManager().updatePlayerParty(leader, party);
            list.remove(0);
            for (Player member : list) {
                party.acceptMemberInvite(member);
                member.sendMessage(ColorUtil.format(PREFIX + "&r&a You have been added into the party."));
                RunicCore.getPartyManager().updatePlayerParty(member, party);
                RunicCore.getTabListManager().setupTab(member);
            }
            RunicCore.getTabListManager().setupTab(leader);
            list.clear();
        }
    }

    public void removeFromQueue(Player player) {
        List<Player> queue = null;
        for (List<Player> players : this.queues.values()) {
            if (players.contains(player)) {
                queue = players;
                break;
            }
        }

        if (queue != null) {
            queue.remove(player);
            for (Player member : queue) {
                member.sendMessage(ColorUtil.format(PREFIX + "&r&a A player left the queue."));
            }
        }
    }

    public boolean isInQueue(Player player) {
        for (List<Player> players : this.queues.values()) {
            if (players.contains(player)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        InventoryHolder inventoryHolder = e.getView().getTopInventory().getHolder();
        if (inventoryHolder instanceof GroupFinderMainUI
                || inventoryHolder instanceof GroupFinderMiniBossUI
                || inventoryHolder instanceof GroupFinderGrindingUI
                || inventoryHolder instanceof GroupFinderDungeonUI) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            if (e.getCurrentItem() == null) return;
            if (e.getCurrentItem().getType() == Material.AIR) return;
            if (e.getCurrentItem().getType() == GUIUtil.borderItem().getType()) return;
            ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.removeFromQueue(event.getPlayer());
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, RunicCore.getInstance());
        }
    }

    /**
     * Initialize max members for given group finder option
     *
     * @return a map of max members for finder option
     */
    private static Map<IGroupFinderItem, Integer> getValues() {
        List<IGroupFinderItem> groupFinderItems = new ArrayList<>(Arrays.asList(GroupFinderItem.values()));

        FileConfiguration config = RunicCore.getInstance().getConfig();
        Map<IGroupFinderItem, Integer> returnValue = new HashMap<>();

        for (IGroupFinderItem iGroupFinderItem : groupFinderItems) {
            int max;
            if (iGroupFinderItem.getQueueReason() == QueueReason.DUNGEONS) {
                String name = iGroupFinderItem.toString().replace('_', '-').toLowerCase();
                max = config.getInt("queue-recommended.dungeons." + name);
                returnValue.put(iGroupFinderItem, max);
                continue;
            } else if (iGroupFinderItem.getQueueReason() == QueueReason.GRINDING) {
                max = config.getInt("queue-recommended.grinding");
            } else {
                max = config.getInt("queue-recommended.mini-bosses");
            }
            returnValue.put(iGroupFinderItem, max);
        }

        return returnValue;
    }
}
