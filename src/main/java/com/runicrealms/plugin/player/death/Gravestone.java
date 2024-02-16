package com.runicrealms.plugin.player.death;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.BlocksUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public class Gravestone {
    private static final double HITBOX_SCALE = 1.5;
    private static final String MODEL_ID = "boulder";
    private final int priorityTime; // Seconds
    private final int duration; // Seconds
    private final UUID uuid;
    private final Hologram hologram;
    private final long startTime;
    private final Inventory inventory;
    private final ModeledEntity entity;
    private boolean priority; // False in PvP (anyone can loot)

    /**
     * Creates a Gravestone at the player's death location which holds their items.
     * The player who died has priority over the chest, unless they died in PvP
     *
     * @param player        who died
     * @param deathLocation where the player died
     * @param inventory     containing the items they dropped
     * @param priority      whether the player has loot priority. true if they did not die in PvP
     * @param priorityTime  how long the slain player has priority over their items
     * @param duration      the total time before the gravestone de-spawns
     */
    public Gravestone(Player player, Location deathLocation, Inventory inventory, boolean priority, int priorityTime, int duration) {
        this.uuid = player.getUniqueId();
        this.inventory = inventory;
        this.priority = priority;
        this.startTime = System.currentTimeMillis();
        this.entity = spawnGravestone(deathLocation);
        this.hologram = buildHologram(player);
        this.priorityTime = priorityTime;
        this.duration = duration;
        RunicCore.getGravestoneManager().getGravestoneMap().put(uuid, this);
    }

    private Hologram buildHologram(Player player) {
        // Spawn the hologram a few blocks above the Dummy entity
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(
                this.entity.getBase().getLocation().clone().add(0, 2.0f, 0)
        );
        hologram.getLines().appendText(ChatColor.RED + player.getName() + "'s Gravestone");
        String priorityFormatted = String.format("%dm%ds", priorityTime / 60, 0);
        String durationFormatted = String.format("%dm%ds", duration / 60, 0);
        hologram.getLines().appendText(org.bukkit.ChatColor.YELLOW + "Priority: " + priorityFormatted); // Add the updated line
        hologram.getLines().appendText(org.bukkit.ChatColor.GRAY + "Time left: " + durationFormatted); // Add the updated line
        // Link the hologram to this gravestone
        return hologram;
    }

    /**
     * Remove the entity
     *
     * @param dropItems true if gravestone should drop its items
     */
    public void collapse(boolean dropItems) {
        this.hologram.delete();
        try {
            Location location = this.entity.getBase().getLocation();
            location.getWorld().playSound(location, Sound.ENTITY_SHULKER_SHOOT, 0.5f, 0.2f);
            location.getWorld().spawnParticle(Particle.CLOUD, location, 25, 0.75f, 1.0f, 0.75f, 0);
            this.entity.destroy();
            // If the player dies while a gravestone is active, gravestone will collapse and drop their items
            if (dropItems) {
                for (ItemStack itemStack : this.inventory.getContents()) {
                    if (itemStack == null) continue;
                    location.getWorld().dropItem(location, itemStack);
                }
            }
        } catch (Exception exception) {
            Bukkit.getLogger().severe("There was an error de-spawning a gravestone for player " + this.uuid);
        }
    }

    /**
     * Spawns a gravestone at the given location by spawning a Dummy base entity and a
     * boulder model
     *
     * @param deathLocation to spawn the entity
     * @return a modeled entity
     */
    public ModeledEntity spawnGravestone(Location deathLocation) {
        Location gravestoneLocation = BlocksUtil.findNearestValidBlock(deathLocation, 5, Set.of(Material.AIR, Material.WATER));
        // Verify the gravestone has a valid location to spawn
        if (gravestoneLocation == null || gravestoneLocation.getWorld() == null) {
            Bukkit.getLogger().severe("A gravestone could not be placed!");
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Error: Your gravestone could not be placed. Please contact an admin.");
            }
            return null;
        }

        // Center gravestone location in the block
        gravestoneLocation = gravestoneLocation.getBlock().getLocation().add(0.5f, 0, 0.5f);

        // Spawn a base entity
        Dummy<?> dummy = new Dummy<>();
        dummy.setLocation(gravestoneLocation);

        ActiveModel activeModel = ModelEngineAPI.createActiveModel(MODEL_ID);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);

        if (activeModel != null) {
            activeModel.setHitboxVisible(true);
            activeModel.setHitboxScale(HITBOX_SCALE);
            modeledEntity.addModel(activeModel, true);
        }

        return modeledEntity;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Location getLocation() {
        return this.entity.getBase().getLocation();
    }

    public ModeledEntity getEntity() {
        return entity;
    }

    public long getStartTime() {
        return startTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean hasPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public int getPriorityTime() {
        return this.priorityTime;
    }

    public int getDuration() {
        return this.duration;
    }
}