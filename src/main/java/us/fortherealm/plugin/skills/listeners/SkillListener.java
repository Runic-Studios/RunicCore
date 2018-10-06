package us.fortherealm.plugin.skills.listeners;

import org.bukkit.event.Event;
import us.fortherealm.plugin.skills.Skill;

public interface SkillListener<T extends Event> {

    Class<T> getEventClass();

    boolean isPreciseEvent(T event);

    void initializeSkillVariables(T event);

    Skill getSkill();

    void doImpact(Event cast);

    default double timeUntilRemoval() {
        return -1;
    }

    default boolean removeAfterImpact() {
        return true;
    }
}
