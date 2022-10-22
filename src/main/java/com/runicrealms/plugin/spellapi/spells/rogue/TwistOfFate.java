package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.HealthRegenEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

@SuppressWarnings("FieldCanBeLocal")
public class TwistOfFate extends Spell {

    private static final int BUBBLE_DURATION = 6;
    private static final int BUBBLE_SIZE = 5;
    private static final double UPDATES_PER_SECOND = 10;
    private final HashSet<BukkitTask> coneTasks;
    private final HashSet<UUID> fateCasters;
    private final HashMap<UUID, Player> invertedEntities;

    public TwistOfFate() {
        super("Twist of Fate",
                "For " + BUBBLE_DURATION + "s, you conjure a " +
                        "wicked sphere of inversion, affecting " +
                        "all enemies within " + BUBBLE_SIZE + " blocks! " +
                        "Enemies who enter the sphere are debuffed for the " +
                        "duration of this spell and cannot be healed✦, " +
                        "suffering magicʔ damage instead! During this time, you may not move. " +
                        "Sneak to cancel the spell early. Against monsters, " +
                        "this ability will have no effect.",
                ChatColor.WHITE, ClassEnum.ROGUE, 45, 40);
        coneTasks = new HashSet<>();
        fateCasters = new HashSet<>();
        invertedEntities = new HashMap<>();
    }

    @Override
    public boolean attemptToExecute(Player pl) {
        if (!pl.isOnGround()) {
            pl.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
            return false;
        }
        return true;
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // Play sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        //pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5F, 0.5F);
        pl.getWorld().spigot().strikeLightningEffect(pl.getLocation(), true);
        fateCasters.add(pl.getUniqueId());

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI / 10;
                Location loc = pl.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = BUBBLE_SIZE * cos(theta) * sin(phi);
                    double y = BUBBLE_SIZE * cos(phi) + 1.5;
                    double z = BUBBLE_SIZE * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    pl.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > BUBBLE_DURATION * 1000 || pl.isSneaking()) {
                    this.cancel();
                    for (BukkitTask bukkitTask : coneTasks) {
                        bukkitTask.cancel();
                    }
                    coneTasks.clear();
                    fateCasters.clear();
                    invertedEntities.clear();
                    return;
                }

                // More effect noises
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);

                // Look for targets nearby
                for (Entity entity : pl.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {
                    if (!verifyEnemy(pl, entity)) continue;
                    if (invertedEntities.containsKey(entity.getUniqueId())) continue;
                    invertedEntities.put(entity.getUniqueId(), pl);
                    BukkitTask cone = Cone.coneEffect((LivingEntity) entity, Particle.SMOKE_NORMAL, BUBBLE_DURATION, 0, 20, Color.BLACK);
                    coneTasks.add(cone);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20 / UPDATES_PER_SECOND));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!fateCasters.contains(e.getPlayer().getUniqueId())) return;
        if (e.getTo() == null) return;
        if (!e.getFrom().toVector().equals(e.getTo().toVector())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHeal(SpellHealEvent e) {
        if (!invertedEntities.containsKey(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
        DamageUtil.damageEntitySpell(e.getAmount(), (LivingEntity) e.getEntity(), invertedEntities.get(e.getEntity().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRegen(HealthRegenEvent e) {
        if (!invertedEntities.containsKey(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
        DamageUtil.damageEntitySpell(e.getAmount(), e.getPlayer(), invertedEntities.get(e.getPlayer().getUniqueId()));
    }
}