package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class GroupManager implements Listener {

    private static final String PREFIX = "&2[Group Finder] &6»";

    private final Map<QueueReason, List<Player>> queues;
    private final GroupFinderMainUI ui;
    private final GroupFinderMiniBossUI miniBossUI;
    private final GroupFinderGrindingUI grindingUI;
    private final GroupFinderDungeonUI dungeonUI;

    public static final List<QueueReason> REASONS = new ArrayList<>(getReasons());
    public static final Map<QueueReason, Integer> MAX_MEMBERS = new HashMap<>(getValues());

    public GroupManager() {
        this.queues = new HashMap<>();
        this.ui = new GroupFinderMainUI();
        this.miniBossUI = new GroupFinderMiniBossUI();
        this.grindingUI = new GroupFinderGrindingUI();
        this.dungeonUI = new GroupFinderDungeonUI();

        Set<QueueReason> reasons = new HashSet<>(Arrays.asList(GrindSpots.values()));
        reasons.addAll(Arrays.asList(MiniBosses.values()));
        reasons.addAll(Arrays.asList(Dungeons.values()));
        for (QueueReason reason : reasons) {
            this.queues.put(reason, new ArrayList<>());
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

    public void addToQueue(QueueReason reason, Player player) {
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

    public enum GrindSpots implements QueueReason {

        ROOKIE_BANDITS("&eRookie Bandits &7- lvl 5-10, Lawson’s Farm", 5),
        FOREST_SPIDERS("&eForest Spiders &7- lvl 11-17, Silverwood Forest", 11),
        BARBARIANS("&eBarbarians &7- lvl 18-25, Ruins of Togrund", 18),
        AZANIAN_SOLDIERS("&eAzanian Soldiers &7- lvl 25-30, Haunted Cliffs", 25),
        DESERT_HUSK("&eDesert Husks &7- lvl 31-48, Zenyth Desert", 31),
        HOBGOBLIN("&eHobgoblin &7- lvl 49-59, Orc Outpost", 49),
        INFERNAL_ARMY("&eInfernal Army &7- lvl 60+, Valmyra", 60);

        private final String itemName;
        private final int minLevel;

        GrindSpots(String itemName, int minLevel) {
            this.itemName = ColorUtil.format(itemName);
            this.minLevel = minLevel;
        }

        @Override
        public String getItemName() {
            return this.itemName;
        }

        @Override
        public int getMinLevel() {
            return this.minLevel;
        }
    }

    public enum MiniBosses implements QueueReason {

        BLACK_RIDER("&eBlack Rider &7- Lv. 5+, Silverwood Camp", 5),
        CAVE_MOTHER("&eCave Mother &7- Lv. 5+, Silver Wood Caves", 5),
        IRON_SOLDIER("&eIron Soldier &7- Lv. 8+, Koldorian Mines", 8),
        TOGRUND_THE_BLIGHTED("&eTogrund the Blighted &7- Lv. 14+, Hilstead", 14),
        PHARINDAR("&ePharindar &7- Lv. 20+, Wintervale Outskirts", 20),
        ADMIRAL_VEX("&eAdmiral Vex &7- lvl 20+, Dead Man’s Rest", 20),
        GOLEM_LORD("&eGolem Lord &7- lvl 25+, Tireneas", 25),
        MASTER_FELDRUID("&eMater Feldruid &7- lvl 35+, Tirineas", 35),
        SUN_PRIEST("&eSun Priest &7- lvl 40+, Zenyth", 40),
        PYROMANCER("&ePyromancer &7- Lv. 55+, Valmyra Citadel", 55);

        private final String itemName;
        private final int minLevel;

        MiniBosses(String itemName, int minLevel) {
            this.itemName = ColorUtil.format(itemName);
            this.minLevel = minLevel;
        }

        @Override
        public String getItemName() {
            return this.itemName;
        }

        @Override
        public int getMinLevel() {
            return this.minLevel;
        }
    }

    public enum Dungeons implements QueueReason {

        SEBATHS_CAVE("Gritzgore", "&eSebath’s Cave",
                new String[] {
                        "&7Req Lv &f5+",
                        "&7Location &fSilkwood Forest"
                }, 5),
        CRYSTAL_CAVERN("a_storz", "&eCrystal Cavern",
                new String[] {
                        "&7Req Lv &f12+",
                        "&7Location &fWhaletown"
                }, 12),
        ODINS_KEEP("GoodUHCTipZAKO", "&eOdin’s Keep",
                new String[] {
                        "&7Req Lv &f15+",
                        "&7Location &fHilstead"
                }, 15),
        SUNKEN_LIBRARY("Haku", "&eSunken Library",
                new String[] {
                        "&7Req Lv &f25+",
                        "&7Location &fDead Man's Rest"
                }, 25),
        CRYPTS_OF_DERA("Anubis", "&eCrypts of Dera",
                new String[] {
                        "&7Req Lv &f35+",
                        "&7Location &fZenyth Desert"
                }, 35),
        THE_FROZEN_FORTRESS("adaydremer", "&eThe Frozen Fortress",
                new String[] {
                        "&7Req Lv &f60",
                        "&7Location &fFrost's End"
                }, 60);

        private final String skullPlayerName;
        private final String itemName;
        private final String[] itemDescription;
        private final int minLevel;

        /**
         * Used to create the UI for dungeons in the group finder.
         * @param skullPlayerName name of the boss NPC player skin so its head can be used
         * @param itemName display name of the item
         * @param minLevel min level of dungeon
         */
        Dungeons(String skullPlayerName, String itemName, String[] itemDescription, int minLevel) {
            this.skullPlayerName = skullPlayerName;
            this.itemName = ColorUtil.format(itemName);
            this.itemDescription = itemDescription;
            this.minLevel = minLevel;
        }

        public String getSkullPlayerName() {
            return this.skullPlayerName;
        }

        // todo: add this to interface
        public String[] getItemDescription() {
            return this.itemDescription;
        }

        @Override
        public String getItemName() {
            return this.itemName;
        }

        @Override
        public int getMinLevel() {
            return this.minLevel;
        }
    }

    public interface QueueReason {
        String getItemName();

        int getMinLevel();
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

    private static Map<QueueReason, Integer> getValues() {
        List<QueueReason> reasons = new ArrayList<>(Arrays.asList(GrindSpots.values()));
        reasons.addAll(Arrays.asList(MiniBosses.values()));
        reasons.addAll(Arrays.asList(Dungeons.values()));

        FileConfiguration config = RunicCore.getInstance().getConfig();
        Map<QueueReason, Integer> returnValue = new HashMap<>();

        for (QueueReason reason : reasons) {
            int max;
            if (reason instanceof Dungeons) {
                String name = ((Dungeons)reason).name().replace('_', '-').toLowerCase();
                max = config.getInt("queue-recommended.dungeons." + name);
                returnValue.put(reason, max);
                continue;
            }

            if (reason instanceof GrindSpots) {
                max = config.getInt("queue-recommended.grinding");
            } else {
                max = config.getInt("queue-recommended.mini-bosses");
            }
            returnValue.put(reason, max);
        }

        return returnValue;
    }

    private static List<QueueReason> getReasons() {
        List<QueueReason> reasons = new ArrayList<>(Arrays.asList(GrindSpots.values()));
        reasons.addAll(Arrays.asList(MiniBosses.values()));
        reasons.addAll(Arrays.asList(Dungeons.values()));
        return reasons;
    }
}
