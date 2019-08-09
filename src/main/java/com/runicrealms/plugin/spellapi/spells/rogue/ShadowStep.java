package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ShadowStep extends Spell {

    private static final int DURATION = 6;
    private static int HEALING_AMT = 20;
    private HashMap<UUID, BukkitTask> activeSteppers;

    public ShadowStep() {
        super("Shadow Step",
                "You mark your current location" +
                        "\nin shadow! After " + DURATION + " seconds, you" +
                        "\nteleport back to the marked" +
                        "\nlocation, restoring✦ " + HEALING_AMT + " health!",
                ChatColor.WHITE,12, 15);
        activeSteppers = new HashMap<>();
    }

    /**
     * Cancel teleportation on death to prevent exploits
     */
    @EventHandler
    public void onDeath(RunicDeathEvent e) {
        if (activeSteppers.containsKey(e.getVictim().getUniqueId())) {
            activeSteppers.get(e.getVictim().getUniqueId()).cancel();
            activeSteppers.remove(e.getVictim().getUniqueId());
        }
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);

        BukkitTask teleportTask = new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                            new Particle.DustOptions(Color.PURPLE, 3));
                    pl.teleport(loc);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                            new Particle.DustOptions(Color.PURPLE, 3));
                    HealUtil.healPlayer(HEALING_AMT, pl, pl, true, false, false);
                } else {
                    count += 1;
                    createCircle(pl, loc, 1f);
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);

        activeSteppers.put(pl.getUniqueId(), teleportTask);
    }

    private void createCircle(Player pl, Location loc, float radius) {

        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc,
                    1, 0, 0, 0, 0, new Particle.DustOptions(Color.PURPLE, 1));
            pl.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }
    }
}

