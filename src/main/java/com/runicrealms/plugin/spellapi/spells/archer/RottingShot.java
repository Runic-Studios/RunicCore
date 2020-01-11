package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
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
public class RottingShot extends Spell {

    private static final int DAMAGE = 3;
    private static final int PERIOD = 2;
    private static final int DURATION = 8;
    private List<Arrow> poisonedArrs = new ArrayList<>();

    public RottingShot() {
        super("Rotting Shot",
                "You launch an unholy arrow which" +
                "\ndeals " + DAMAGE + " spellʔ damage every " + PERIOD + " seconds" +
                "\nfor " + DURATION + " seconds to its target." +
                "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, 16, 15);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);
        Arrow poisoned = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        poisoned.setVelocity(vec);
        poisoned.setShooter(pl);
        poisonedArrs.add(poisoned);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = poisoned.getLocation();
                pl.getWorld().spawnParticle(Particle.SLIME, arrowLoc, 5, 0, 0, 0, 0);
                pl.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.YELLOW, 1));
                if (poisoned.isDead() || poisoned.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    @EventHandler
    public void onPoisArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) {
            return;
        }

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // deal magic damage if arrow in in the barrage hashmap
        if (!poisonedArrs.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (!verifyEnemy(pl, le)) return;
        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();

                } else {

                    count += PERIOD;
                    DamageUtil.damageEntitySpell(DAMAGE, le, pl, true);
                    le.getWorld().spawnParticle(Particle.SLIME, le.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
                    le.getWorld().playSound(le.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 1);

                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD*20L);
    }
}
