package us.fortherealm.plugin.professions;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import java.util.*;

public class ProfManager {

    // globals
    private Main plugin = Main.getInstance();
    private ArrayList<Player> currentCrafters = new ArrayList<>();
    private HashMap<Block, UUID> ores = new HashMap<>();

    // constructor
    public ProfManager() {
        this.startRegenTask();
    }

    // starts the repeating task to regenerate farms, ores, trees
    private void startRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

//                for (Player online : Bukkit.getOnlinePlayers()) {
//                    online.sendMessage("test");
//                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 20, 200);//1 sec delay, 10 sec intervals
    }

    // todo: add particles tasks for ores, anvils, farms, trees, and fish (oh, dear)
    public ArrayList<Player> getCurrentCrafters() {
        return currentCrafters;
    }
}
