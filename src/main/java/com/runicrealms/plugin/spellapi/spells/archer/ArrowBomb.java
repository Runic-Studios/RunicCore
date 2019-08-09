package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ArrowBomb extends Spell {

    // globals
    private HashMap<Arrow, UUID> bombArrow;
    private static final int DAMAGE = 15;
    private static final int RADIUS = 4;

    // constructor
    public ArrowBomb() {
        super("Arrow Bomb",
                "You launch a flaming arrow that ignites" +
                        "\non-hit, dealing " + DAMAGE + " spellʔ damage to all" +
                        "\nenemies within " + RADIUS + " blocks!",
                ChatColor.WHITE, 8, 15);
        this.bombArrow = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        Vector path = pl.getEyeLocation().getDirection().normalize().multiply(2);
        startTask(pl, path);
    }

    // vectors, particles
    private void startTask(Player player, Vector vector) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        UUID uuid = player.getUniqueId();
        arrow.setVelocity(vector);
        arrow.setShooter(player);
        bombArrow.put(arrow, uuid);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = arrow.getLocation();
                arrowLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, arrowLoc, 5, 0, 0, 0, 0);
                if (arrow.isDead() || arrow.isOnGround()) {
                    this.cancel();

                    // particles, sounds
                    arrow.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, arrow.getLocation(), 1, 0, 0, 0, 0);
                    arrow.getWorld().spawnParticle(Particle.REDSTONE, arrow.getLocation(),
                            25, 0, 2f, 2f, 2f, new Particle.DustOptions(Color.ORANGE, 20));
                    arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);

                    // hit them baddies
                    for (Entity e : arrow.getWorld().getNearbyEntities(arrow.getLocation(), RADIUS, RADIUS, RADIUS)) {
                        tryToDamage(arrow, e);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
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

        // cancel the arrow damage event
        if (bombArrow.containsKey(arrow)) {
            e.setCancelled(true);
        }
    }

    private void tryToDamage(Arrow arrow, Entity victim) {

        if (!(victim instanceof LivingEntity)) return;
        Player pl = (Player) arrow.getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) victim;

        // ignore NPCs
        if (le.hasMetadata("NPC")) {
            return;
        }

        // outlaw check
        if (le instanceof Player && (!OutlawManager.isOutlaw(((Player) le)) || !OutlawManager.isOutlaw(pl))) {
            return;
        }

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(le.getUniqueId())) { return; }

        DamageUtil.damageEntitySpell(DAMAGE, le, pl, false);
    }
}
