package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.RangedDamageEvent;
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
    private static final String ARROW_META_KEY = "data";
    private static final String ARROW_META_VALUE = "storm shot";
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

    private Arrow fireArrow(Player player, Vector vector) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(vector);
        arrow.setShooter(player);
        arrow.setCustomNameVisible(false);
        arrow.setCustomName("autoAttack");
        arrow.setMetadata(ARROW_META_KEY, new FixedMetadataValue(RunicCore.getInstance(), ARROW_META_VALUE));
        arrow.setBounce(false);
        EntityTrail.entityTrail(arrow, Particle.CRIT_MAGIC);
        return arrow;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPhysicalDamage(RangedDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!event.isBasicAttack()) return;
        Arrow arrow = event.getArrow();
        if (!arrow.hasMetadata(ARROW_META_KEY)) return;
        if (!arrow.getMetadata(ARROW_META_KEY).get(0).asString().equalsIgnoreCase(ARROW_META_VALUE)) return;
        if (hasBeenHit.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        DamageUtil.damageEntitySpell(DAMAGE + (DAMAGE_PER_LEVEL * event.getPlayer().getLevel()), event.getVictim(), event.getPlayer(), this);
        hasBeenHit.put(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId()); // prevent concussive hits
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> hasBeenHit.remove(event.getPlayer().getUniqueId()), 8L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRunicBowEvent(RunicBowEvent event) {
        if (!stormPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        event.getArrow().remove();
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.25f, 0.75f);
        Vector middle = player.getEyeLocation().getDirection().normalize().multiply(2);
        Vector leftMid = rotateVectorAroundY(middle, -10);
        Vector rightMid = rotateVectorAroundY(middle, 10);
        Vector[] vectors = new Vector[]{middle, leftMid, rightMid};
        for (Vector vector : vectors) {
            event.setArrow(fireArrow(player, vector));
        }
        stormPlayers.put(player.getUniqueId(), stormPlayers.get(player.getUniqueId()) - 1);
        if (stormPlayers.get(event.getPlayer().getUniqueId()) <= 0)
            stormPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        stormPlayers.put(event.getCaster().getUniqueId(), 3);
    }

}
