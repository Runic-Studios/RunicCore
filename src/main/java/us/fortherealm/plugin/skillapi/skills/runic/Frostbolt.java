package us.fortherealm.plugin.skillapi.skills.runic;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skillutil.KnockbackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Frostbolt extends Skill {

    //globals
    private static final double SPEED = 2;
    private static final int RANGE = 15;
    private static final int RADIUS = 20;
    private static final int DAMAGE_AMT = 10;
    private static int BEAM_WIDTH = 2;
    private HashMap<UUID, List<UUID>> hasBeenHit;

    // constructor
    public Frostbolt() {
        super("Frostbolt",
                "You launch a projectile bolt of ice" +
                        "\nwhich deals " + DAMAGE_AMT + " damage on impact" +
                        "\nand slows its target!",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1, 5);
        this.hasBeenHit = new HashMap<>();
    }

    // skill execute code
    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        // create our vector to be used later
        Vector vector = pl.getEyeLocation().getDirection().normalize().multiply(SPEED);

        // play sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);

        // particle effect
        new BukkitRunnable() {

            Location location = pl.getEyeLocation();
            Location startLoc = pl.getLocation();

            @Override
            public void run() {

                location.add(vector);
                pl.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 5, 0, 0, 0, 0, Material.PACKED_ICE.createBlockData());

                // 10 block range before skill dies out naturally
                if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                    this.cancel();
                    pl.getWorld().spawnParticle(Particle.SNOWBALL, location, 25, 0, 0, 0, 0);
                }


                // get nearby entities
                for (Entity e : location.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                    if (e.getLocation().distance(location) <= BEAM_WIDTH) {
                        if (e != (pl)) {
                            if (e.getType().isAlive()) {
                                Damageable victim = (Damageable) e;

                                // skip party members
                                if (Main.getPartyManager().getPlayerParty(pl) != null
                                        && Main.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) {
                                    continue;
                                }

                                // prevent player from being hit by the same frostbolt
                                if (hasBeenHit.containsKey(victim.getUniqueId())) {
                                    List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                    if (uuids.contains(pl.getUniqueId())) {
                                        break;
                                    } else {
                                        uuids.add(pl.getUniqueId());
                                        hasBeenHit.put(victim.getUniqueId(), uuids);
                                    }
                                } else {
                                    List<UUID> uuids = new ArrayList<>();
                                    uuids.add(pl.getUniqueId());
                                    hasBeenHit.put(victim.getUniqueId(), uuids);
                                }

                                // can't be hit by the same player's beam for 5 secs
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                        uuids.remove(pl.getUniqueId());
                                        hasBeenHit.put(victim.getUniqueId(), uuids);
                                    }
                                }.runTaskLater(Main.getInstance(), 100L);

                                // apply skill effects
                                victim.damage(DAMAGE_AMT, pl);
                                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
                                pl.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 25, 0, 0, 0, 0, Material.PACKED_ICE.createBlockData());
                                KnockbackUtil.knockback(pl, victim, 1);
                                if (victim instanceof Player) {
                                    ((Player) victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                                }
                                this.cancel();
                                pl.getWorld().spawnParticle(Particle.SNOWBALL, location, 25, 0, 0, 0, 0);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }
}


