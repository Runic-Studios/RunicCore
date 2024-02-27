package com.runicrealms.plugin.spellapi.modeled;

import com.runicrealms.plugin.RunicCore;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ModeledStand {
    private static final String HITBOX_MODEL_ID = "hitbox";
    private final Player player;
    private final Location location;
    private final Vector vector;
    private final int customModelData;
    private final double duration;
    private final double hitboxScale;
    private final Predicate<Entity> filter;
    private final ModeledEntity modeledEntity;
    private final ArmorStand armorStand;
    private final long startTime;

    /**
     * Creates a ModeledStand, which is used as a projectile library. Using custom resources and ModelEngine,
     * creates moving projectiles with custom hitboxes for use in spell design
     *
     * @param customModelData an integer which is used in the '/models' folder's .json files to specify texture
     * @param duration        the maximum length before the stand will be destroyed
     * @param hitboxScale     a modifier to scale the custom hitbox up or down
     * @param player          who fired/spawned the modeled stand
     * @param vector          that the stand will follow (its velocity)
     */
    public ModeledStand(
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
        this.armorStand = createArmorStand();
        this.modeledEntity = createModeledEntity();
        RunicCore.getModeledStandAPI().addModeledStandToManager(this);
        this.startTime = System.currentTimeMillis();
    }

    private ArmorStand createArmorStand() {
        ItemStack modeledItemStack = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = modeledItemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData); // Set custom model data 2274
            modeledItemStack.setItemMeta(meta);
        }

        ArmorStand armorStand = player.getWorld().spawn(this.location.clone().subtract(0, 0.25f, 0), ArmorStand.class);
        if (armorStand.getEquipment() != null) {
            armorStand.getEquipment().setHelmet(modeledItemStack);
        }
        armorStand.setVisible(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setMarker(false);

        armorStand.teleport(armorStand.getLocation().add(vector));
//        armorStand.setVelocity(this.vector);

        return armorStand;
    }

    private ModeledEntity createModeledEntity() {
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(HITBOX_MODEL_ID);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(this.armorStand);

        if (activeModel != null) {
            activeModel.setHitboxVisible(true);
            activeModel.setHitboxScale(hitboxScale);
            modeledEntity.addModel(activeModel, true);
        }

        return modeledEntity;
    }

    public void destroy() {
        this.modeledEntity.destroy();
        this.armorStand.remove();
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

    public ArmorStand getArmorStand() {
        return armorStand;
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
