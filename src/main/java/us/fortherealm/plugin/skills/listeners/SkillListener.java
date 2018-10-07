package us.fortherealm.plugin.skills.listeners;

import org.bukkit.event.Event;
import us.fortherealm.plugin.skills.Skill;

public interface SkillListener<T extends Event> {

    // Maximum duration a skill can exist on the skillObserverList
    double MAX_SKILL_DURATION = 15;

    // If I were a reflection wizard this part wouldn't be necessary. For now though,
    // I'm going to keep it and spend time doing other things.
    Class<T> getEventClass();

    boolean isPreciseEvent(T event);

    void initializeSkillVariables(T event);

    Skill getSkill();

    void doImpact(T event);

    default double timeUntilRemoval() {
        return -1;
    }

    default boolean removeAfterImpact() {
        return true;
    }
}
