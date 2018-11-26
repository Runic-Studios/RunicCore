package us.fortherealm.plugin.skills.skilltypes.cleric.defensive;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.skillutil.HealUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Rejuvenate extends TargetingSkill<LivingEntity> implements ImpactListener<EntityDamageByEntityEvent> {

    private HealUtil hu = new HealUtil();
    private HashMap<UUID, List<UUID>> hasBeenHit;
    private final int RADIUS = 10;
    private final double BEAM_WIDTH = 2.0;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 5;

    // constructor
    public Rejuvenate() {
        super("Rejuvenate", "a prjectile heal", 8, true);
        this.hasBeenHit = new HashMap<>();
    }

    // actual skill code (really all we need)
    public void executeSkill() {

        // sound effect
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);

        // particle effect, skill effects
        Vector middle = getPlayer().getEyeLocation().getDirection().normalize();
        startTask(getPlayer(), new Vector[]{middle});
    }

    // return our event
    @Override
    public Class<EntityDamageByEntityEvent> getEventClass() {
        return EntityDamageByEntityEvent.class;
    }

    // returns this object when we call getSkill method
    @Override
    public Skill getSkill() {
        return this;
    }

    // only listen for our event
    // still, do we need this?
    @Override
    public boolean isPreciseEvent(EntityDamageByEntityEvent event) {
        return true;
    }

    // reminder to initialize skill variables
    // do we need this?
    @Override
    public void initializeSkillVariables(EntityDamageByEntityEvent event) {

    }

    // do we need this?
    @Override
    public void doImpact(EntityDamageByEntityEvent event) {

    }

    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            new BukkitRunnable() {
                Location location = player.getEyeLocation();
                Location startLoc = player.getLocation();

                @Override
                public void run() {
                    location.add(vector);
                    // 10 block range before skill dies out naturally
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RADIUS) {
                        this.cancel();
                    }
                    player.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 5, 0, 0, 0, 0);
                    allyCheck(location, player);
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L);
        }
    }

    @SuppressWarnings("deprecation")
    private void allyCheck(Location location, Player player) {
        for (Entity e : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (e.getLocation().distance(location) <= BEAM_WIDTH) {
                if (e != (player)) {

                    // only listen for players
                    if (!(e instanceof Player)) { return; }

                    // skip the player if we've got a party and they're not in it
                    if (Main.getPartyManager().getPlayerParty(getPlayer()) != null
                            && !Main.getPartyManager().getPlayerParty(getPlayer()).hasMember(e.getUniqueId())) { continue; }

                    // a bunch of fancy checks to make sure one player can't be spam healed by the same effect
                    // multiple times
                    Player ally = (Player) e;
                    if (hasBeenHit.containsKey(ally.getUniqueId())) {
                        List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                        if (uuids.contains(player.getUniqueId())) {
                            break;
                        } else {
                            uuids.add(player.getUniqueId());
                            hasBeenHit.put(ally.getUniqueId(), uuids);
                        }
                    } else {
                        List<UUID> uuids = new ArrayList<>();
                        uuids.add(player.getUniqueId());
                        hasBeenHit.put(ally.getUniqueId(), uuids);
                    }

                    // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                            uuids.remove(player.getUniqueId());
                            hasBeenHit.put(ally.getUniqueId(), uuids);
                        }
                    }.runTaskLater(Main.getInstance(), (SUCCESSIVE_COOLDOWN * 20));

                    if (ally.getHealth() == ally.getMaxHealth()) {
                        ally.sendMessage(
                                ChatColor.WHITE + getPlayerName()
                                        + ChatColor.GRAY + " tried to heal you, but you are currently at full health.");
                        ally.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);

                    } else {
                        hu.healPlayer(25, ally, " from " + ChatColor.WHITE + getPlayerName());
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                        ally.getWorld().spawnParticle(Particle.HEART, ally.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);

                        // what does this guy do?
                        break;
                    }
                }
            }
        }
    }
}

