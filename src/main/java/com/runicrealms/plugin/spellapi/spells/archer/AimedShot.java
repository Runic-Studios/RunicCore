package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;
import java.util.Set;

public class AimedShot extends Spell implements PhysicalDamageSpell {
    private static final int DAMAGE = 20;
    private static final int DURATION = 6;
    private static final int MAX_DIST = 48;
    private static final double DAMAGE_PER_LEVEL = 2.75;
    private static final double RAY_SIZE = 1.15D;
    private final Set<ProjectileSource> aimedPlayers;

    public AimedShot() {
        super("Aimed Shot",
                "You aim down your sights, massively slowing yourself for " +
                        DURATION + "s, or until your next shot. " +
                        "Upon firing, the closest enemy within " + RAY_SIZE +
                        " blocks of your scope is struck by an unavoidable arrow, dealing " +
                        "(" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl)" + " physical⚔ damage! " +
                        "Cannot reach a target father than " + MAX_DIST + " blocks.",
                ChatColor.WHITE, CharacterClass.ARCHER, 18, 35);
        aimedPlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 1000000));
        aimedPlayers.add(player);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onAimedShot(RunicBowEvent event) {
        if (!aimedPlayers.contains(event.getPlayer())) return;
        if (event.isCancelled()) return;
        event.setCancelled(true);
        event.getArrow().remove();
        Player player = event.getPlayer();
        aimedPlayers.remove(event.getPlayer());
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
            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
            VectorUtil.drawLine(player, Particle.FLAME, Color.RED, player.getEyeLocation().add(-1.0, 0, 0), location.add(-1.0, 0, 0), 1.0, 15);
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(210, 180, 140), player.getEyeLocation(), location, 1.0, 25);
            VectorUtil.drawLine(player, Particle.FLAME, Color.RED, player.getEyeLocation().add(1, 0, 0), location.add(1, 0, 0), 1.0, 15);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Your arrow could not find a target in range!");
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.25f, 1.0f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
            VectorUtil.drawLine(player, Particle.FLAME, Color.RED, player.getEyeLocation().add(-1.0, 0, 0), livingEntity.getEyeLocation().add(-1.0, 0, 0), 1.0, 15);
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(210, 180, 140), player.getEyeLocation(), livingEntity.getEyeLocation(), 1.0, 25);
            VectorUtil.drawLine(player, Particle.FLAME, Color.RED, player.getEyeLocation().add(1, 0, 0), livingEntity.getEyeLocation().add(1, 0, 0), 1.0, 15);
            DamageUtil.damageEntityPhysical(DAMAGE, livingEntity, player, false, true, this);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
            livingEntity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, livingEntity.getLocation(), 1, 0, 0, 0, 0);
        }

        player.removePotionEffect(PotionEffectType.SLOW);
    }

}
