package com.runicrealms.plugin.player.death;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.BlocksUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Gravestone {
    private final int priorityTime; // Seconds
    private final int duration; // Seconds
    private final UUID uuid;
    private final Location location;
    private final Hologram hologram;
    private final long startTime;
    private final Inventory inventory;
    private final ShulkerBox shulkerBox;
    private boolean priority; // False in PvP (anyone can loot)

    /**
     * Creates a Gravestone at the player's death location which holds their items.
     * The player who died has priority over the chest, unless they died in PvP
     *
     * @param player    who died
     * @param inventory containing the items they dropped
     * @param priority  true if the player did not die in PvP
     */
    public Gravestone(Player player, Inventory inventory, boolean priority, int priorityTime, int duration) {
        this.uuid = player.getUniqueId();
        this.location = player.getLocation();
        this.inventory = inventory;
        this.priority = priority;
        this.startTime = System.currentTimeMillis();
        this.shulkerBox = spawnGravestone(player);
        this.hologram = buildHologram(player);
        this.priorityTime = priorityTime;
        this.duration = duration;
        RunicCore.getGravestoneManager().getGravestoneMap().put(uuid, this);
    }

    /**
     * @param location specify exact location to spawn gravestone. useful if player combat logged
     */
    public Gravestone(Player player, Location location, Inventory inventory, boolean priority, int priorityTime, int duration) {
        this.uuid = player.getUniqueId();
        this.location = location;
        this.inventory = inventory;
        this.priority = priority;
        this.startTime = System.currentTimeMillis();
        this.shulkerBox = spawnGravestone(player);
        this.hologram = buildHologram(player);
        this.priorityTime = priorityTime;
        this.duration = duration;
        RunicCore.getGravestoneManager().getGravestoneMap().put(uuid, this);
    }

    private Hologram buildHologram(Player player) {
        // Spawn the hologram a few blocks above
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(shulkerBox.getLocation().add(0.5f, 2.5f, 0.5f));
        hologram.getLines().appendText(ChatColor.RED + player.getName() + "'s Gravestone");
        String priorityFormatted = String.format("%dm%ds", priorityTime / 60, 0);
        String durationFormatted = String.format("%dm%ds", duration / 60, 0);
        hologram.getLines().appendText(org.bukkit.ChatColor.YELLOW + "Priority: " + priorityFormatted); // Add the updated line
        hologram.getLines().appendText(org.bukkit.ChatColor.GRAY + "Time left: " + durationFormatted); // Add the updated line
        // Link the hologram to this gravestone
        return hologram;
    }

    public void collapse(boolean dropItems) {
        this.hologram.delete();
        shulkerBox.getWorld().playSound(shulkerBox.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 0.5f, 0.2f);
        shulkerBox.getWorld().spawnParticle(Particle.CLOUD, shulkerBox.getLocation(), 25, 0.75f, 1.0f, 0.75f, 0);
        shulkerBox.getLocation().getBlock().setType(Material.AIR);
        if (dropItems) {
            for (ItemStack itemStack : this.inventory.getContents()) {
                if (itemStack == null) continue;
                shulkerBox.getWorld().dropItem(this.location, itemStack);
            }
        }
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Location getLocation() {
        return location;
    }

    public ShulkerBox getShulkerBox() {
        return shulkerBox;
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

    private ShulkerBox spawnGravestone(Player player) {
        // 1. Get the player's death location
        Location gravestoneLocation = BlocksUtil.findNearestAir(this.location, 5);
        if (gravestoneLocation == null) {
            Bukkit.getLogger().severe("A gravestone could not be placed for " + player.getName() + "!");
            player.sendMessage(ChatColor.RED + "Error: Your gravestone could not be placed. Please contact an admin.");
            return null;
        }
        // 2. Set the block at the death location to a shulker box
        gravestoneLocation.getBlock().setType(Material.LIGHT_GRAY_SHULKER_BOX);
        BlockState blockState = gravestoneLocation.getBlock().getState();
        // Ensure the blockState is of ShulkerBox
        if (blockState instanceof ShulkerBox shulkerBox) {
            // 3. Load the player's items into the shulker box
            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    RunicItemsAPI.addItem(shulkerBox.getInventory(), item);
                }
            }
            // Update the block with the inventory
            shulkerBox.update();
            return shulkerBox;
        }
        return null;
    }

    public int getPriorityTime() {
        return this.priorityTime;
    }

    public int getDuration() {
        return this.duration;
    }

}
