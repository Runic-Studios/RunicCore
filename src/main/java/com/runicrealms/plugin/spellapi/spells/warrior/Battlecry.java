package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Battlecry extends Spell {

    private final HashMap<UUID, List<UUID>> hasBeenHit;
    private static final int SHIELD_AMT = 35;
    private static final double RADIUS = 2;
    private static final int RANGE = 10;
    private static final int SPEED = 3;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 2;

    public Battlecry() {
        super("Battlecry",
                "You unleash a rallying cry" +
                        "\nthat travels up to " + RANGE + " blocks!" +
                        "\nAllies hit by the battlecry" +
                        "\nare shielded■ for " + SHIELD_AMT + " health!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 15, 30);
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.swingMainHand();

        // heal the caster
        HealUtil.shieldPlayer(SHIELD_AMT, pl, pl, true, false, false);

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);

        // particle effect, spell effects
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(SPEED);
        startTask(pl, new Vector[]{middle});
    }

    // particle effect
    private void startTask(Player pl, Vector[] vectors) {
        for (Vector vector : vectors) {
            new BukkitRunnable() {

                final Location location = pl.getEyeLocation();
                final Location startLoc = pl.getLocation();

                @Override
                public void run() {
                    location.add(vector);
                    // 10 block range before spell dies out naturally
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                        this.cancel();
                    }
                    pl.getWorld().spawnParticle(Particle.CLOUD, location, 10, 0.2f, 0.2f, 0.2f, 0);
                    allyCheck(pl, location);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }
    }

    // checks for allies near the beam, stops multiple healing of the same player
    private void allyCheck(Player pl, Location location) {

        for (Entity e : Objects.requireNonNull
                (location.getWorld()).getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {

            if (!e.getType().isAlive()) return;
            LivingEntity le = (LivingEntity) e;

            if (le == (pl)) {
                continue;
            }

            // only listen for players
            if (!(le instanceof Player)) return;

            // a bunch of fancy checks to make sure one player can't be spam healed by the same effect
            // multiple times
            Player ally = (Player) le;
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

            // ignore NPCs
            if (le.hasMetadata("NPC")) continue;

            // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
            new BukkitRunnable() {
                @Override
                public void run() {
                    List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                    uuids.remove(pl.getUniqueId());
                    hasBeenHit.put(ally.getUniqueId(), uuids);
                }
            }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));


            HealUtil.shieldPlayer(SHIELD_AMT, ally, pl, true, false, false);
        }
    }
}
