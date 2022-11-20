package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class NetShot extends Spell implements PhysicalDamageSpell {

    private static final int AMPLIFIER = 1;
    private static final int DAMAGE = 25;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final int DURATION = 3;
    private static final int RADIUS = 5;
    private Arrow bindingArrow;

    public NetShot() {
        super("Net Shot",
                "You fire an arrow that deploys a net, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) physical⚔ damage to its " +
                        "primary target and slowing all enemies within " +
                        RADIUS + " blocks for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 16, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 2f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2f);
        bindingArrow = player.launchProjectile(Arrow.class);
        Vector vector = player.getEyeLocation().getDirection().normalize().multiply(2);
        bindingArrow.setVelocity(vector);
        bindingArrow.setShooter(player);
        EntityTrail.entityTrail(bindingArrow, Color.WHITE);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onBindingArrowHit(EntityDamageByEntityEvent event) {

        // only listen for arrows
        if (!event.getDamager().equals(this.bindingArrow)) return;
        event.setCancelled(true);
        Player player = (Player) ((Arrow) event.getDamager()).getShooter();
        if (player == null) return;
        LivingEntity target = (LivingEntity) event.getEntity();
        if (!isValidEnemy(player, target)) return;

        DamageUtil.damageEntityPhysical(DAMAGE, target, player, false, true, this);

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, AMPLIFIER));
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
        player.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 10, 0.5f, 0.5f, 0.5f, 0);

        for (Entity entity : target.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(player, entity)) continue;
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, AMPLIFIER));
            player.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
            player.getWorld().spawnParticle(Particle.CLOUD, entity.getLocation(), 10, 0.5f, 0.5f, 0.5f, 0);
        }
    }
}
