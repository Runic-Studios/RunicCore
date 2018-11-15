package us.fortherealm.plugin.skills.skilltypes.mage.offensive;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.skillutil.KnockbackUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class IceNova extends TargetingSkill<LivingEntity> implements ImpactListener<EntityDamageByEntityEvent> {

    // global variables
    private static final int DAMAGE_AMOUNT = 20;
    private static final double BEAM_WIDTH = 1.5;
    private HashMap<UUID, List<UUID>> hasBeenHit;

    // constructor
    public IceNova() {
        super("Ice Nova", "Shoots three beams of ice.", 8, false);
        this.hasBeenHit = new HashMap<>();
    }

    @Override
    public void executeSkill() {

        // sound effects
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.0f);

        // create three beams
        Vector middle = getPlayer().getEyeLocation().getDirection().normalize();
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector right = rotateVectorAroundY(middle, 22.5);

        // begin particle effect & entity check tasks
        startTask(getPlayer(), new Vector[]{middle, left, right});
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEventClass() {
        return EntityDamageByEntityEvent.class;
    }

    @Override
    public Skill getSkill() {
        return this;
    }

    @Override
    public boolean isPreciseEvent(EntityDamageByEntityEvent event) {

        // do we need to to this for skills like this?
        return true;
    }

    @Override
    public void initializeSkillVariables(EntityDamageByEntityEvent event) {

        // TODO: need to do this for AoE skills
    }

    @Override
    public void doImpact(EntityDamageByEntityEvent event) {

    }

    private void startTask(Player player, Vector[] vectors) {
        for(Vector vector : vectors) {
            Location location = player.getEyeLocation();
            for (double t = 0; t < 16; t += 1) {
                location.add(vector);
                player.getWorld().spawnParticle(Particle.BLOCK_CRACK, location, 5, 0, 0, 0, 0, new MaterialData(Material.PACKED_ICE));
                entityCheck(location, player);
                if (location.getBlock().getType().isSolid()) {
                    break;
                }
            }
        }
    }

    private void entityCheck(Location location, Player player) {

        for (Entity e : location.getChunk().getEntities()) {
            if (e.getLocation().distance(location) <= BEAM_WIDTH) {
                if (e != (player)) {
                    if (e.getType().isAlive()) {
                        Damageable victim = (Damageable) e;
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
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                uuids.remove(player.getUniqueId());
                                hasBeenHit.put(victim.getUniqueId(), uuids);
                            }
                        }.runTaskLater(Main.getInstance(), 100L); // can't be hit by the same player's beam for 5 secs

                        victim.damage(DAMAGE_AMOUNT, player);
                        KnockbackUtil.knockback(player, victim, 1);
                        ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2)); // 100 ticks = 5s (Slowness III)
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                        ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2)); // 100 ticks = 5s (Slowness III)
                        victim.sendMessage(ChatColor.RED + "You are slowed by " + ChatColor.WHITE + player.getName() + "§c's ice nova!");
                        break;
                    }
                }
            }
        }
    }

    private Vector rotateVectorAroundY(Vector vector, double degrees) {
        Vector newVector = vector.clone();
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sine = Math.sin(rad);
        double x = vector.getX();
        double z = vector.getZ();
        newVector.setX(cos * x - sine * z);
        newVector.setZ(sine * x + cos * z);
        return newVector;
    }
}

