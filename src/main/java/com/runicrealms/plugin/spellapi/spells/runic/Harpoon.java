package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;

@SuppressWarnings("FieldCanBeLocal")
public class Harpoon extends Spell {

    // globals
    private static final double TRIDENT_SPEED = 1.25;
    private Trident trident;

    // constructor
    public Harpoon() {
        super ("Harpoon",
                "You launch a projectile harpoon" +
                        "\nwhich pulls your enemy towards" +
                        "\nyou!",
                ChatColor.WHITE, 12, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {
        trident = player.launchProjectile(Trident.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(TRIDENT_SPEED);
        trident.setVelocity(velocity);
        trident.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.5f);

        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (trident.isDead()) {
                    this.cancel();
                }
                trident.getWorld().spawnParticle(Particle.REDSTONE, trident.getLocation(),
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTridentDamage(EntityDamageByEntityEvent event) {

        // only listen for our trident
        if (!(event.getDamager().equals(this.trident))) return;

        event.setCancelled(true);

        // grab our variables
        Player player = (Player) trident.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        // skip NPCs
        if (victim.hasMetadata("NPC")) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(player) != null
                && RunicCore.getPartyManager().getPlayerParty(player).hasMember(victim.getUniqueId())) { return; }

        // apply spell mechanics
        Location playerLoc = player.getLocation();
        Location targetLoc = victim.getLocation();

        Vector pushUpVector = new Vector(0.0D, 0.4D, 0.0D);
        victim.setVelocity(pushUpVector);

        final double xDir = (playerLoc.getX() - targetLoc.getX()) / 3.0D;
        double zDir = (playerLoc.getZ() - targetLoc.getZ()) / 3.0D;
        final double hPower = 0.5D;

        new BukkitRunnable() {
            @Override
            public void run() {
                Vector pushVector = new Vector(xDir, 0.0D, zDir).normalize().multiply(2).setY(0.4D);
                victim.setVelocity(pushVector);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            }
        }.runTaskLater(RunicCore.getInstance(), 4L);
    }
}

