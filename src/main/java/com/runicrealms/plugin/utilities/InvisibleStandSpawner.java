package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Consumer;

/**
 * Implements the Bukkit Consumer interface to spawn an armor stand that is invisible
 * and has no display name to prevent "flickering" of armor stands.
 */
public class InvisibleStandSpawner implements Consumer<ArmorStand> {

    @Override
    public void accept(ArmorStand armorStand) {
        armorStand.setMetadata("indicator", new FixedMetadataValue(RunicCore.getInstance(), "indicator"));
        armorStand.setVisible(false);
        armorStand.setCollidable(false);
//        armorStand.setCustomNameVisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(true);
        armorStand.setMarker(false);
//        armorStand.setCustomName(ChatColor.DARK_GRAY + "");
    }
}
