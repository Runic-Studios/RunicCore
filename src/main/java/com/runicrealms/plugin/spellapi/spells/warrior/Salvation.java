package com.runicrealms.plugin.spellapi.spells.warrior;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Salvation extends Spell implements DistanceSpell, DurationSpell, HealingSpell {
    private final Map<Block, BellTask> blockMap = new HashMap<>();
    private double distance;
    private double duration;
    private double heal;
    private double healingPerLevel;

    public Salvation() {
        super("Salvation", CharacterClass.WARRIOR);
        this.setDescription("You fire a beam of light up to " + distance + " blocks away " +
                "that summons an enchanted bell for " + duration + "s! " +
                "Allies can right-click the bell to be teleported to you. " +
                "You and the teleported ally gain (" + heal + " + &f" +
                healingPerLevel + "x&7 lvl) health this way. " +
                "You can also activate the bell to heal this amount and destroy the bell.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        Location location = player.getTargetBlock(null, (int) distance).getLocation();
        if (location.getBlock().getType() != Material.AIR)
            location.add(0, 1, 0); // Try to prevent bell spawning in floor
        VectorUtil.drawLine(player, Particle.VILLAGER_HAPPY, Color.WHITE, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 8, 0.5f, 0.5f, 0.5f, 0);
        spawnBell(player, location);
    }

    private Location findNearestAir(Location location) {
        Location bestLocation = null;
        double minDistanceSquared = Double.MAX_VALUE;

        for (int y = 0; y <= 3; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    Block currentBlock = location.clone().add(x, y, z).getBlock();
                    if (currentBlock.getType() == Material.AIR) {
                        double distanceSquared = location.distanceSquared(currentBlock.getLocation());
                        if (distanceSquared < minDistanceSquared) {
                            minDistanceSquared = distanceSquared;
                            bestLocation = currentBlock.getLocation();
                        }
                    }
                }
            }


        }

        return bestLocation;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getHeal() {
        return heal;
    }

    @Override
    public void setHeal(double heal) {
        this.heal = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

    @EventHandler
    public void onBellRung(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (blockMap.isEmpty()) return;
        if (!(event.getHand() == EquipmentSlot.HAND)) return;
        if (event.getClickedBlock() == null) return;
        if (action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.BELL) return;
        Optional<Block> optional = blockMap.keySet().stream().filter(block1 -> block1.equals(event.getClickedBlock())).findFirst();
        if (optional.isEmpty()) return;
        Block block = optional.get();
        Player caster = Bukkit.getPlayer(blockMap.get(block).getCasterUUID());
        if (caster == null) return;
        if (!isValidAlly(caster, player)) return; // Only allies can click the bell
        player.getWorld().playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_BELL_USE, 0.5f, 2.0f);
        // Handle the bell ringing event
        healPlayer(caster, caster, heal, this);
        // Destroy bell
        blockMap.get(block).getBukkitTask().cancel();
        blockMap.get(block).execute();
        // An ally clicked the bell (NOT the caster)
        if (!player.getUniqueId().equals(blockMap.get(block).getCasterUUID())) {
            healPlayer(caster, player, heal, this);
            player.teleport(caster);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
        }
    }

    /**
     * Spawns a bell block with a task to remove it. Tries to find the 'best' location in a 3 block
     * radius
     *
     * @param location to spawn the block
     */
    private void spawnBell(Player caster, Location location) {
        Location bestLocation = findNearestAir(location);
        if (bestLocation == null) { // Couldn't find a nearby air block
            caster.sendMessage(ChatColor.RED + "A valid location could not be found!");
            caster.playSound(caster.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            return;
        }
        Material oldMaterial = bestLocation.getBlock().getType();
        bestLocation.getBlock().setType(Material.BELL, false);
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), bestLocation.clone().add(0.5, 2.5, 0.5));
        hologram.appendTextLine(ChatColor.WHITE + caster.getName() + "'s " + ChatColor.GRAY + "Bell");
        caster.getWorld().playSound(location, Sound.BLOCK_BELL_USE, 0.5f, 1.0f);
        BellTask bellTask = new BellTask(caster.getUniqueId(), hologram, bestLocation, oldMaterial, duration);
        blockMap.put(bestLocation.getBlock(), bellTask);
    }

    /**
     * Used to keep track of the Radiant Fire stack refresh task.
     * Uses AtomicInteger to be thread-safe
     */
    static class BellTask {
        private final UUID casterUUID;
        private final Hologram hologram;
        private final Location bestLocation;
        private final Material oldMaterial;
        private final BukkitTask bukkitTask;

        public BellTask(UUID casterUUID, Hologram hologram, Location bestLocation, Material oldMaterial, double duration) {
            this.casterUUID = casterUUID;
            this.hologram = hologram;
            this.bestLocation = bestLocation;
            this.oldMaterial = oldMaterial;
            this.bukkitTask = Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                hologram.delete();
                bestLocation.getBlock().setType(oldMaterial);
            }, (int) duration * 20L);

        }

        private void execute() {
            hologram.delete();
            bestLocation.getBlock().setType(oldMaterial);
        }

        public BukkitTask getBukkitTask() {
            return bukkitTask;
        }

        public UUID getCasterUUID() {
            return casterUUID;
        }
    }

}

