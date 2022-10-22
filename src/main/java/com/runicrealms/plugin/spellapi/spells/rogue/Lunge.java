package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;

@SuppressWarnings("FieldCanBeLocal")
public class Lunge extends Spell {

    private static final double LAUNCH_PATH_MULT = 1.5;
    private static final double PERCENT_MULT = 2.0;
    private final HashSet<Entity> lungers;

    public Lunge() {
        super("Lunge",
                "You lunge forward into the air! " +
                        "Your next weapon⚔ attack deals " +
                        (int) (PERCENT_MULT * 100) + "% damage!",
                ChatColor.WHITE, ClassEnum.ROGUE, 8, 15);
        lungers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // spell variables, vectors
        Location location = pl.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(look.getX(), 0, look.getZ()).normalize();

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.8f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));

        pl.teleport(location.add(0, 0.5, 1));
        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));

        lungers.add(pl);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> lungers.remove(pl),
                (long) (this.getCooldown() - 2) * 20L);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (!lungers.contains(e.getEntity())) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // fires FIRST
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (!lungers.contains(e.getPlayer())) return;
        e.setAmount((int) (e.getAmount() * PERCENT_MULT));
        e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        SlashEffect.slashHorizontal(e.getPlayer());
        lungers.remove(e.getPlayer());
    }
}
