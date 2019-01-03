package us.fortherealm.plugin.level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class ExpListener implements Listener {

    // stops mobs, players from dropping experience orbs, since the exp bar will likely be used for
    // quests or as a timer or something.
    @EventHandler
    public void onExpDrop(EntityDeathEvent e) {
        e.setDroppedExp(0);
    }
}
