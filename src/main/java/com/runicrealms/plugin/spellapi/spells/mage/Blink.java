package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Blink extends Spell {

    private static final int MAX_DIST = 8;
    private final Set<UUID> blinkers;

    public Blink() {
        super("Blink",
                "You teleport forward, up to " +
                        "a distance of " + MAX_DIST + " blocks!",
                ChatColor.WHITE, CharacterClass.MAGE, 12, 25);
        blinkers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        blinkers.add(player.getUniqueId()); // prevent fall damage

        Location loc = player.getLocation();
        Block validFinalBlock = null;
        Block currentBlock;

        // make sure the player is blinking to a valid location
        BlockIterator iterator = null;
        try {
            iterator = new BlockIterator(player, MAX_DIST);
        } catch (IllegalStateException e) {
            player.sendMessage(ChatColor.RED + "You cannot blink here!");
        }
        while (iterator.hasNext()) {

            currentBlock = iterator.next();
            Material currentBlockType = currentBlock.getType();

            if (currentBlockType == Material.BARRIER
                    || currentBlock.getRelative(BlockFace.UP).getType() == Material.BARRIER) {
                break;
            }

            if (currentBlockType.isTransparent()) {
                if (currentBlock.getRelative(BlockFace.UP).getType().isTransparent()) {
                    validFinalBlock = currentBlock;
                }
            } else {
                break;
            }
        }

        // create the blink location
        try {
            Location teleportLoc = validFinalBlock.getLocation().clone();
            teleportLoc.add(new Vector(.5, 0, .5));

            // Set the blink location yaw/pitch to the player's
            teleportLoc.setPitch(loc.getPitch());
            teleportLoc.setYaw(loc.getYaw());

            // particles, sounds
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0),
                    10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 5));
            player.getWorld().spawnParticle(Particle.REDSTONE, teleportLoc.add(0, 1, 0),
                    10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 5));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);

            // teleport the player to the blink location
            player.teleport(teleportLoc);
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.FUCHSIA, loc, teleportLoc, 1.0);
            final Vector velocity = player.getLocation().getDirection().add(new Vector(0, 0.5, 0)).normalize().multiply(0.5);
            player.setVelocity(velocity);
        } catch (NullPointerException e) {
            player.sendMessage(ChatColor.RED + "Error: blink location invalid!");
        }
    }

    /**
     * Disable fall damage for players who are lunging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(GenericDamageEvent event) {
        if (!blinkers.contains(event.getVictim().getUniqueId())) return;
        if (event.getCause() == GenericDamageEvent.DamageCauses.FALL_DAMAGE) {
            event.setCancelled(true);
            blinkers.remove(event.getVictim().getUniqueId());
        }
    }
}

