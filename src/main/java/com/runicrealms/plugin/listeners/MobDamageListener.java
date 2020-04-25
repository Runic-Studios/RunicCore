package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MobDamageListener implements Listener {

    /**
     * A proper listener!
     */
    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        DamageUtil.damageEntityMob(Math.ceil(e.getAmount()),
                (LivingEntity) e.getVictim(), e.getDamager(), e.shouldApplyMechanics());
    }
}
