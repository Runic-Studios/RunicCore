package com.runicrealms.plugin.spellapi.statuseffects;

import com.runicrealms.plugin.events.RunicDamageEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A method that is called when an entity takes damage from bleeding
 *
 * @author BoBoBalloon
 */
public class EntityBleedEvent extends RunicDamageEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private double levelMultiplier;

    public EntityBleedEvent(@NotNull LivingEntity target) {
        super(target, 1);
        this.levelMultiplier = .25;
    }

    public double getLevelMultiplier() {
        return this.levelMultiplier;
    }

    public void setLevelMultiplier(double levelMultiplier) {
        this.levelMultiplier = levelMultiplier;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
