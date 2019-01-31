package us.fortherealm.plugin.skillapi.skills.mage;

import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skillutil.KnockbackUtil;

import java.util.*;

public class ArcaneSpike extends Skill {

    // globals
    private static final int DAMAGE_AMOUNT = 5;
    private static final double BEAM_WIDTH = 1.5;
    private static final int BEAM_LENGTH = 16;
    private static final int RADIUS = 16;
    private HashMap<UUID, List<UUID>> hasBeenHit;

    // constructor
    public ArcaneSpike() {
        super("Arcane Spike",
                "You launch three beams of arcane magic!" +
                "\nEach beam deals " + DAMAGE_AMOUNT + " damage to enemies " +
                "\nit passes through.",
                ChatColor.WHITE, 1, 5);
        this.hasBeenHit = new HashMap<>();
    }

    // skill execute code
    @Override
    public void executeSkill(Player pl, SkillItemType type) {
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

        for (Entity e : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (e.getLocation().distance(location) <= BEAM_WIDTH) {

                // skip our player
                if (e == (player)) { continue; }

                if (e.getType().isAlive()) {
                    Damageable victim = (Damageable) e;

                    // skip party members
                    if (Main.getPartyManager().getPlayerParty(player) != null
                            && Main.getPartyManager().getPlayerParty(player).hasMember(e.getUniqueId())) { continue; }

                    if (this.hasBeenHit.containsKey(victim.getUniqueId())) {
                        List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                        if (uuids.contains(player.getUniqueId())) {
                            break;
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
                    }.runTaskLater(Main.getInstance(), 100L);

                    victim.damage(DAMAGE_AMOUNT, player);
                    KnockbackUtil.knockback(player, victim, 1);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    break;
                }
            }
        }
    }
}

