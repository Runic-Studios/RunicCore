package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;
import java.util.Set;

public class HomingShot extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 20;
    private static final int DURATION = 6;
    private static final int MAX_DIST = 50;
    private static final int RADIUS = 5;
    private static final double DAMAGE_PER_LEVEL = 2.75;
    private static final double RAY_SIZE = 2.5D;
    private final Set<ProjectileSource> honingPlayers;

    public HomingShot() {
        super("Homing Shot",
                "You aim down your sights, massively slowing yourself for " +
                        DURATION + "s, or until your next shot. " +
                        "You aim at a block within " + MAX_DIST + " blocks, and the " +
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
        honingPlayers.remove(event.getEntity().getShooter());
        player.removePotionEffect(PotionEffectType.SLOW);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 1000000));
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            player.sendMessage("your ray trace was null. your cooldown was partially refunded");
            RunicCore.getSpellAPI().reduceCooldown(player, this, this.getCooldown() / 2);
        } else if (rayTraceResult.getHitEntity() != null) { // todo: distance might be too far now
            // todo: nausea? knockback?
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.25f, 1.0f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
            VectorUtil.drawLine(player, Particle.FLAME, Color.RED, player.getEyeLocation(), livingEntity.getEyeLocation(), 1.0);
            DamageUtil.damageEntityPhysical(DAMAGE, livingEntity, player, false, true, this);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            livingEntity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, livingEntity.getLocation(), 1, 0, 0, 0, 0);
        }
        // todo: renamed to 'aimed shot' and change ultimate passive to 'homing arrows?' keep headshot
        player.removePotionEffect(PotionEffectType.SLOW);
    }

}
