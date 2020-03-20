package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class RunicMissile extends Spell {

    //private HashMap<UUID, List<UUID>> hasBeenHit;
    private static int DAMAGE_AMT = 15;
    //private final double RADIUS = 1.5;
    private final int RANGE = 10;
    private final int SPEED = 3;
    private static final int IMPACT_RADIUS = 3;
    private static final double GEM_BOOST = 0;

    // in seconds
    //private final int SUCCESSIVE_COOLDOWN = 2;

    public RunicMissile() {
        super("Runic Missile",
                "You launch a beam of runic magic," +
                        "\ndealing " + DAMAGE_AMT + " spellʔ damage to enemies" +
                        "\nwithin " + IMPACT_RADIUS + " blocks on impact!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: " + (int) GEM_BOOST + "%",
                ChatColor.WHITE, ClassEnum.RUNIC, 20, 35);
        //this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);

        // particle effect, spell effects
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(SPEED);
        startTask(pl, middle);
    }

    // particle effect
    private void startTask(Player pl, Vector direction) {
        //for (Vector vector : vectors) {

              new BukkitRunnable() {
                Location location = pl.getEyeLocation();
                Location startLoc = pl.getLocation();

                @Override
                public void run() {
                    location.add(direction);
                    // 10 block range before spell dies out naturally
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                        this.cancel();
                        detonate(pl, location, true);
                    }
                    pl.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.FUCHSIA, 1));
                    pl.getWorld().spawnParticle(Particle.SPELL_WITCH, location, 10, 0, 0, 0, 0);
                    if (detonate(pl, location, false)) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
        //}
    }

//    // checks for allies near the beam, stops multiple healing of the same player
//    @SuppressWarnings("deprecation")
//    private void enemyCheck(Player pl, Location location) {
//
//        for (Entity e : Objects.requireNonNull
//                (location.getWorld()).getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
//
//            if (!e.getType().isAlive()) return;
//            LivingEntity le = (LivingEntity) e;
//
//            if (le == (pl)) { continue; }
//
//            // a bunch of fancy checks to make sure one player can't be spam damaged by the same effect
//            // multiple times
//            if (hasBeenHit.containsKey(le.getUniqueId())) {
//                List<UUID> uuids = hasBeenHit.get(le.getUniqueId());
//                if (uuids.contains(pl.getUniqueId())) {
//                    break;
//                } else {
//                    uuids.add(pl.getUniqueId());
//                    hasBeenHit.put(le.getUniqueId(), uuids);
//                }
//            } else {
//                List<UUID> uuids = new ArrayList<>();
//                uuids.add(pl.getUniqueId());
//                hasBeenHit.put(le.getUniqueId(), uuids);
//            }
//
//            // ignore NPCs
//            if (le.hasMetadata("NPC")) continue;
//
//            // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    List<UUID> uuids = hasBeenHit.get(le.getUniqueId());
//                    uuids.remove(pl.getUniqueId());
//                    hasBeenHit.put(le.getUniqueId(), uuids);
//                }
//            }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));
//
//            // stop the beam if it hits a player
//            detonate();
//            break;
//
//        }
//    }

    private boolean detonate(Player caster, Location missleLoc, boolean effectEarly) {
        if (missleLoc.getWorld() == null) return false;
        if (effectEarly) {
            // particle effect
            missleLoc.getWorld().spawnParticle(Particle.REDSTONE, missleLoc,
                    50, 1f, 1f, 1f, new Particle.DustOptions(Color.PURPLE, 20));
            // sound effects
            missleLoc.getWorld().playSound(missleLoc, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
        }
        boolean hitEnemy = false;
        for (Entity en : missleLoc.getWorld().getNearbyEntities(missleLoc, IMPACT_RADIUS, IMPACT_RADIUS, IMPACT_RADIUS)) {
            if (!(en instanceof LivingEntity)) continue;
            if (verifyEnemy(caster, en)) {
                hitEnemy = true;
                DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, caster, GEM_BOOST);
            }
        }
        if (hitEnemy && !effectEarly) {
            // particle effect
            missleLoc.getWorld().spawnParticle(Particle.REDSTONE, missleLoc,
                    50, 1f, 1f, 1f, new Particle.DustOptions(Color.PURPLE, 20));
            // sound effects
            missleLoc.getWorld().playSound(missleLoc, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
        }
        return hitEnemy;
    }
}
