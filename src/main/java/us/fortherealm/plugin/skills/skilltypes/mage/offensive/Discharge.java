package us.fortherealm.plugin.skills.skilltypes.mage.offensive;

import org.bukkit.entity.*;
import us.fortherealm.plugin.Main;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

import java.util.HashMap;
import java.util.UUID;

public class Discharge extends TargetingSkill<EntityDamageByEntityEvent> implements ImpactListener<EntityDamageByEntityEvent> {

    // global variables
    private static final int DAMAGE_AMT = 10;
    private static final int BLAST_RADIUS = 3;
    private static final double KNOCKBACK_MULT = -1;
    private static final double KNOCKUP_AMT = 0.5;
    private HashMap<Arrow, UUID> trails = new HashMap<>();

    public Discharge() {
        super("Discharge", "fire a bolt of lightning that travels. if it hits a solid block, it blows up", 7, false);
    }

    @Override
    public void executeSkill() {
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1.0f);
        Vector middle = getPlayer().getEyeLocation().getDirection().normalize();

        startTask(getPlayer(), new Vector[]{middle});
    }

    private void startTask(Player player, Vector[] vectors) {
        for(Vector vector : vectors) {
            Vector direction = player.getEyeLocation().getDirection().normalize().multiply(1);
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.isSilent();
            UUID uuid = player.getUniqueId();
            arrow.setVelocity(direction);
            arrow.setShooter(player);
            trails.put(arrow, uuid);
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(arrow.getEntityId());
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    player.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 5, 0, 0, 0, 0);
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                        arrow.getWorld().spigot().strikeLightningEffect(arrowLoc, true);
                        arrowLoc.getWorld().playSound(arrowLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);
                        arrowLoc.getWorld().playSound(arrowLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                        arrowLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 5, 0.2f, 0.2f, 0.2f, 0);

                        // get nearby enemies within blast radius
                        for (Entity entity : arrow.getNearbyEntities(BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)) {
                            if (entity != (player)) {
                                if (entity.getType().isAlive()) {
                                    Damageable victim = (Damageable) entity;
                                    victim.damage(DAMAGE_AMT, player);
                                    Vector force = (arrowLoc.toVector().subtract(victim.getLocation().toVector()).multiply(KNOCKBACK_MULT).setY(KNOCKUP_AMT));
                                    victim.setVelocity(force);
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L);
        }
    }

    // TODO: put this in precise event and doImpact
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (trails.containsKey(arrow)) {
                e.setCancelled(true);
            }
        }
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
        return false;
    }

    @Override
    public void initializeSkillVariables(EntityDamageByEntityEvent event) {

    }

    @Override
    public void doImpact(EntityDamageByEntityEvent event) {

    }
}
