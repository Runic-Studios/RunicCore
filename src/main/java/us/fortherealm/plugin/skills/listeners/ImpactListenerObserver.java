package us.fortherealm.plugin.skills.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;

import javax.xml.bind.SchemaOutputResolver;
import java.util.*;

public class ImpactListenerObserver implements Listener {

    // This class exists for the sole purpose of making skills with listeners
    // less of a pain in the ass to write.

    private static Map<ImpactListener, Long> activeSkillListeners = new HashMap<>();

    public ImpactListenerObserver() {
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
        Set<ImpactListener> safeActiveSkillsMap = this.getActiveSkillListeners().keySet();

        if(safeActiveSkillsMap.size() == 0)
            return;

        for(ImpactListener activeImpactListener : safeActiveSkillsMap) {

            // Intellij gets really triggered over this and I could stop that but the "check" already happened in
            // the ImpactListener instantiation and it would only waste time during event execution to check again.
            Class<? extends Event> activeEventClass = activeImpactListener.getEventClass();

            if (activeEventClass == null) {
                System.out.println("Dumby, you gotta implement the getEventClass to something other than null");
                return;
            }
            System.out.println(activeEventClass.getName() + "\t" + event.getEventName());

            if(!(activeEventClass.isInstance(event)))
                continue;

            if(!(activeImpactListener.isPreciseEvent(activeEventClass.cast(event))))
                continue;

            activeImpactListener.initializeSkillVariables(activeEventClass.cast(event));

            SkillImpactEvent impactEvent = new SkillImpactEvent(activeImpactListener.getSkill());
            Bukkit.getServer().getPluginManager().callEvent(impactEvent);

            if(impactEvent.isCancelled())
                return;

            activeImpactListener.doImpact(activeEventClass.cast(event));

            if(activeImpactListener.removeAfterImpact())
                activeSkillListeners.remove(activeImpactListener);

        }
    }

    private void removalTask() {

        new BukkitRunnable() {

            @Override
            public void run() {
                Set<ImpactListener> safeActiveSkillsMap = ImpactListenerObserver.this.getActiveSkillListeners().keySet();

                Long approximateTime = System.currentTimeMillis();
                for(ImpactListener impactListener : safeActiveSkillsMap) {
                    Long removalTime = ImpactListenerObserver.this.getActiveSkillListeners().get(impactListener);
                    if(removalTime == -1)
                        continue;
                    if(removalTime < approximateTime)
                        ImpactListenerObserver.this.delActiveSkillListener(impactListener);
                }
            }

        }.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);

    }

    public static synchronized Map<ImpactListener, Long> getActiveSkillListeners() {
        return activeSkillListeners;
    }

    public static synchronized void delActiveSkillListener(ImpactListener impactListener) {
        activeSkillListeners.remove(impactListener);
    }

    public static synchronized void addActiveSkillListener(ImpactListener impactListener) {
        if(impactListener.timeUntilRemoval() == -1)
            activeSkillListeners.put(
                    impactListener,
                    (long) (System.currentTimeMillis() + ImpactListener.MAX_SKILL_DURATION * 1000)
            );
        else
            activeSkillListeners.put(
                    impactListener,
                    Math.min(
                            System.currentTimeMillis() + (long) (impactListener.timeUntilRemoval()*1000),
                            System.currentTimeMillis() + (long) (ImpactListener.MAX_SKILL_DURATION)
                    )
            );
    }

}
