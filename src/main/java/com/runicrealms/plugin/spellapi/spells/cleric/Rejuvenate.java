package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.entity.LivingEntity;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class Rejuvenate extends Spell {

    // grab our globals
    private HashMap<UUID, List<UUID>> hasBeenHit;
    private static final int HEAL_AMT = 20;
    private final int RADIUS = 1;
    private final int RANGE = 15;
    private final int SPEED = 2;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 2;

    // constructor
    public Rejuvenate() {
        super("Rejuvenate",
                "You launch a beam of healing magic," +
                "\nrestoring " + HEAL_AMT + " health to all party members" +
                "\nit passes through.",
                ChatColor.WHITE, 10, 15);
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);

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
                    pl.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.LIME, 1));
                    pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 5, 0, 0, 0, 0);
                    allyCheck(pl, location);
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        }
    }

    // checks for allies near the beam, stops multiple healing of the same player
    @SuppressWarnings("deprecation")
    private void allyCheck(Player pl, Location location) {

        for (Entity e : Objects.requireNonNull
                (location.getWorld()).getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {

            if (!e.getType().isAlive()) return;
            LivingEntity le = (LivingEntity) e;

                if (le != (pl)) {

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

                    // ignore NPCs, additional check for tutorial island
                    if (le.hasMetadata("NPC")) {
                        SpellCastEvent sce = new SpellCastEvent(pl, this, le);
                        Bukkit.getPluginManager().callEvent(sce);
                        if (sce.isCancelled()) return;
//                        if (//todo: tap into quest objective) {
//                            pl.chat("healpass");
                            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
                            le.getWorld().spawnParticle(Particle.HEART, le.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);
//                        }
                        continue;
                    }

                    // heal nobody if we don't have a party
                    if (RunicCore.getPartyManager().getPlayerParty(pl) == null) return;

                    // skip the player if they're not in the party
                    if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                            && !RunicCore.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) { continue; }

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
                        HealUtil.healPlayer(HEAL_AMT, ally, pl);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                        //ally.getWorld().spawnParticle(Particle.HEART, ally.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);

                        // stop the beam if it hits a player
                        break;
                    }
               // }
            }
        }
    }
}
