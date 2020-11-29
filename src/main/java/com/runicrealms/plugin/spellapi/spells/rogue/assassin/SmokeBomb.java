package com.runicrealms.plugin.spellapi.spells.rogue.assassin;

import com.runicrealms.plugin.classes.ClassEnum;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class SmokeBomb extends Spell {

    private final boolean frostBomb;
    private static final int FROSTBOMB_DURATION = 4;
    private static final int DAMAGE_AMT = 15;
    private static final int DURATION = 2;
    private static final int RADIUS = 5;
    private ThrownPotion thrownPotion;

    public SmokeBomb() {
        super("Smoke Bomb",
                "You fire a cloud of toxic smoke" +
                        "\nthat deals " + DAMAGE_AMT + " spellʔ damage and" +
                        "\nslows enemies within " + RADIUS + " blocks" +
                        "\nfor " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.ROGUE, 6, 15);
        this.frostBomb = false;
    }

    public SmokeBomb(boolean frostBomb) {
        super("Smoke Bomb",
                "You fire a cloud of toxic smoke" +
                        "\nthat deals " + DAMAGE_AMT + " spellʔ damage and" +
                        "\nslows enemies within " + RADIUS + " blocks" +
                        "\nfor " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.ROGUE, 6, 15);
        this.frostBomb = frostBomb;
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.swingMainHand();
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        Objects.requireNonNull(meta).setColor(Color.YELLOW);
        Color thrownPotionColor = frostBomb ? Color.AQUA : Color.YELLOW;
        Objects.requireNonNull(meta).setColor(thrownPotionColor);
        item.setItemMeta(meta);
        thrownPotion = pl.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(item);
        final Vector velocity = pl.getLocation().getDirection().normalize().multiply(1.25);
        thrownPotion.setVelocity(velocity);
        thrownPotion.setShooter(pl);
    }

    @EventHandler
    public void onPotionBreak(PotionSplashEvent e) {

        // only listen for our fireball
        if (!(e.getPotion().equals(this.thrownPotion))) return;
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        e.setCancelled(true);
        ThrownPotion expiredBomb = e.getPotion();
        Location loc = expiredBomb.getLocation();
        Player pl = (Player) e.getPotion().getShooter();

        if (pl == null) return;

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.5F, 1.0F);

        if (!frostBomb) {
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc,
                    50, 1f, 1f, 1f, new Particle.DustOptions(Color.YELLOW, 20));
            damageNearby(pl, loc);
        } else {
            new BukkitRunnable() {
                int count = 1;
                @Override
                public void run() {
                    if (count > FROSTBOMB_DURATION)
                        this.cancel();
                    else {
                        count += 1;
                        pl.getWorld().spawnParticle(Particle.REDSTONE, loc,
                                50, 1f, 1f, 1f, new Particle.DustOptions(Color.AQUA, 20));
                        damageNearby(pl, loc);
                    }
                }
            }.runTaskTimer(plugin, 0, 20L);
        }
    }

    private void damageNearby(Player pl, Location loc) {
        for (Entity entity : pl.getWorld().getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof LivingEntity && verifyEnemy(pl, entity)) {
                LivingEntity victim = (LivingEntity) entity;
                DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl, 100);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 2));
            }
        }
    }

    public static int getFrostbombDuration() {
        return FROSTBOMB_DURATION;
    }
}

