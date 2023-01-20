package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Stormborn extends Spell {
    private static final int DAMAGE = 20;
    private static final double DAMAGE_PER_LEVEL = 1.0;
    private final Map<UUID, Integer> stormPlayers = new HashMap<>();
    private final HashMap<UUID, UUID> hasBeenHit = new HashMap<>();

    public Stormborn() {
        super("Stormborn",
                "After casting an ability, your next three basic attacks " +
                        "are infused with the storm! Each basic attack you fire " +
                        "will instead launch 3 empowered arrows in a cone! " +
                        "The empowered arrows deal an additional (" +
                        DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 14, 40);
        this.setIsPassive(true);
    }

    private void fireArrow(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow arrow = player.launchProjectile(Arrow.class);
//            arrow.
            // todo: don't let multiple arrows hit
            // todo: does it scale properly?
            arrow.setVelocity(vector);
            arrow.setShooter(player);
            arrow.setCustomNameVisible(false);
            arrow.setCustomName("autoAttack");
            arrow.setMetadata("data", new FixedMetadataValue(RunicCore.getInstance(), "storm shot"));
            arrow.setBounce(false);
            EntityTrail.entityTrail(arrow, Particle.CRIT_MAGIC);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!event.isBasicAttack()) return;
        if (!stormPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (hasBeenHit.containsKey(event.getPlayer().getUniqueId())) return;
        DamageUtil.damageEntitySpell(DAMAGE, event.getVictim(), event.getPlayer(), this);
        hasBeenHit.put(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId()); // prevent concussive hits
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> hasBeenHit.remove(event.getPlayer().getUniqueId()), 8L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRunicBowEvent(RunicBowEvent event) {
        if (!stormPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        stormPlayers.put(event.getPlayer().getUniqueId(), stormPlayers.get(event.getPlayer().getUniqueId()) - 1);
        if (stormPlayers.get(event.getPlayer().getUniqueId()) <= 0)
            stormPlayers.remove(event.getPlayer().getUniqueId());
        event.setCancelled(true);
        event.getArrow().remove();
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.25f, 0.75f);
        Vector middle = player.getEyeLocation().getDirection().normalize().multiply(2);
        Vector leftMid = rotateVectorAroundY(middle, -10);
        Vector rightMid = rotateVectorAroundY(middle, 10);
        fireArrow(player, new Vector[]{middle, leftMid, rightMid});
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        stormPlayers.put(event.getCaster().getUniqueId(), 3);
    }

}
