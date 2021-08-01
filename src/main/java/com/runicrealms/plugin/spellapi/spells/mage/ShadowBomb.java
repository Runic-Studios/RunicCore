package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class ShadowBomb extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 4;
    private static final double DAMAGE_PER_LEVEL = .25;
    private static final int DURATION = 6;
    private static final int PERIOD = 2;
    private static final int RADIUS = 5;
    private ThrownPotion thrownPotion;

    public ShadowBomb() {
        super("Shadow Bomb",
                "You launch a magical vial of shadow, " +
                        "dealing (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) spellʔ damage per second for " +
                        DURATION + "s to enemies within " +
                        RADIUS + " blocks of the cloud. ",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 25);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        Objects.requireNonNull(meta).setColor(Color.PURPLE);
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

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_CAT_HISS, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1.0F);

        expiredBomb.getWorld().spawnParticle(Particle.REDSTONE, loc,
                50, 1f, 1f, 1f, new Particle.DustOptions(Color.PURPLE, 10));

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (!(en instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity) en;
            if (!verifyEnemy(pl, le)) continue;
            damageOverTime(le, pl);
            // Doom (passive)
            if (pl == null) continue;
            if (Shadowmeld.getDoomers().contains(pl.getUniqueId()))
                addStatusEffect(le, EffectEnum.SILENCE, Shadowmeld.getDuration());
        }
    }

    private void damageOverTime(LivingEntity le, Player pl) {
        Spell spell = this;
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= DURATION) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    le.getWorld().playSound(le.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.1F, 2.0F);
                    le.getWorld().spawnParticle(Particle.REDSTONE, le.getLocation(),
                            50, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.GREEN, 1));
                    DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, spell);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

