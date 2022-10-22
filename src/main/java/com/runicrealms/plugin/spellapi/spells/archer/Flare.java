package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.spellapi.spells.rogue.Cloak;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;

@SuppressWarnings("FieldCanBeLocal")
public class Flare extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 15;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final int DURATION = 3;
    private static final int RADIUS = 5;
    private static final double FLARE_SPEED = 2;
    private final HashSet<Arrow> flares;

    public Flare() {
        super("Flare",
                "You launch a flare that deals (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) " +
                        "magicʔ damage to the first enemy hit, then dispells " +
                        "the beneficial effects of enemies within " + RADIUS + " " +
                        "blocks, removing speed and invisibility! The flare then " +
                        "dazes its targets, causing blindness and " +
                        "slowing them for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 12, 15);
        flares = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(FLARE_SPEED);
        Arrow flare = pl.launchProjectile(Arrow.class);
        flare.setVelocity(vec);
        flare.setShooter(pl);
        flares.add(flare);
        EntityTrail.entityTrail(flare, Particle.FLAME);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (flare.isDead() || flare.isOnGround()) {
                    this.cancel();
                    flares.remove(flare);
                    flareEffect(pl, flare);
                }
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    @EventHandler
    public void onFlareArrowHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!flares.contains(arrow)) return;
        e.setCancelled(true);
        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();
        if (!verifyEnemy(pl, le)) return;
        DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, this);
    }

    private void flareEffect(Player player, Arrow arrow) {
        for (Entity en : player.getWorld().getNearbyEntities(arrow.getLocation(), RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(player, en)) continue;
            LivingEntity le = (LivingEntity) en;
            le.getWorld().playSound(le.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 0.5f, 1.0f);
            ClassUtil.launchFirework(le, Color.RED);
            Cloak.getMarkedForEarlyReveal().add(le.getUniqueId());
            le.removePotionEffect(PotionEffectType.SPEED);
            le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (DURATION * 20L), 2));
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (DURATION * 20L), 2));
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
