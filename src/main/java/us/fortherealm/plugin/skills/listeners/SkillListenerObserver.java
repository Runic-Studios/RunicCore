package us.fortherealm.plugin.skills.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;

import java.util.*;

public class SkillListenerObserver implements Listener {

    // This class exists for the sole purpose of making skills with listeners
    // less of a pain in the ass to write.

    private static Map<SkillListener, Long> activeSkillListeners = new HashMap<>();
    private static boolean removalTaskIsRunning;

    @EventHandler
    private void onSkillEvent(Event event) {

        // For some reason Spigot sends null events to listeners... wtf?
        if(event == null)
            return;

        Set<SkillListener> safeActiveSkillsMap = this.getActiveSkillListeners().keySet();
        for(SkillListener activeSkillListener : safeActiveSkillsMap) {

            Class<? extends Event> activeEventClass = activeSkillListener.getEventClass();

            if(!(activeEventClass.isInstance(event)))
                continue;

            if(!(activeSkillListener.isPreciseEvent(activeEventClass.cast(event))))
                continue;

            activeSkillListener.initializeSkillVariables(activeEventClass.cast(event));

            SkillImpactEvent impactEvent = new SkillImpactEvent(activeSkillListener.getSkill());
            Bukkit.getServer().getPluginManager().callEvent(impactEvent);

            if(impactEvent.isCancelled())
                return;

            activeSkillListener.doImpact(activeEventClass.cast(event));

            if(activeSkillListener.removeAfterImpact())
                activeSkillListeners.remove(activeSkillListener);

        }
    }

    private void removalTask() {
        if(removalTaskIsRunning)
            return;
        removalTaskIsRunning = true;

        new BukkitRunnable() {

            @Override
            public void run() {
                Set<SkillListener> safeActiveSkillsMap = SkillListenerObserver.this.getActiveSkillListeners().keySet();

                if(safeActiveSkillsMap.isEmpty()) {
                    this.cancel();
                    removalTaskIsRunning = false;
                    return;
                }

                Long approximateTime = System.currentTimeMillis();
                for(SkillListener skillListener : safeActiveSkillsMap) {
                    Long removalTime = SkillListenerObserver.this.getActiveSkillListeners().get(skillListener);
                    if(removalTime == -1)
                        continue;
                    if(removalTime < approximateTime)
                        SkillListenerObserver.this.delActiveSkillListener(skillListener);
                }
            }

        }.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);

    }

    public synchronized Map<SkillListener, Long> getActiveSkillListeners() {
        return activeSkillListeners;
    }

    public synchronized void delActiveSkillListener(SkillListener skillListener) {
        activeSkillListeners.remove(skillListener);
    }

    public synchronized void addActiveSkillListener(SkillListener skillListener) {
        if(skillListener.timeUntilRemoval() == -1)
            activeSkillListeners.put(skillListener, (long) -1);
        else
            activeSkillListeners.put(skillListener, System.currentTimeMillis() + (long) (skillListener.timeUntilRemoval()*1000));

        if(!(removalTaskIsRunning))
            removalTask();

    }

}
