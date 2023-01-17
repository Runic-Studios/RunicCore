package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Stormborn extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 20;
    private static final int DURATION = 7;
    private static final double DAMAGE_PER_LEVEL = 1.0;
    private final Set<UUID> stormPlayers = new HashSet<>();
    private final HashMap<UUID, UUID> hasBeenHit = new HashMap<>();

    public Stormborn() {
        super("Stormborn",
                "You channel the storm for " + DURATION + "s! " +
                        "While the storm persists, each basic attack you fire " +
                        "will instead launch 3 empowered arrows in a cone! " +
                        "The empowered arrows deal an additional (" +
                        DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 14, 40);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.0f);
        stormPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> stormPlayers.remove(player.getUniqueId()), DURATION * 20L);
    }

    private void fireArrow(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow arrow = player.launchProjectile(Arrow.class);
//            arrow.
            arrow.setVelocity(vector);
            arrow.setShooter(player);
            arrow.setCustomNameVisible(false);
            arrow.setCustomName("autoAttack");
            arrow.setMetadata("data", new FixedMetadataValue(RunicCore.getInstance(), "storm shot"));
            arrow.setBounce(false);
            EntityTrail.entityTrail(arrow, Particle.CRIT_MAGIC);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!event.isBasicAttack()) return;
        if (!stormPlayers.contains(event.getPlayer().getUniqueId())) return;
        if (hasBeenHit.containsKey(event.getPlayer().getUniqueId())) return;
        DamageUtil.damageEntitySpell(DAMAGE, event.getVictim(), event.getPlayer(), this);
        hasBeenHit.put(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId()); // prevent concussive hits
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> hasBeenHit.remove(event.getPlayer().getUniqueId()), 8L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRunicBowEvent(RunicBowEvent event) {
        if (!stormPlayers.contains(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        event.getArrow().remove();
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5f, 0.75f);
        Vector middle = player.getEyeLocation().getDirection().normalize().multiply(2);
        Vector leftMid = rotateVectorAroundY(middle, -10);
        Vector rightMid = rotateVectorAroundY(middle, 10);
        fireArrow(player, new Vector[]{middle, leftMid, rightMid});
    }

}
