package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.DirectionUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Blizzard extends Spell {

    // globals
    private static final int DAMAGE_AMOUNT = 5;
    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final double SNOWBALL_SPEED = 0.5;
    private HashMap<Snowball, UUID> snowballMap;

    // constructor
    public Blizzard() {
        super("Blizzard",
                "You summon a cloud of snow up to " +
                        "\n" + MAX_DIST + " blocks away that rains down snowballs" +
                        "\nfor " + DURATION + " seconds, each dealing " + DAMAGE_AMOUNT + " spellʔ" +
                        "\ndamage to enemies and slowing them.",
                ChatColor.WHITE, 10, 15);
        this.snowballMap = new HashMap<>();
    }

    // spell execute code
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

        // quest code for tutorial island, grab all regions the player is standing in
        // -----------------------------------------------------------------------------------------
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(pl.getLocation()));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return;
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("tutorial_mage")) {

                // ensure player is facing the flames
                if (!DirectionUtil.getDirection(pl).equals("S")) return;
                SpellCastEvent sce = new SpellCastEvent(pl, this);
                Bukkit.getPluginManager().callEvent(sce);
                if (sce.isCancelled()) return;
                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1);
                pl.sendBlockChange(new Location(Bukkit.getWorld("Alterra"), -2336, 37, 1738), Material.ICE.createBlockData());
                pl.sendBlockChange(new Location(Bukkit.getWorld("Alterra"), -2335, 37, 1738), Material.ICE.createBlockData());
            }
        }
        // -----------------------------------------------------------------------------------------
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

            // outlaw check
            if (le instanceof Player && (!OutlawManager.isOutlaw(((Player) le)) || !OutlawManager.isOutlaw(shooter))) {
                return;
            }

            // skip party members
            if (RunicCore.getPartyManager().getPlayerParty(shooter) != null
                    && RunicCore.getPartyManager().getPlayerParty(shooter).hasMember(victim.getUniqueId())) return;

            // apply damage, knockbackPlayer
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, shooter);
            victim.setLastDamageCause(e);

            // apply slow
            if (victim instanceof Player) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
            }
        }
    }

    private void spawnSnowball(Player pl, Location loc, Vector vec) {
        Snowball snowball = pl.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowballMap.put(snowball, pl.getUniqueId());
    }
}

