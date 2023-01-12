package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Set;

public class HomingShot extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 20;
    private static final int DURATION = 6;
    private static final int MAX_DIST = 50;
    private static final int RADIUS = 5;
    private static final double DAMAGE_PER_LEVEL = 2.75;
    private final Set<ProjectileSource> honingPlayers;

    public HomingShot() {
        super("Homing Shot",
                "You aim down your sights, massively slowing yourself for " +
                        DURATION + "s, or until your next shot. " +
                        "You aim at a location within " + MAX_DIST + " blocks, and the " +
                        "closest enemy within " + RADIUS +
                        " blocks of your target " +
                        "location will be hit by an unavoidable arrow, dealing " +
                        "(" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl)" + " physical⚔ damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 18, 35);
        honingPlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 1000000));
        honingPlayers.add(player);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onHoningShot(ProjectileLaunchEvent event) {
        if (!honingPlayers.contains(event.getEntity().getShooter())) return;
        event.setCancelled(true);
        Player player = (Player) event.getEntity().getShooter();
        assert player != null;
        Location targetLocation = player.getTargetBlock(null, MAX_DIST).getLocation();
        honingPlayers.remove(event.getEntity().getShooter());
        player.removePotionEffect(PotionEffectType.SLOW);
        for (Entity entity : player.getWorld().getNearbyEntities(targetLocation, RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(player, entity)) continue;
            entity.getWorld().spawnParticle(Particle.FLAME, entity.getLocation(), 15, 0.25f, 0.25f, 0.25f, 0);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.25f, 1.0f);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.2f);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
            DamageUtil.damageEntityPhysical(DAMAGE, (LivingEntity) entity, player, false, true, this);
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
        player.sendMessage(ChatColor.RED + "You arrow could not find a target in range!");
    }
}
