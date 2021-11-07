package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Rejuvenate extends Spell implements HealingSpell {

    private static final int HEAL_AMT = 20;
    private static final int DURATION = 6;
    private static final int RANGE = 15;
    private static final int BEAM_SPEED = 3;
    private static final int SUCCESSIVE_COOLDOWN = 2; // seconds
    private static final double HEALING_PER_LEVEL = 1.25;
    private static final double PERCENT = 35;
    private static final double RADIUS = 1.5;
    private final HashMap<UUID, List<UUID>> hasBeenHit;
    private final HashMap<UUID, HashSet<UUID>> affectedPlayers;

    public Rejuvenate() {
        super("Rejuvenate",
                "You launch a beam of healing magic, " +
                        "restoring✦ (" + HEAL_AMT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) health to yourself and all allies it passes through! " +
                        "Affected allies receive an additional " + (int) PERCENT + "% health " +
                        "from your healing✦ spells for " + DURATION + "s! ",
                ChatColor.WHITE, ClassEnum.CLERIC, 12, 25);
        hasBeenHit = new HashMap<>();
        affectedPlayers = new HashMap<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.swingMainHand();
        HealUtil.healPlayer(HEAL_AMT, pl, pl, false, this);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
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

    /*
     checks for allies near the beam, stops multiple healing of the same player
     */
    private void allyCheck(Player caster, Location location) {
        HashSet<UUID> allies = new HashSet<>();
        affectedPlayers.put(caster.getUniqueId(), allies);
        for (Entity entity : caster.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
            if (verifyAlly(caster, entity)) {
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

                    HealUtil.healPlayer(HEAL_AMT, ally, caster, false, this);
                    caster.playSound(caster.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                    affectedPlayers.get(caster.getUniqueId()).add(entity.getUniqueId());
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> affectedPlayers.remove(caster.getUniqueId()), DURATION * 20L);
                    break; // stop the beam if it hits a player
                }
            }
        }
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    /**
     * Players receive extra healing from caster (excludes caster)
     */
    @EventHandler
    public void onHeal(SpellHealEvent e) {
        Player caster = e.getPlayer();
        if (!affectedPlayers.containsKey(caster.getUniqueId())) return;
        if (!affectedPlayers.get(caster.getUniqueId()).contains(e.getEntity().getUniqueId())) return;
        double percent = PERCENT / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        if (extraAmt < 1) extraAmt = 1;
        e.setAmount(e.getAmount() + extraAmt);
    }
}
