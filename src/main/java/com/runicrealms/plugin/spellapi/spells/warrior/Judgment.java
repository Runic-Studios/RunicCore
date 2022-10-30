package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell {

    private static final int BUBBLE_DURATION = 6;
    private static final int BUBBLE_SIZE = 5;
    private static final int SHIELD_AMT = 200;
    private static final int SHIELD_PERIOD = 2;
    private static final double KNOCKBACK = 0.15;
    private static final double UPDATES_PER_SECOND = 10;
    private final List<UUID> judgers;

    public Judgment() {
        super("Judgment",
                "You summon a barrier of magic " +
                        "around yourself for " + BUBBLE_DURATION + "s! The barrier " +
                        "prevents enemies from entering, but allies may pass through freely! " +
                        "Every " + SHIELD_PERIOD + "s, allies within the barrier are " +
                        "shielded■ for " + SHIELD_AMT + " health! " +
                        "During this time, you may not move. " +
                        "Sneak to cancel the spell early.",
                ChatColor.WHITE, ClassEnum.WARRIOR, 60, 35);
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

        BukkitTask shieldTask = new BukkitRunnable() {
            @Override
            public void run() {
                HealUtil.shieldPlayer(SHIELD_AMT, pl, pl, false);
                for (Entity en : pl.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {
                    if (isValidAlly(pl, en)) {
                        HealUtil.shieldPlayer(SHIELD_AMT, (Player) en, pl, false);
                    }
                }
            }
        }.runTaskTimer(plugin, SHIELD_PERIOD * 20L, SHIELD_PERIOD * 20L);

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI / 10;
                Location loc = pl.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = BUBBLE_SIZE * cos(theta) * sin(phi);
                    double y = BUBBLE_SIZE * cos(phi) + 1.5;
                    double z = BUBBLE_SIZE * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > BUBBLE_DURATION * 1000 || pl.isSneaking()) {
                    this.cancel();
                    shieldTask.cancel();
                    judgers.clear();
                    return;
                }

                // More effect noises
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);

                // Look for targets nearby
                for (Entity entity : pl.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {
                    //if (entity instanceof ItemStack) continue;
                    if (isValidEnemy(pl, entity)) {
                        Vector force = pl.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-KNOCKBACK).setY(0.3);
                        entity.setVelocity(force);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20 / UPDATES_PER_SECOND));
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

    public static int getShieldAmt() {
        return SHIELD_AMT;
    }
}

