package us.fortherealm.plugin.skillapi.skills.mage;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skilltypes.skillutil.KnockbackUtil;

import java.util.HashMap;
import java.util.UUID;

public class Blizzard extends Skill {

    // globals
    private static final int DAMAGE_AMOUNT = 3;
    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final double SNOWBALL_SPEED = 0.5;
    private HashMap<Snowball, UUID> snowballMap;

    // constructor
    public Blizzard() {
        super("Blizzard",
                "You summon a cloud of snow up to " + MAX_DIST + " blocks\n"
                        + "away that rains down snowballs for " + DURATION + " seconds,\n"
                        + "each dealing " + DAMAGE_AMOUNT + " damage to enemies.",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
        this.snowballMap = new HashMap<>();
    }

    // skill execute code
    @Override
    public void onRightClick(Player pl, SkillItemType type) {

        Location lookLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
        Vector launchPath = new Vector(0, -1.0, 0).normalize().multiply(SNOWBALL_SPEED);
        double startTime = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {

                // cancel after duration
                if (System.currentTimeMillis() - startTime >= DURATION*1000) {
                    this.cancel();
                }

                Location cloudLoc = new Location(pl.getWorld(), lookLoc.getX(),
                        pl.getLocation().getY(), lookLoc.getZ()).add(0, 7.5, 0);

                // sounds, reduced volume due to quantity of snowballs
                pl.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.25f, 1.0f);

                // particles
                pl.getWorld().spawnParticle(Particle.REDSTONE, cloudLoc,
                        25, 1.5f, 0.75f, 0.75f, new Particle.DustOptions(Color.WHITE, 20));

                // spawn 5 snowballs
                spawnSnowball(pl, cloudLoc, launchPath);
                spawnSnowball(pl, cloudLoc.add(1, 0, 0), launchPath);
                spawnSnowball(pl, cloudLoc.add(-2, 0 , 0), launchPath);
                spawnSnowball(pl, cloudLoc.add(2, 0, 1), launchPath);
                spawnSnowball(pl, cloudLoc.add(0, 0 , -2), launchPath);
            }
        }.runTaskTimer(Main.getInstance(), 0, 10); // drops a snowball every half second
    }

    // listener to damage player
    @EventHandler
    public void onSnowballDamage(EntityDamageByEntityEvent e) {

        if (!(e.getDamager() instanceof Snowball)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        Snowball snowball = (Snowball) e.getDamager();
        if (!snowballMap.containsKey(snowball)) return;

        Player shooter = Bukkit.getPlayer(snowballMap.get(snowball));
        LivingEntity victim = (LivingEntity) e.getEntity();

        e.setCancelled(true);

        // skip the caster
        if (victim.getUniqueId() == shooter.getUniqueId()) return;

        // skip party members
        if (Main.getPartyManager().getPlayerParty(shooter) != null
                && Main.getPartyManager().getPlayerParty(shooter).hasMember(victim.getUniqueId())) return;

        // apply damage, knockback
        victim.damage(DAMAGE_AMOUNT, shooter);
        victim.setLastDamageCause(e);
        KnockbackUtil.knockback(shooter, victim);
    }

    private void spawnSnowball(Player pl, Location loc, Vector vec) {
        Snowball snowball = pl.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowballMap.put(snowball, pl.getUniqueId());
    }
}

