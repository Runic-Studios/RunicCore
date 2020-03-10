package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Discharge extends Spell {

    private static final int DAMAGE_AMT = 20;
    private static final int BLAST_RADIUS = 5;
    private static final double KNOCKBACK_MULT = -0.2;
    private static final double KNOCKUP_AMT = 0.5;
    private HashMap<Arrow, UUID> trails = new HashMap<>();

    public Discharge() {
        super("Discharge",
                "You launch an electric spark!" +
                        "\nUpon impact, it summons lightning," +
                        "\ndealing " + DAMAGE_AMT + " spellʔ damage to enemies" +
                        "\nwithin " + BLAST_RADIUS + " blocks.",
                ChatColor.WHITE, ClassEnum.MAGE, 8, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize();
        startTask(player, new Vector[]{middle});
    }

    private void startTask(Player pl, Vector[] vectors) {
        for (Vector ignored : vectors) {
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
                    pl.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.TEAL, 1));
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                        arrow.getWorld().spigot().strikeLightningEffect(arrowLoc, true);
                        arrow.getWorld().playSound(arrowLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);
                        arrow.getWorld().playSound(arrowLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                        arrow.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 5, 0.2f, 0.2f, 0.2f, 0);

                        // get nearby enemies within blast radius
                        for (Entity entity : arrow.getNearbyEntities(BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)) {
                            if (entity != (pl)) {
                                if (entity.getType().isAlive()) {
                                    LivingEntity victim = (LivingEntity) entity;
                                    if (verifyEnemy(pl, victim)) {
                                        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl, false);
                                        Vector force = (arrowLoc.toVector().subtract(victim.getLocation().toVector()).multiply(KNOCKBACK_MULT).setY(KNOCKUP_AMT));
                                        victim.setVelocity(force);
                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
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

