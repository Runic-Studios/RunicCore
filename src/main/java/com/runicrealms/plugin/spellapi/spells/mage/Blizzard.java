package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
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

public class Blizzard extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMOUNT = 15;
    private static final double DAMAGE_PER_LEVEL = 0.75;
    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final double SNOWBALL_SPEED = 0.5;
    private final HashMap<Snowball, UUID> snowballMap;

    public Blizzard() {
        super("Blizzard",
                "You summon a cloud of snow that " +
                        "rains down snowballs for " + DURATION + " seconds, " +
                        "each dealing (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies and slowing them!",
                ChatColor.WHITE, ClassEnum.MAGE, 15, 40);
        this.snowballMap = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location lookLoc = player.getTargetBlock(null, MAX_DIST).getLocation();
        Vector launchPath = new Vector(0, -1.0, 0).normalize().multiply(SNOWBALL_SPEED);

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count++;

                    Location cloudLoc = new Location(player.getWorld(), lookLoc.getX(),
                            player.getLocation().getY(), lookLoc.getZ()).add(0, 7.5, 0);

                    // sounds, reduced volume due to quantity of snowballs
                    player.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.25f, 1.0f);

                    // particles
                    player.getWorld().spawnParticle(Particle.REDSTONE, cloudLoc,
                            25, 1.5f, 0.75f, 0.75f, new Particle.DustOptions(Color.WHITE, 20));

                    // spawn 9 snowballs in a 3x3 square
                    spawnSnowball(player, cloudLoc, launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(-2, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(2, 0, 1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -2), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 2), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -2), launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);

                    // 1
                    spawnSnowball(player, cloudLoc.add(-1, 0, -1), launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
                    // 2
                    spawnSnowball(player, cloudLoc.add(1, 0, 1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, 1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, 1), launchPath);
                    // 3
                    spawnSnowball(player, cloudLoc.add(-1, 0, 1), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
                    // 4
                    spawnSnowball(player, cloudLoc.add(-1, 0, -1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -1), launchPath);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20); // drops a snowball every second
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
        if (le.hasMetadata("NPC")) return;
        if (victim.getUniqueId() == shooter.getUniqueId()) return;

        if (isValidEnemy(shooter, le)) {
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, shooter, this);
            victim.setLastDamageCause(e);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
        }
    }

    private void spawnSnowball(Player player, Location loc, Vector vec) {
        Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowballMap.put(snowball, player.getUniqueId());
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

