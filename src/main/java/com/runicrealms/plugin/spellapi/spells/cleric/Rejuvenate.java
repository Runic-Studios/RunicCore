package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Rejuvenate extends Spell implements DurationSpell, HealingSpell, RadiusSpell {
    private static final int RANGE = 15;
    private static final int BEAM_SPEED = 3;
    private static final int SUCCESSIVE_COOLDOWN = 2; // seconds
    private static final double PERCENT = 35;
    private final HashMap<UUID, List<UUID>> hasBeenHit;
    private final HashMap<UUID, HashSet<UUID>> affectedPlayers;
    private double heal;
    private double duration;
    private double healingPerLevel;
    private double radius;

    public Rejuvenate() {
        super("Rejuvenate", CharacterClass.CLERIC);
        hasBeenHit = new HashMap<>();
        affectedPlayers = new HashMap<>();
        this.setDescription("You launch a beam of healing magic, " +
                "restoring✦ (" + heal + " + &f" + healingPerLevel +
                "x&7 lvl) health to yourself and all allies it passes through! " +
                "Affected allies receive an additional " + (int) PERCENT + "% health " +
                "from your healing✦ spells for " + duration + "s!");
    }

    /*
     checks for allies near the beam, stops multiple healing of the same player
     */
    private void allyCheck(Player caster, Location location) {
        HashSet<UUID> allies = new HashSet<>();
        affectedPlayers.put(caster.getUniqueId(), allies);
        for (Entity entity : caster.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (isValidAlly(caster, entity)) {
                // a bunch of fancy checks to make sure one player can't be spam healed by the same effect
                // multiple times
                Player ally = (Player) entity;
                if (hasBeenHit.containsKey(ally.getUniqueId())) {
                    List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                    if (uuids.contains(caster.getUniqueId())) {
                        break;
                    } else {
                        uuids.add(caster.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }
                } else {
                    List<UUID> uuids = new ArrayList<>();
                    uuids.add(caster.getUniqueId());
                    hasBeenHit.put(ally.getUniqueId(), uuids);
                }

                // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                        uuids.remove(caster.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }
                }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));

                if (ally.getHealth() == ally.getMaxHealth()) {
                    ally.sendMessage(
                            ChatColor.WHITE + caster.getName()
                                    + ChatColor.GRAY + " tried to heal you, but you are at full health.");
                    ally.playSound(caster.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);

                } else {

                    healPlayer(caster, ally, heal, this);
                    caster.playSound(caster.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                    affectedPlayers.get(caster.getUniqueId()).add(entity.getUniqueId());
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> affectedPlayers.remove(caster.getUniqueId()), (int) duration * 20L);
                    break; // stop the beam if it hits a player
                }
            }
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        healPlayer(player, player, heal, this);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize().multiply(BEAM_SPEED);
        startTask(player, new Vector[]{middle});
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Players receive extra healing from caster (excludes caster)
     */
    @EventHandler
    public void onHeal(SpellHealEvent event) {
        Player caster = event.getPlayer();
        if (!affectedPlayers.containsKey(caster.getUniqueId())) return;
        if (!affectedPlayers.get(caster.getUniqueId()).contains(event.getEntity().getUniqueId()))
            return;
        double percent = PERCENT / 100;
        int extraAmt = (int) (event.getAmount() * percent);
        if (extraAmt < 1) extraAmt = 1;
        event.setAmount(event.getAmount() + extraAmt);
    }

    // particle effect
    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            new BukkitRunnable() {
                final Location location = player.getEyeLocation();
                final Location startLoc = player.getLocation();

                @Override
                public void run() {
                    location.add(vector);
                    if (location.getBlock().getType().isSolid() || location.distanceSquared(startLoc) >= RANGE * RANGE) {
                        this.cancel();
                        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 15, 0.5f, 0.5f, 0.5f, 0);
                    }
                    player.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.LIME, 1));
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10, 0.1f, 0.1f, 0.1f, 0);
                    allyCheck(player, location);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }
    }
}
