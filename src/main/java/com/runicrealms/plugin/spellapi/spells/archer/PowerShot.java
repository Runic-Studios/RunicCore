package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class PowerShot extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 15;
    private static final double DAMAGE_PER_LEVEL = 2.5;
    private static final int DURATION = 6;
    private static final double PERCENT_INCREASE = .25;
    private static final int RADIUS = 3;
    private final List<Arrow> powerShots;
    private static final HashMap<UUID, UUID> markedEntities = new HashMap<>();

    public PowerShot() {
        super("Power Shot",
                "You launch an enchanted arrow which " +
                        "deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl)" + " magicʔ damage on-hit to " +
                        "all enemies within " + RADIUS + " blocks! " +
                        "For " + DURATION + "s, enemies hit by this spell are afflicted " +
                        "with &aHunter's Mark&7, increasing all damage you " +
                        "deal to them by " + (int) (PERCENT_INCREASE * 100) + "%!",
                ChatColor.WHITE, ClassEnum.ARCHER, 12, 25);
        powerShots = new ArrayList<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);
        Vector vec = player.getEyeLocation().getDirection().normalize().multiply(2);
        startTask(player, vec);
    }

    private void startTask(Player player, Vector vector) {
        Arrow powerShot = player.launchProjectile(Arrow.class);
        powerShot.setVelocity(vector);
        powerShot.setShooter(player);
        powerShots.add(powerShot);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = powerShot.getLocation();
                powerShot.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc,
                        10, 0, 0, 0, 0);
                if (powerShot.isDead() || powerShot.isOnGround()) {
                    this.cancel();
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                    powerShot.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 10, 0, 0, 0, 0);
                    powerShot.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 25, 0.5f, 0.5f, 0.5f, 0);
                    for (Entity entity : player.getWorld().getNearbyEntities(arrowLoc, RADIUS, RADIUS, RADIUS)) {
                        if (!(entity instanceof LivingEntity)) continue;
                        if (!isValidEnemy(player, entity)) continue;
                        applyHuntersMark(player, (LivingEntity) entity);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }


    @EventHandler
    public void onSearingArrowHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!powerShots.contains(arrow)) return;
        e.setCancelled(true);
    }

    private void applyHuntersMark(Player pl, LivingEntity le) {
        DamageUtil.damageEntitySpell(DAMAGE, le, pl, this);
        markedEntities.put(pl.getUniqueId(), le.getUniqueId());
        Cone.coneEffect(le, Particle.REDSTONE, DURATION, 0, 20L, Color.GREEN);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> markedEntities.remove(pl.getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        e.setAmount((int) huntersMarkDamage(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        e.setAmount((int) huntersMarkDamage(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    private double huntersMarkDamage(Player caster, Entity victim, double eventDamage) {
        if (!markedEntities.containsKey(caster.getUniqueId())) return eventDamage;
        if (markedEntities.get(caster.getUniqueId()) != victim.getUniqueId()) return eventDamage;
        return eventDamage + (eventDamage * PERCENT_INCREASE);
    }

    public static HashMap<UUID, UUID> huntersMarkMap() {
        return markedEntities;
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
