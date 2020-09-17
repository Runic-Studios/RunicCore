package com.runicrealms.plugin.player.listener;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.item.GearScanner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpeedListener implements Listener {

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        double speed = GearScanner.getSpeedEnchant(e.getPlayer());
        double bonus = 0.2*(speed/100);
        e.getPlayer().setWalkSpeed((float) (0.2 + bonus));
    }
}
