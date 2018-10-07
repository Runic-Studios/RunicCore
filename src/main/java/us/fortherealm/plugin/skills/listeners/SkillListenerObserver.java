package us.fortherealm.plugin.skills.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;

import java.util.*;

public class SkillListenerObserver implements Listener {

    // This class exists for the sole purpose of making skills with listeners
    // less of a pain in the ass to write.

    private static Map<SkillListener, Long> activeSkillListeners = new HashMap<>();

    public SkillListenerObserver() {
        RegisteredListener registeredListener =
                new RegisteredListener(
                        this,
                        (listener, event) -> onSkillEvent(event),
                        EventPriority.NORMAL,
                        Main.getInstance(),
                        false);
        for (HandlerList handler : HandlerList.getHandlerLists())
            handler.register(registeredListener);

        removalTask();
    }

    private void onSkillEvent(Event event) {
        Set<SkillListener> safeActiveSkillsMap = this.getActiveSkillListeners().keySet();

        if(safeActiveSkillsMap.size() == 0)
            return;

        for(SkillListener activeSkillListener : safeActiveSkillsMap) {

            // Intellij gets really triggered over this and I could stop that but the "check" already happened in
            // the SkillListener instantiation and it would only waste time during event execution to check again.
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

        new BukkitRunnable() {

            @Override
            public void run() {
                Set<SkillListener> safeActiveSkillsMap = SkillListenerObserver.this.getActiveSkillListeners().keySet();

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

    public static synchronized Map<SkillListener, Long> getActiveSkillListeners() {
        return activeSkillListeners;
    }

    public static synchronized void delActiveSkillListener(SkillListener skillListener) {
        activeSkillListeners.remove(skillListener);
    }

    public static synchronized void addActiveSkillListener(SkillListener skillListener) {
        if(skillListener.timeUntilRemoval() == -1)
            activeSkillListeners.put(
                    skillListener,
                    (long) (System.currentTimeMillis() + SkillListener.MAX_SKILL_DURATION * 1000)
            );
        else
            activeSkillListeners.put(
                    skillListener,
                    Math.min(
                            System.currentTimeMillis() + (long) (skillListener.timeUntilRemoval()*1000),
                            System.currentTimeMillis() + (long) (SkillListener.MAX_SKILL_DURATION)
                    )
            );
    }

}
