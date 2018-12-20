package us.fortherealm.plugin.professions;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import java.util.HashMap;
import java.util.UUID;

public class ProfManager {

    // globals
    private Main plugin = Main.getInstance();
    private static HashMap<Block, UUID> ores = new HashMap<>();

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
}
