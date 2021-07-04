package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;

@SuppressWarnings("FieldCanBeLocal")
public class IceVolley extends Spell {

    private static final int DAMAGE = 25;
    private static final int DURATION = 2;
    private final HashSet<Arrow> iceArrows;

    public IceVolley() {
        super("Ice Volley",
                "You rapid-fire a volley of five arrows, " +
                        "each dealing " + DAMAGE + " spellʔ damage " +
                        "and slowing any enemies hit for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 10, 30);
        iceArrows = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > 5)
                    this.cancel();
                else {
                    count += 1;
                    Vector vector = pl.getEyeLocation().getDirection().normalize().multiply(2);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
                    Arrow arrow = pl.launchProjectile(Arrow.class);
                    arrow.setVelocity(vector);
                    arrow.setShooter(pl);
                    iceArrows.add(arrow);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location arrowLoc = arrow.getLocation();
                            arrow.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
                            arrow.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
                            if (arrow.isDead() || arrow.isOnGround())
                                this.cancel();
                        }
                    }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 3L);
    }

    // deal bonus damage if arrow is a barrage arrow
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) return;

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        // deal magic damage if arrow in in the barrage hashmap
        if (!iceArrows.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        if (pl == null) return;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (verifyEnemy(pl, le)) {
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
            e.getEntity().getWorld().spawnParticle(Particle.CRIT_MAGIC, e.getEntity().getLocation(), 1, 0, 0, 0, 0);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (DURATION * 20L), 2));
            DamageUtil.damageEntitySpell(DAMAGE, le, pl, this);
        }
    }
}
