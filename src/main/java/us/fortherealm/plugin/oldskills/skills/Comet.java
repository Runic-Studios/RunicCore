package us.fortherealm.plugin.oldskills.skills;

import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.oldskills.skilltypes.Skill;
import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

// TODO: party damage check
public class Comet extends Skill {
    public Comet() {
        super("Comet", "coming soon",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        int maxDist = 10;
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 1.0F);
        Block targetBlock = player.getTargetBlock(null, maxDist);
        Location targetLoc = targetBlock.getLocation().clone().add(0.5, 40, 0.5);
        FallingBlock comet = targetLoc.getWorld().spawnFallingBlock(targetLoc, Material.DRAGON_EGG.getId(), (byte) 0);
        comet.setDropItem(false);
        comet.setHurtEntities(false);
        comet.setVelocity(new Vector(0,-3,0));
        startTask(player, comet);
    }

    public void startTask(Player player, FallingBlock comet) {
        new BukkitRunnable() {

            @Override
            public void run() {
                comet.getWorld().spawnParticle(Particle.FLAME, comet.getLocation(), 5, 0.2F, 1.0F, 0.2F, 0);
                comet.getWorld().spawnParticle(Particle.SMOKE_LARGE, comet.getLocation(), 5, 0, 1.0F, 0, 0);
                if ((comet.isOnGround() || comet.isDead())) {
                    if (!(comet.getLocation().getBlock().getType().equals(Material.STEP)
                            || comet.getLocation().getBlock().getType().equals(Material.WOOD_STEP))) {
                        comet.getLocation().getBlock().setType(Material.AIR);
                    }
                    comet.getWorld().playSound(comet.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
                    comet.getWorld().playSound(comet.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 1.0F);
                    comet.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, comet.getLocation(), 5, 0.2F, 1.0F, 0.2F, 0);
                    comet.getWorld().spawnParticle(Particle.FLAME, comet.getLocation(), 45, 1F, 1F, 1F, 0);
                    comet.getWorld().spawnParticle(Particle.SMOKE_LARGE, comet.getLocation(), 45, 1F, 1F, 1F, 0);
                    entityCheck(player, comet.getLocation());
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);
    }

    public void entityCheck(Player player, Location cometLocation) {
        for (Entity entity : cometLocation.getChunk().getEntities()) {
            if (entity.getLocation().distance(cometLocation) <= 5) {
                if (entity != (player)) {
                    if (entity.getType().isAlive()) {
                        Damageable victim = (Damageable) entity;
                        victim.damage(50, player);
                        Vector force = (cometLocation.toVector().subtract(victim.getLocation().toVector()).multiply(-1).setY(0.6));
                        victim.setVelocity(force);
                    }
                }
            }
        }
    }
}
