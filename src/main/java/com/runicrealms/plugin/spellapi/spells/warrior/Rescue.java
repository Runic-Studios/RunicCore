package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Rescue extends Spell {

    private static final double DURATION = 1.5;
    private static final double LAUNCH_MULT = 1.5;
    private static final double PERCENT = .25;
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
    public void executeSpell(Player pl, SpellItemType type) {

        Location location = pl.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(look.getX(), 0, look.getZ()).normalize();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 0.5f, 1.0f);
        pl.teleport(location.add(0, 0.5, 1));
        pl.setVelocity(launchPath.multiply(LAUNCH_MULT));
        long startTime = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {

                if (System.currentTimeMillis() - startTime > (DURATION * 1000)) {
                    this.cancel();
                    //return;
                }

                pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, pl.getLocation(), 1, 0, 0, 0, 0);

                for (Entity entity : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                    if (entity.equals(pl)) continue; // skip caster
                    if (hasBeenHit.containsKey(entity.getUniqueId())) continue;
                    if (verifyAlly(pl, entity)) {
                        if (entity instanceof Player && RunicCore.getPartyManager().getPlayerParty(pl).hasMember((Player) entity)) { // normal ally check allows for non-party spells, so this prevents axe trolling
                            this.cancel();
                            hasBeenHit.put(pl.getUniqueId(), entity.getUniqueId()); // prevent concussive hits
                            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.2f);
                            entity.getWorld().spawnParticle
                                    (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                            entity.getWorld().spawnParticle
                                    (Particle.SPELL_INSTANT, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                            shieldCasterAndAlly(pl, (Player) entity);
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), hasBeenHit::clear, (long) (DURATION * 20L));
    }

    private void shieldCasterAndAlly(Player caster, Player ally) {
        double amount = caster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT;
        HealUtil.shieldPlayer(amount, caster, caster, true, false, false);
        HealUtil.shieldPlayer(amount, ally, caster, true, false, false);
    }
}

