package com.runicrealms.plugin.spellapi.modeled;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

/**
 * Child class of ModeledStand that includes an array of models to loop through
 */
public class ModeledStandAnimated extends ModeledStand {
    private final int[] modelDataArray;
    private int index;

    public ModeledStandAnimated(
            Player player,
            Location location,
            Vector vector,
            int customModelData,
            double duration,
            double hitboxScale,
            StandSlot standSlot,
            Predicate<Entity> filter,
            int[] modelDataArray) {
        super(player, location, vector, customModelData, duration, hitboxScale, standSlot, filter);
        this.index = 0;
        this.modelDataArray = modelDataArray;
    }

    public void incrementAnimationFrame() {
        if (this.index == this.modelDataArray.length - 1) return;
        this.index++;
        ItemMeta meta = this.getItemStack().getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(this.modelDataArray[this.index]);
        }
        this.updateEquipment();
    }
}
