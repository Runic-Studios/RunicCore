package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Consumer;

/**
 * Implements the Bukkit Consumer interface to spawn an armor stand that is invisible
 * and has no displayname to prevent "flickering" of armor stands.
 * @author Skyfallin_
 */
public class InvisStandSpawner implements Consumer<ArmorStand> {

    @Override
    public void accept(ArmorStand armorStand) {

        armorStand.setMetadata("indicator", new FixedMetadataValue(RunicCore.getInstance(), "indicator"));
        armorStand.setVisible(false);
        armorStand.setCollidable(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setCustomName(ChatColor.DARK_GRAY + "");
    }
}
