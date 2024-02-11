package com.runicrealms.plugin.player.death;

import com.runicrealms.plugin.RunicCore;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Gravestone {
    private final int priorityTime; // Seconds
    private final int duration; // Seconds
    private final UUID uuid;
    private final Hologram hologram;
    private final long startTime;
    private final Inventory inventory;
    private final FallingBlock fallingBlock;
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
        this.fallingBlock = createFallingBlock(player, deathLocation);
        this.hologram = buildHologram(player);
        this.priorityTime = priorityTime;
        this.duration = duration;
        RunicCore.getGravestoneManager().getGravestoneMap().put(uuid, this);
    }

    private Hologram buildHologram(Player player) {
        // Spawn the hologram a few blocks above
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(this.fallingBlock.getLocation().add(0, 2.5f, 0));
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
            Location location = this.fallingBlock.getLocation();
            location.getWorld().playSound(location, Sound.ENTITY_SHULKER_SHOOT, 0.5f, 0.2f);
            location.getWorld().spawnParticle(Particle.CLOUD, location, 25, 0.75f, 1.0f, 0.75f, 0);
            this.fallingBlock.remove();
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

    private FallingBlock createFallingBlock(Player player, Location deathLocation) {
        Location gravestoneLocation = deathLocation;
        // Verify the gravestone has a valid location to spawn
        if (gravestoneLocation == null || gravestoneLocation.getWorld() == null) {
            Bukkit.getLogger().severe("A gravestone could not be placed for " + player.getName() + "!");
            player.sendMessage(ChatColor.RED + "Error: Your gravestone could not be placed. Please contact an admin.");
            return null;
        }

        // Center gravestone location in the block
        gravestoneLocation = gravestoneLocation.getBlock().getLocation().add(0.5f, 0, 0.5f);

        // Spawn a FallingBlock that is stationary
        FallingBlock fallingBlock = gravestoneLocation.getWorld().spawnFallingBlock(gravestoneLocation, Material.BASALT.createBlockData());
        fallingBlock.setDropItem(false); // Prevent the block from dropping items
        fallingBlock.setGravity(false); // Make it not affected by gravity if you want it to stay in place
        fallingBlock.setHurtEntities(false); // Prevent the block from hurting entities
        fallingBlock.setInvulnerable(true); // Prevent the entity from taking damage

        return fallingBlock;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Location getLocation() {
        return this.fallingBlock.getLocation();
    }

    public FallingBlock getFallingBlock() {
        return fallingBlock;
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