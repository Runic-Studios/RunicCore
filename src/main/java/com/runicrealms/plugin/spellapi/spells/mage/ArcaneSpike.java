package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ArcaneSpike extends Spell {

    // globals
    private static final int DAMAGE_AMOUNT = 20;
    private static final int BEAM_LENGTH = 24;
    private static final int RADIUS = 1;
    private HashMap<UUID, List<UUID>> hasBeenHit;

    // constructor
    public ArcaneSpike() {
        super("Arcane Spike",
                "You launch three beams of arcane magic!" +
                "\nEach beam deals " + DAMAGE_AMOUNT + " spellʔ damage to" +
                "\nenemies it passes through.",
                ChatColor.WHITE, 6, 10);
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        // sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.0f);

        // create three beams
        Vector middle = pl.getEyeLocation().getDirection().normalize();
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector right = rotateVectorAroundY(middle, 22.5);

        // begin particle effect & entity check tasks
        startTask(pl, new Vector[]{middle, left, right});
    }

    // particles, vectors
    private void startTask(Player player, Vector[] vectors) {
        for(Vector vector : vectors) {
            Location location = player.getEyeLocation();
            for (double t = 0; t < BEAM_LENGTH; t += 1) {
                location.add(vector);
                player.getWorld().spawnParticle(Particle.SPELL_WITCH, location, 5, 0, 0, 0, 0);
                entityCheck(location, player);
                if (location.getBlock().getType().isSolid()) {
                    break;
                }
            }
        }
    }

    // prevents players from being hit twice by a single beam
    private void entityCheck(Location location, Player player) {

        for (Entity e : Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {

            // skip our player
            if (e == (player)) {
                continue;
            }

            if (e.getType().isAlive()) {
                LivingEntity victim = (LivingEntity) e;

                // ignore NPCs
                if (victim.hasMetadata("NPC")) {
                    continue;
                }

                // outlaw check
                if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(player))) {
                    continue;
                }

                // skip party members
                if (RunicCore.getPartyManager().getPlayerParty(player) != null
                        && RunicCore.getPartyManager().getPlayerParty(player).hasMember(e.getUniqueId())) {
                    continue;
                }

                if (this.hasBeenHit.containsKey(victim.getUniqueId())) {
                    List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                    if (uuids.contains(player.getUniqueId())) {
                        continue;
                    } else {
                        uuids.add(player.getUniqueId());
                        hasBeenHit.put(victim.getUniqueId(), uuids);
                    }
                } else {
                    List<UUID> uuids = new ArrayList<>();
                    uuids.add(player.getUniqueId());
                    hasBeenHit.put(victim.getUniqueId(), uuids);
                }

                // can't be hit by the same player's beam for 5 secs
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                        uuids.remove(player.getUniqueId());
                        hasBeenHit.put(victim.getUniqueId(), uuids);
                    }
                }.runTaskLater(RunicCore.getInstance(), 100L);

                DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, false);
                //KnockbackUtil.knockbackPlayer(player, victim, 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                continue;
            }
        }
    }
}

