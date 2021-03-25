package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Rejuvenate extends Spell {

    private static final int HEAL_AMT = 45;
    private static final int RANGE = 15;
    private static final int BEAM_SPEED = 3;
    private final double RADIUS = 1.5;
    private final HashMap<UUID, List<UUID>> hasBeenHit;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 2;

    public Rejuvenate() {
        super("Rejuvenate",
                "You launch a beam of healing magic, " +
                "restoring✦ " + HEAL_AMT + " health to yourself and " +
                "all allies it passes through!",
                ChatColor.WHITE, ClassEnum.CLERIC, 12, 25);
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.swingMainHand();

        // heal the caster
        HealUtil.healPlayer(HEAL_AMT, pl, pl, true, false, false);

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);

        // particle effect, spell effects
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(BEAM_SPEED);
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
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE)
                        this.cancel();

                    pl.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.LIME, 1));
                    pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10, 0, 0, 0, 0);
                    allyCheck(pl, location);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }
    }

    // checks for allies near the beam, stops multiple healing of the same player
    @SuppressWarnings("deprecation")
    private void allyCheck(Player pl, Location location) {
        for (Entity e : pl.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
            if (verifyAlly(pl, e)) {
                // a bunch of fancy checks to make sure one player can't be spam healed by the same effect
                // multiple times
                Player ally = (Player) e;
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

                if (ally.getHealth() == ally.getMaxHealth()) {
                    ally.sendMessage(
                            ChatColor.WHITE + pl.getName()
                                    + ChatColor.GRAY + " tried to heal you, but you are at full health.");
                    ally.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);

                } else {
                    HealUtil.healPlayer(HEAL_AMT, ally, pl, true, false, false);
                    pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);

                    // stop the beam if it hits a player
                    break;
                }
            }
        }
    }
}
