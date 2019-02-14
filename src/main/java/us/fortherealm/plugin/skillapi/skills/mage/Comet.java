package us.fortherealm.plugin.skillapi.skills.mage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import us.fortherealm.plugin.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.utilities.DamageUtil;

@SuppressWarnings("deprecation")
public class Comet extends Skill {

    // global variables
    private FallingBlock comet;
    private static final double COMET_SPEED = 0.1;
    private static final int DAMAGE_AMT = 20;
    private static final int BLAST_RADIUS = 5;
    private static final int MAX_DIST = 10;
    private static final double KNOCKBACK_MULT = -0.5;
    private static final double KNOCKUP_AMT = 0.5;

    // constructor
    public Comet() {
        super("Comet",
                "You call a comet to fall from the sky!" +
                        "\nUpon impact, the comet deals " + DAMAGE_AMT + " damage" +
                        "\nto all enemies within " + BLAST_RADIUS + " blocks!",
                ChatColor.WHITE, 1, 15);
    }

    // skill execute code
    @Override
    public void executeSkill(Player pl, SkillItemType type) {

        // play effects, spawn the comet
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 1.0F);
        Block targetBlock = pl.getTargetBlock(null, MAX_DIST);
        Location targetLoc = targetBlock.getLocation().clone().add(0, 30, 0);
        comet = targetLoc.getWorld().spawnFallingBlock(targetLoc, Material.DRAGON_EGG, (byte) 0);//FallingBlock
        comet.setDropItem(false);

        // set the comet's trajectory
        Vector trajectory = targetBlock.getLocation().toVector().subtract(comet.getLocation().toVector());
        comet.setVelocity(trajectory.multiply(COMET_SPEED));

        // start the runnable
        long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {

                // trail particle effects
                comet.getWorld().spawnParticle(Particle.FLAME, comet.getLocation(), 5, 0.2F, 1.0F, 0.2F, 0);
                comet.getWorld().spawnParticle(Particle.SMOKE_LARGE, comet.getLocation(), 5, 0, 1.0F, 0, 0);

                // once the comet lands or despawns, or after 10s have passed to prevent weird exceptions.
                if ((comet.isOnGround() || comet.isDead()) || (System.currentTimeMillis() - startTime > 10000)) {

                    this.cancel();

                    // impact particle effects
                    comet.getWorld().playSound(comet.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
                    comet.getWorld().playSound(comet.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 1.0F);
                    comet.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, comet.getLocation(), 5, 0.2F, 1.0F, 0.2F, 0);
                    comet.getWorld().spawnParticle(Particle.LAVA, comet.getLocation(), 45, 1F, 1F, 1F, 0);
                    comet.getWorld().spawnParticle(Particle.SMOKE_LARGE, comet.getLocation(), 45, 1F, 1F, 1F, 0);

                    // get nearby enemies within blast radius
                    for (Entity entity : comet.getNearbyEntities(BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)) {

                        // skip our caster
                        if (entity.equals(pl)) {
                            continue;
                        }

                        // skip party members
                        if (Main.getPartyManager().getPlayerParty(pl) != null
                                && Main.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

                        // apply effects, damage
                        if (entity.getType().isAlive()) {
                            LivingEntity victim = (LivingEntity) entity;
                            DamageUtil.damageEntityMagic(DAMAGE_AMT, victim, pl);
                            Vector force = (comet.getLocation().toVector().subtract(victim.getLocation().toVector()).multiply(KNOCKBACK_MULT).setY(KNOCKUP_AMT));
                            victim.setVelocity(force);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    // stops this pesky comet from changing the map
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockChange(EntityChangeBlockEvent e) {
        if (e.getEntity().equals(this.comet)) {
            e.setCancelled(true);
        }
    }
}

