package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BaseStatListener implements Listener {

    private static final float DEFAULT_WALKSPEED = 0.2f;

    @EventHandler
    public void onStatChangeEvent(StatChangeEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double walkBonusPercent = BaseStatEnum.getMovementSpeedMult() * RunicCoreAPI.getPlayerDexterity(uuid);
        if (walkBonusPercent <= 0) return;
        e.getPlayer().setWalkSpeed((float) (DEFAULT_WALKSPEED + (DEFAULT_WALKSPEED * walkBonusPercent)));
    }

    @EventHandler
    public void onRangedDamage(WeaponDamageEvent e) {
        if (!e.isRanged()) return;
        UUID uuid = e.getPlayer().getUniqueId();
        double bonusPercent = BaseStatEnum.getRangedDmgMult() * RunicCoreAPI.getPlayerDexterity(uuid);
        e.setAmount((int) (e.getAmount() + (e.getAmount() * bonusPercent)));
    }
}
