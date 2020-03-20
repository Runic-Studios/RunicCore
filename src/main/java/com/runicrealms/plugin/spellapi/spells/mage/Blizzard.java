package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class Blizzard extends Spell {

    private static final int DAMAGE_AMOUNT = 15;
    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final double SNOWBALL_SPEED = 0.5;
    private HashMap<Snowball, UUID> snowballMap;
    private static final double GEM_BOOST = 50;

    public Blizzard() {
        super("Blizzard",
                "You summon a cloud of snow that" +
                        "\nrains down snowballs for " + DURATION + " seconds," +
                        "\neach dealing " + DAMAGE_AMOUNT + " spellʔ damage" +
                        "\nto enemies and slowing them!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 35);
        this.snowballMap = new HashMap<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location lookLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
        Vector launchPath = new Vector(0, -1.0, 0).normalize().multiply(SNOWBALL_SPEED);
        double startTime = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {

                // cancel after duration
                if (System.currentTimeMillis() - startTime >= DURATION * 1000) {
                    this.cancel();
                }

                Location cloudLoc = new Location(pl.getWorld(), lookLoc.getX(),
                        pl.getLocation().getY(), lookLoc.getZ()).add(0, 7.5, 0);

                // sounds, reduced volume due to quantity of snowballs
                pl.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.25f, 1.0f);

                // particles
                pl.getWorld().spawnParticle(Particle.REDSTONE, cloudLoc,
                        25, 1.5f, 0.75f, 0.75f, new Particle.DustOptions(Color.WHITE, 20));

                // spawn 9 snowballs in a 3x3 square
                spawnSnowball(pl, cloudLoc, launchPath);
                spawnSnowball(pl, cloudLoc.add(1, 0, 0), launchPath);
                spawnSnowball(pl, cloudLoc.add(-2, 0, 0), launchPath);
                spawnSnowball(pl, cloudLoc.add(2, 0, 1), launchPath);
                spawnSnowball(pl, cloudLoc.add(0, 0, -2), launchPath);
                spawnSnowball(pl, cloudLoc.add(-1, 0, 2), launchPath);
                spawnSnowball(pl, cloudLoc.add(-1, 0, 0), launchPath);
                spawnSnowball(pl, cloudLoc.add(0, 0, -2), launchPath);
                spawnSnowball(pl, cloudLoc.add(1, 0, 0), launchPath);

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 10); // drops a snowball every half second
    }

    // listener to damage player
    @EventHandler
    public void onSnowballDamage(EntityDamageByEntityEvent e) {

        if (!(e.getDamager() instanceof Snowball)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        LivingEntity le = (LivingEntity) e.getEntity();

        Snowball snowball = (Snowball) e.getDamager();
        if (!snowballMap.containsKey(snowball)) return;

        Player shooter = Bukkit.getPlayer(snowballMap.get(snowball));
        LivingEntity victim = (LivingEntity) e.getEntity();

        e.setCancelled(true);

        // ignore NPCs
        if (!le.hasMetadata("NPC")) {

            // skip the caster
            if (victim.getUniqueId() == shooter.getUniqueId()) return;

            if (verifyEnemy(shooter, le)) {
                DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, shooter, GEM_BOOST);
                victim.setLastDamageCause(e);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
            }
        }
    }

    private void spawnSnowball(Player pl, Location loc, Vector vec) {
        Snowball snowball = pl.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowballMap.put(snowball, pl.getUniqueId());
    }
}

