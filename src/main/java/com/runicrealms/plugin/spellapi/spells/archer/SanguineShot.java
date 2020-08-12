package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class SanguineShot extends Spell {

    private static final int HEAL_AMT = 50;
    private Arrow sanguineArrow;

    public SanguineShot() {
        super("Sanguine Shot",
                "You fire an arrow of blood" +
                        "\nmagic, restoring✦ " + HEAL_AMT + " of your" +
                        "\nhealth if it hits an enemy!",
                ChatColor.WHITE, ClassEnum.ARCHER, 15, 35);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2f);

        sanguineArrow = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        sanguineArrow.setVelocity(vec);
        sanguineArrow.setShooter(pl);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = sanguineArrow.getLocation();
                pl.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc,
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 2));
                pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, arrowLoc, 10, 0, 0, 0, 0);
                if (sanguineArrow.isDead() || sanguineArrow.isOnGround())
                    this.cancel();
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    @EventHandler
    public void onPoisArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!e.getDamager().equals(this.sanguineArrow)) return;
        e.setCancelled(true);
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        LivingEntity le = (LivingEntity) e.getEntity();
        if (!verifyEnemy(pl, le)) return;

        HealUtil.healPlayer(HEAL_AMT, pl, pl, true, false, false);
    }
}
