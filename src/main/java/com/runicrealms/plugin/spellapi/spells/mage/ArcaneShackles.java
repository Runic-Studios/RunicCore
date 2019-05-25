package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class ArcaneShackles extends Spell {

    // globals variables
    private static final int DURATION = 6;
    private static final int RADIUS = 4;
    private HashMap<Arrow, UUID> trails = new HashMap<>();
    private List<LivingEntity> victims = new ArrayList<>();

    // constructor
    public ArcaneShackles() {
        super("Arcane Shackles",
                "You launch a stream of arcane magic!" +
                        "\nUpon impact, enemies within " + RADIUS +  " blocks" +
                        "\nare rooted for " + DURATION + " seconds.",
                ChatColor.WHITE, 1, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize();
        startTask(player, new Vector[]{middle});
    }

    // particles, vectors
    private void startTask(Player pl, Vector[] vectors) {

        for (Vector vector : vectors) {
            Vector direction = pl.getEyeLocation().getDirection().normalize().multiply(1);
            Arrow arrow = pl.launchProjectile(Arrow.class);
            arrow.isSilent();
            UUID uuid = pl.getUniqueId();
            arrow.setVelocity(direction);
            arrow.setShooter(pl);
            trails.put(arrow, uuid);

            // send packets to make arrow invisible
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(arrow.getEntityId());
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    pl.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.FUCHSIA, 1));
                    if (arrow.isDead() || arrow.isOnGround()) {

                        // impact effect
                        this.cancel();
                        if (arrowLoc.getWorld() == null) return;
                        arrowLoc.getWorld().playSound(arrowLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);
                        arrowLoc.getWorld().playSound(arrowLoc, Sound.ENTITY_SHULKER_SHOOT, 0.5f, 1.25f);
                        arrowLoc.getWorld().spawnParticle
                                (Particle.REDSTONE, arrowLoc, 30, 1.0f, 1.0f, 1.0f,
                                        new Particle.DustOptions(Color.FUCHSIA, 6));

                        // get nearby enemies within blast radius
                        for (Entity entity : arrow.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                            if (entity != (pl)) {
                                if (entity.getType().isAlive()) {
                                    LivingEntity victim = (LivingEntity) entity;

                                    // ignore NPCs
                                    if (entity.hasMetadata("NPC")) continue;

                                    // skip party members
                                    if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                                            && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) {
                                        continue;
                                    }

                                    // apply skill effect
                                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 2.0f);
                                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION*20, 6));
                                    victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, DURATION*20, 128));
                                    victims.add(victim);
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);

            new BukkitRunnable() {
                int count = 1;
                @Override
                public void run() {

                    if (count > DURATION) {
                        this.cancel();
                        victims.clear();
                    } else {

                        count += 1;
                        for (LivingEntity victim : victims) {

                            victim.setFallDistance(-512f);

                            victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                                    1, 0.25f, 0.25f, 0.25f, new Particle.DustOptions(Color.FUCHSIA, 2));
                        }
                    }
                }
            }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (trails.containsKey(arrow)) {
                e.setCancelled(true);
            }
        }
    }
}

