package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell {

    private static final int BUBBLE_DURATION = 8;
    private static final int BUBBLE_SIZE = 5;
    private static final double UPDATES_PER_SECOND = 10;
    private List<UUID> judgers;

    public Judgment() {
        super("Judgment",
                "You summon a barrier of magic" +
                        "\naround yourself for " + BUBBLE_DURATION + " seconds!" +
                        "\nThe barrier repels enemies and" +
                        "\nprevents them from entering, but" +
                        "\nallies may pass through it. During" +
                        "\nthis time, you may not move.",
                ChatColor.WHITE, ClassEnum.WARRIOR, 9, 35);
        judgers = new ArrayList<>();
    }

    @Override
    public boolean attemptToExecute(Player pl) {
        if (!pl.isOnGround()) {
            pl.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
            return false;
        }
        return true;
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // Play sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        pl.getWorld().spigot().strikeLightningEffect(pl.getLocation(), true);
        judgers.add(pl.getUniqueId());

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;
            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI/10;
                Location loc = pl.getLocation();
                for (double theta = 0; theta <= 2*Math.PI; theta += Math.PI/40) {
                    double x = BUBBLE_SIZE*cos(theta)*sin(phi);
                    double y = BUBBLE_SIZE*cos(phi) + 1.5;
                    double z = BUBBLE_SIZE*sin(theta)*sin(phi);
                    loc.add(x,y,z);
                    pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x,y,z);
                }

                // Spell duration
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > BUBBLE_DURATION * 1000) {
                    this.cancel();
                    judgers.clear();
                    return;
                }

                // More effect noises
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_CAT_HISS, 0.01F, 0.5F);

                // Look for targets nearby
                for (Entity entity : pl.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {
                    //if (entity instanceof ItemStack) continue;
                    if (verifyEnemy(pl, entity)) {
                        Vector force = pl.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.25).setY(0.3);
                        entity.setVelocity(force);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20/UPDATES_PER_SECOND));
    }

    /*
    Cancel player movement.
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!judgers.contains(e.getPlayer().getUniqueId())) return;
        if (e.getTo() == null) return;
        if (!e.getFrom().toVector().equals(e.getTo().toVector())) e.setCancelled(true);
    }
}

