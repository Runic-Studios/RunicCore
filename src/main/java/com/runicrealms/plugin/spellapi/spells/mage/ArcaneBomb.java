package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

import java.util.Objects;

public class ArcaneBomb extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 15;
    private static final double DAMAGE_PER_LEVEL = 2.5;
    private static final double DURATION = 2.5;
    private static final int RADIUS = 5;
    private ThrownPotion thrownPotion;

    public ArcaneBomb() {
        super("Arcane Bomb",
                "You launch a magical vial of the arcane, " +
                        "dealing (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies within " +
                        RADIUS + " blocks of the impact and silencing them " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        Objects.requireNonNull(meta).setColor(Color.FUCHSIA);
        item.setItemMeta(meta);
        thrownPotion = player.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(item);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(1.25);
        thrownPotion.setVelocity(velocity);
        thrownPotion.setShooter(player);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onPotionBreak(PotionSplashEvent event) {

        // only listen for our fireball
        if (!(event.getPotion().equals(this.thrownPotion))) return;
        if (!(event.getPotion().getShooter() instanceof Player)) return;

        event.setCancelled(true);

        ThrownPotion expiredBomb = event.getPotion();
        Location loc = expiredBomb.getLocation();
        Player player = (Player) event.getPotion().getShooter();

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1.0F);

        expiredBomb.getWorld().spawnParticle(Particle.REDSTONE, loc,
                15, 1f, 1f, 1f, new Particle.DustOptions(Color.FUCHSIA, 3));

        for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity livingEntity = (LivingEntity) entity;
            if (!isValidEnemy(player, livingEntity)) continue;
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.1F, 2.0F);
            livingEntity.getWorld().spawnParticle(Particle.REDSTONE, livingEntity.getLocation(),
                    15, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 1));
            DamageUtil.damageEntitySpell(DAMAGE_AMT, livingEntity, player, this);
            addStatusEffect(livingEntity, EffectEnum.SILENCE, DURATION);
        }
    }
}

