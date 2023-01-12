package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Decoy extends Spell {

    private static final int DURATION = 18; // seconds
    private static final int MAX_DIST = 8;
    private final Map<UUID, DecoyStructure> decoyMap = new HashMap<>();

    public Decoy() {
        super("Decoy",
                "You create a decoy of yourself at your target location" +
                        "within " + MAX_DIST + " blocks! The decoy lasts for 18s. " +
                        "At any point, you can shoot the decoy to instantly teleport to its location, " +
                        "destroying the decoy in the process!",
                ChatColor.WHITE, CharacterClass.ARCHER, 24, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
        player.getWorld().playSound(location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
        DecoyStructure decoyStructure = new DecoyStructure(player, location);
        decoyStructure.erectStructure();
        decoyMap.put(player.getUniqueId(), decoyStructure);
    }

    @EventHandler(priority = EventPriority.LOW) // early
    public void onPhysicalDamage(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (event.getHitBlock() == null) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();
        if (!decoyMap.containsKey(player.getUniqueId())) return;
        DecoyStructure decoyStructure = decoyMap.get(player.getUniqueId());
        for (Block block : decoyStructure.getBlocks().keySet()) {
            if (event.getHitBlock().equals(block)) {
                Location location = decoyStructure.getLocation();
                decoyMap.remove(player.getUniqueId());
                decoyStructure.destroy();
                player.teleport(location);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
                final Vector velocity = player.getLocation().getDirection().add(new Vector(0, 0.5, 0)).normalize().multiply(0.5);
                player.setVelocity(velocity);
            }
        }
    }

    /**
     * Creates a scarecrow-type block structure for events
     */
    static class DecoyStructure {
        private final Player player;
        private final Location location;
        private final Hologram hologram;
        private final Map<Block, Material> blocks = new HashMap<>();
        private BukkitTask bukkitTask;

        public DecoyStructure(Player player, Location location) {
            this.player = player;
            this.location = location;
            this.hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location.getBlock().getLocation().clone().add(0.5, 3.5, 0.5));
            hologram.appendTextLine(ChatColor.WHITE + player.getName() + "'s " + ChatColor.YELLOW + "Decoy");
        }

        private void destroy() {
            this.bukkitTask.cancel();
            this.hologram.delete();
            for (Block block : blocks.keySet()) {
                block.setType(blocks.get(block));
            }
        }

        private void erectStructure() {
            blocks.put(location.getBlock(), location.getBlock().getType());
            location.getBlock().setType(Material.OAK_FENCE, false);
            Block chestBlock = location.add(0, 1, 0).getBlock();
            blocks.put(chestBlock, chestBlock.getType());
            chestBlock.setType(Material.TARGET, false);
            Block helmetBlock = location.add(0, 1, 0).getBlock();
            blocks.put(helmetBlock, helmetBlock.getType());
            helmetBlock.setType(Material.JACK_O_LANTERN, false);
            this.bukkitTask = Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), this::destroy, DURATION * 20L);
        }

        public Map<Block, Material> getBlocks() {
            return blocks;
        }

        public Location getLocation() {
            return location;
        }

        public Player getPlayer() {
            return player;
        }
    }
}
