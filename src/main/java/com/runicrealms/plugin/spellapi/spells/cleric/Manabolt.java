package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Manabolt extends Spell {

    // grab our globals
    private HashMap<UUID, List<UUID>> hasBeenHit;
    private static final int MANA_AMT = 25;
    private final int RADIUS = 1;
    private final int RANGE = 15;
    //private final double BEAM_WIDTH = 2.2;
    private final int SPEED = 2;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 2;

    // constructor
    public Manabolt() {
        super("Manabolt",
                "You launch a beam of soothing magic," +
                        "\nrestoring " + MANA_AMT + " mana to all party members" +
                        "\nit passes through.",
                ChatColor.WHITE, 1, 10);
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1.5f);

        // particle effect, spell effects
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(SPEED);
        startTask(pl, new Vector[]{middle});
    }

    // particle effect
    private void startTask(Player pl, Vector[] vectors) {
        for (Vector vector : vectors) {
            new BukkitRunnable() {
                Location location = pl.getEyeLocation();
                Location startLoc = pl.getLocation();

                @Override
                public void run() {
                    location.add(vector);
                    // 10 block range before spell dies out naturally
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                        this.cancel();
                    }
                    pl.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.NAVY, 1));
                    pl.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.BLUE, 1));
                    allyCheck(pl, location);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }
    }

    // checks for allies near the beam, stops multiple healing of the same player
    private void allyCheck(Player pl, Location location) {

        for (Entity en : Objects.requireNonNull
                (location.getWorld()).getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {

            if (!(en instanceof Player)) return;

            // only listen for players
            Player ally = (Player) en;

            //if (ally.getLocation().distance(location) <= BEAM_WIDTH) {

                if (ally == pl) continue;

                // skip the player if we've got a party and they're not in it
                if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                        && !RunicCore.getPartyManager().getPlayerParty(pl).hasMember(ally.getUniqueId())) {
                    continue;
                }

                // a bunch of fancy checks to make sure one player can't be spam healed by the same effect
                // multiple times
                if (hasBeenHit.containsKey(ally.getUniqueId())) {
                    List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                    if (uuids.contains(pl.getUniqueId())) {
                        break;
                    } else {
                        uuids.add(pl.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }
                } else {
                    List<UUID> uuids = new ArrayList<>();
                    uuids.add(pl.getUniqueId());
                    hasBeenHit.put(ally.getUniqueId(), uuids);
                }

                // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                        uuids.remove(pl.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }
                }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));

                // ignore NPCs, additional check for tutorial island
                if (ally.hasMetadata("NPC")) {
                    continue;
                }

                // sounds
                pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                ally.playSound(ally.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1.5f);

                ally.getWorld().spawnParticle(Particle.REDSTONE, ally.getEyeLocation(),
                        1, 0.25f, 0.25f, 0.25f, new Particle.DustOptions(Color.NAVY, 2));

                RunicCore.getManaManager().addMana(ally, MANA_AMT);
           // }
        }
    }
}
