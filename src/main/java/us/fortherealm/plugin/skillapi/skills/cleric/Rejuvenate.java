package us.fortherealm.plugin.skillapi.skills.cleric;

import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skillapi.skilltypes.skillutil.HealUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Rejuvenate extends Skill {

    // grab our globals
    private HealUtil hu = new HealUtil();
    private HashMap<UUID, List<UUID>> hasBeenHit;
    private static final int HEAL_AMT = 25;
    private final int RADIUS = 10;
    private final int RANGE = 15;
    private final double BEAM_WIDTH = 2.0;
    private final int SPEED = 2;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 5;

    // constructor
    public Rejuvenate() {
        super("Rejuvenate",
                "You launch a beam of healing magic," +
                "\nrestoring " + HEAL_AMT + " health to all party members" +
                "\nit passes through.",
                ChatColor.WHITE, Skill.ClickType.RIGHT_CLICK_ONLY, 10, 10);
        this.hasBeenHit = new HashMap<>();
    }

    // skill execute code
    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);

        // particle effect, skill effects
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
                    // 10 block range before skill dies out naturally
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                        this.cancel();
                    }
                    pl.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
                    pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 5, 0, 0, 0, 0);
                    allyCheck(pl, location);
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L);
        }
    }

    // checks for allies near the beam, stops multiple healing of the same player
    @SuppressWarnings("deprecation")
    private void allyCheck(Player pl, Location location) {

        String storedName = plugin.getConfig().get(pl.getUniqueId() + ".info.name").toString();

        for (Entity e : location.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
            if (e.getLocation().distance(location) <= BEAM_WIDTH) {
                if (e != (pl)) {

                    // only listen for players
                    if (!(e instanceof Player)) { return; }

                    // skip the player if we've got a party and they're not in it
                    if (Main.getPartyManager().getPlayerParty(pl) != null
                            && !Main.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) { continue; }

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
                    }.runTaskLater(Main.getInstance(), (SUCCESSIVE_COOLDOWN * 20));

                    if (ally.getHealth() == ally.getMaxHealth()) {
                        ally.sendMessage(
                                ChatColor.WHITE + storedName
                                        + ChatColor.GRAY + " tried to heal you, but you are at full health.");
                        ally.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);

                    } else {
                        hu.healPlayer(HEAL_AMT, ally, " from " + ChatColor.WHITE + storedName);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                        ally.getWorld().spawnParticle(Particle.HEART, ally.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);

                        // stop the beam if it hits a player
                        break;
                    }
                }
            }
        }
    }
}
