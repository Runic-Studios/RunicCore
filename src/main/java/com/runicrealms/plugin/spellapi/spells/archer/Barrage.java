package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.DirectionUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Barrage extends Spell {

    // globals
    private HashMap<Arrow, UUID> bArrows;
    private HashMap<UUID, UUID> hasBeenHit;
    private static final int DAMAGE = 10;

    // in seconds
    private final int SUCCESSIVE_COOLDOWN = 1;

    // constructor
    public Barrage() {
        super("Barrage",
                "You launch a volley of five magical arrows\n"
                        + "that deal " + DAMAGE + " damage!",
                ChatColor.WHITE, 6, 15);
        this.bArrows = new HashMap<>();
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector leftMid = rotateVectorAroundY(middle, -11.25);
        Vector rightMid = rotateVectorAroundY(middle, 11.25);
        Vector right = rotateVectorAroundY(middle, 22.5);
        startTask(pl, new Vector[]{middle, left, leftMid, rightMid, right});

        // quest code for tutorial island, grab all regions the player is standing in
        // -----------------------------------------------------------------------------------------
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(pl.getLocation()));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return;
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("tutorial_archer")
                    && (!pl.hasPermission("tutorial.complete.archer") || pl.isOp())) {

                // ensure player is facing the targets
                if (!DirectionUtil.getDirection(pl).equals("S")) return;
                pl.chat("barragepass");
            }
        }
        // -----------------------------------------------------------------------------------------
    }

    // vectors, particles
    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow arrow = player.launchProjectile(Arrow.class);
            UUID uuid = player.getUniqueId();
            arrow.setVelocity(vector);
            arrow.setShooter(player);
            bArrows.put(arrow, uuid);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    player.getWorld().spawnParticle(Particle.FLAME, arrowLoc, 5, 0, 0, 0, 0);
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
        }
    }

    // deal bonus damage if arrow is a barrage arrow
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) {
            return;
        }

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // deal magic damage if arrow in in the barrage hashmap
        if (bArrows.containsKey(arrow)) {

            e.setCancelled(true);

            if (!(e.getEntity() instanceof LivingEntity)) return;
            Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
            assert pl != null;
            UUID plID = pl.getUniqueId();
            LivingEntity le = (LivingEntity) e.getEntity();

            // ignore NPCs
            if (le.hasMetadata("NPC")) {
                return;
            }

            // skip party members
            if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                    && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(le.getUniqueId())) { return; }

            if (!hasBeenHit.containsKey(le.getUniqueId())) {

                DamageUtil.damageEntitySpell(DAMAGE, le, pl);
                e.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, e.getEntity().getLocation(), 1, 0, 0, 0, 0);
                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);

                hasBeenHit.put(le.getUniqueId(), plID);
                // remove concussive hit tracker
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hasBeenHit.remove(le.getUniqueId());
                    }
                }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));
            }
        }
    }
}
