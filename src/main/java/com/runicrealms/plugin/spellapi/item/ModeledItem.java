package com.runicrealms.plugin.spellapi.item;

import com.runicrealms.plugin.RunicCore;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ModeledItem {
    private static final String HITBOX_MODEL_ID = "hitbox";
    private final Player player;
    private final Location location;
    private final Vector vector;
    private final int customModelData;
    private final double duration;
    private final double hitboxScale;
    private final Predicate<Entity> filter;
    private final ModeledEntity modeledEntity;
    private final Item item;
    private final long startTime;

    /**
     * Creates a ModeledItem, which is used as a projectile library. Using custom resources and ModelEngine,
     * creates moving projectiles with custom hitboxes for use in spell design
     *
     * @param customModelData an integer which is used in the '/models' folder's .json files to specify texture
     * @param duration        the maximum length before the item will be destroyed
     * @param hitboxScale     a modifier to scale the custom hitbox up or down
     * @param player          who fired/spawned the modeled item
     * @param vector          that the item will follow (its velocity)
     */
    public ModeledItem(
            Player player,
            Location location,
            Vector vector,
            int customModelData,
            double duration,
            double hitboxScale,
            Predicate<Entity> filter) {
        this.player = player;
        this.location = location;
        this.vector = vector;
        this.customModelData = customModelData;
        this.duration = duration;
        this.hitboxScale = hitboxScale;
        this.filter = filter;
        this.item = initializeItem();
        this.modeledEntity = createModeledEntity();
        RunicCore.getModeledItemAPI().addModeledItemToManager(this);
        this.startTime = System.currentTimeMillis();
    }

    private Item initializeItem() {
        ItemStack modeledItemStack = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = modeledItemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData); // Set custom model data 2274
            modeledItemStack.setItemMeta(meta);
        }

        Item modeledItem = player.getWorld().dropItem(this.location, modeledItemStack);
        modeledItem.setPickupDelay(Integer.MAX_VALUE);
        modeledItem.setGravity(false);
        modeledItem.setVelocity(vector);

        return modeledItem;
    }

    private ModeledEntity createModeledEntity() {
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(HITBOX_MODEL_ID);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(this.item);

        if (activeModel != null) {
            activeModel.setHitboxVisible(true);
            activeModel.setHitboxScale(hitboxScale);
            modeledEntity.addModel(activeModel, true);
        }

        return modeledEntity;
    }

    public void destroy() {
        this.modeledEntity.destroy();
        this.item.remove();
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public double getDuration() {
        return duration;
    }

    public double getHitboxScale() {
        return hitboxScale;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public Vector getVector() {
        return vector;
    }

    public Item getItem() {
        return item;
    }

    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public long getStartTime() {
        return startTime;
    }

    public Predicate<Entity> getFilter() {
        return filter;
    }
}
