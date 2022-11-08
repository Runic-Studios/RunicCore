package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Rescue extends Spell {

    private static final double DURATION = 1.5;
    private static final double LAUNCH_MULTIPLIER = 2.0;
    private static final double PERCENT = .15;
    private static final double RADIUS = 1.5;
    private final HashMap<UUID, UUID> hasBeenHit;

    public Rescue() {
        super("Rescue",
                "You launch yourself forward, stopping at " +
                        "the first ally you collide with! " +
                        "You and your ally both gain a shield■ " +
                        "equal to " + (int) (PERCENT * 100) + "% of your health!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 10, 20);
        hasBeenHit = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location location = player.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(look.getX(), 0, look.getZ()).normalize();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
        // player.teleport(location.add(0, 0.5, 1));
        player.setVelocity(launchPath.multiply(LAUNCH_MULTIPLIER));
        long startTime = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {

                if (System.currentTimeMillis() - startTime > (DURATION * 1000)) {
                    this.cancel();
                    //return;
                }

                player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 1, 0, 0, 0, 0);

                for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                    if (entity.equals(player)) continue; // skip caster
                    if (hasBeenHit.containsKey(entity.getUniqueId())) continue;
                    if (isValidAlly(player, entity)) {
                        if (entity instanceof Player && RunicCore.getPartyManager().getPlayerParty(player).hasMember((Player) entity)) { // normal ally check allows for non-party spells, so this prevents axe trolling
                            this.cancel();
                            hasBeenHit.put(player.getUniqueId(), entity.getUniqueId()); // prevent concussive hits
                            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.2f);
                            entity.getWorld().spawnParticle
                                    (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                            entity.getWorld().spawnParticle
                                    (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                            // shieldCasterAndAlly(player, (Player) entity);
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), hasBeenHit::clear, (long) (DURATION * 20L));
    }
}

