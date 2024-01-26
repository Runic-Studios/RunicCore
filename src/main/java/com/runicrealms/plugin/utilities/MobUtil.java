package com.runicrealms.plugin.utilities;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

import java.util.UUID;

public class MobUtil {

    /**
     * Returns true if the specified entity id is an active MythicMob with the 'Boss' faction
     *
     * @param entityId id of entity to check
     * @return true if boss, else false
     */
    public static boolean isBoss(UUID entityId) {
        // No boss knockback!
        if (MythicBukkit.inst().getMobManager().getActiveMob(entityId).isPresent()) {
            ActiveMob am = MythicBukkit.inst().getMobManager().getActiveMob(entityId).get();
            return am.hasFaction() && am.getFaction().equalsIgnoreCase("boss");
        }
        return false;
    }
}
