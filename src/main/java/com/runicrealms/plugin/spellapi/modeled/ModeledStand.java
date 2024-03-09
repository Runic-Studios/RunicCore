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
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ModeledStand {
    protected static final String HITBOX_MODEL_ID = "hitbox";
    protected final Player player;
    protected final Location location;
    protected final Vector vector;
    protected final int customModelData;
    protected final double duration;
    protected final double hitboxScale;
    protected final StandSlot standSlot;
    protected final Predicate<Entity> validTargets;
    protected final ItemStack itemStack;
    protected final ArmorStand armorStand;
    protected final ModeledEntity modeledEntity;
    protected long startTime;

    /**
     * Creates a ModeledStand, which is used as a projectile library. Using custom resources and ModelEngine,
     * creates moving projectiles with custom hitboxes for use in spell design
     *
     * @param player          who fired/spawned the modeled stand
     * @param location        where the stand will spawn
     * @param customModelData an integer which is used in the '/models' folder's .json files to specify texture
     * @param duration        the maximum length before the stand will be destroyed
     * @param hitboxScale     a modifier to scale the custom hitbox up or down
     * @param vector          that the stand will follow (its velocity)
     * @param standSlot       whether the item will spawn ARM or HEAD
     * @param validTargets    a predicate filter to specify valid targets (allies or enemies)
     */
    public ModeledStand(
            Player player,
            Location location,
            Vector vector,
            int customModelData,
            double duration,
            double hitboxScale,
            StandSlot standSlot,
            Predicate<Entity> validTargets) {
        this.player = player;
        this.location = location;
        this.vector = vector;
        this.customModelData = customModelData;
        this.duration = duration;
        this.hitboxScale = hitboxScale;
        this.standSlot = standSlot;
        this.validTargets = validTargets;
        this.itemStack = createItemStack();
        this.armorStand = createArmorStand();
        this.modeledEntity = createModeledEntity();
        RunicCore.getModeledStandAPI().addModeledStandToManager(this);
        this.startTime = System.currentTimeMillis();
    }

    private ItemStack createItemStack() {
        ItemStack modeledItemStack = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = modeledItemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            modeledItemStack.setItemMeta(meta);
        }
        return modeledItemStack;
    }

    protected ArmorStand createArmorStand() {
        ArmorStand armorStand = player.getWorld()
                .spawn(this.location.clone().setDirection(player.getLocation().getDirection())
                        .subtract(0, 0.25f, 0), ArmorStand.class);

        armorStand.setVisible(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setMarker(false);

        if (standSlot == StandSlot.ARM) {
            armorStand.setArms(true);
            // Set the right arm pose to (0, 0, 0)
            armorStand.setRightArmPose(new EulerAngle(0, 0, 0));
        }

        this.updateEquipment(armorStand);
        armorStand.teleport(armorStand.getLocation().add(vector));

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

    public void updateEquipment(ArmorStand armorStand) {
        if (armorStand.getEquipment() != null) {
            if (standSlot == StandSlot.HEAD) {
                armorStand.getEquipment().setHelmet(this.itemStack);
            } else {
                armorStand.getEquipment().setItemInMainHand(this.itemStack);
            }
        }
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

    public StandSlot getStandSlot() {
        return standSlot;
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

    public ItemStack getItemStack() {
        return itemStack;
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

    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }

    public Predicate<Entity> getValidTargets() {
        return validTargets;
    }
}
