package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class KillShot extends Spell {

    private static final int DAMAGE = 50;
    private static final int DAMAGE_CAP = 300;
    private static final double PERCENT = .25;
    private final List<Arrow> killShots;

    public KillShot() {
        super("Kill Shot",
                "You launch an enchanted arrow which " +
                        "deals " + "(" + DAMAGE + " + &f" + PERCENT + "x&7 " +
                        "missing health) as weapon⚔ damage! " +
                        "Capped at " + DAMAGE_CAP + " against monsters.",
                ChatColor.WHITE, ClassEnum.ARCHER, 8, 25);
        killShots = new ArrayList<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        startTask(pl, vec);
    }

    private void startTask(Player pl, Vector vector) {
        Arrow powerShot = pl.launchProjectile(Arrow.class);
        powerShot.setVelocity(vector);
        powerShot.setShooter(pl);
        killShots.add(powerShot);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = powerShot.getLocation();
                powerShot.getWorld().spawnParticle(Particle.FLAME, arrowLoc,
                        10, 0, 0, 0, 0);
                if (powerShot.isDead() || powerShot.isOnGround()) {
                    this.cancel();
                    pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                    powerShot.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 3, 0.5f, 0.5f, 0.5f, 0);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }


    @EventHandler
    public void onSearingArrowHit(EntityDamageByEntityEvent e) {

        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!killShots.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (!verifyEnemy(pl, le)) return;

        int amount = DAMAGE + percentMissingHealth(le, PERCENT);
        if (!(le instanceof Player) && amount > DAMAGE_CAP)
            amount = DAMAGE_CAP;
        DamageUtil.damageEntityWeapon(amount, le, pl, false, true);
    }
}
