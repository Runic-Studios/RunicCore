package us.fortherealm.plugin.skills.listeners;

import org.bukkit.event.Event;
import us.fortherealm.plugin.skills.Skill;

public interface ImpactListener<T extends Event> {

    // Maximum duration a skill can exist on the skillObserverList
    double MAX_SKILL_DURATION = 15;

    // If I were a reflection wizard this part wouldn't be necessary. For now though,
    // I'm going to keep it and spend time doing other things.
    Class<T> getEventClass();

    Skill getSkill();

    boolean isPreciseEvent(T event);

    void initializeSkillVariables(T event);

    void doImpact(T event);

    default void onRemoval() { return; }

    default double timeUntilRemoval() {
        return -1;
    }

    default boolean removeAfterImpact() {
        return true;
    }
}
