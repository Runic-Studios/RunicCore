package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class IceVolley extends Spell {

    private HashMap<Arrow, UUID> vArrows;
    private static final int DAMAGE = 15;

    public IceVolley() {
        super("Ice Volley",
                "You rapid-fire a volley of five arrows," +
                        "\neach dealing " + DAMAGE + " spellʔ damage" +
                        "\nand slowing enemies hit!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, 6, 15);
        this.vArrows = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        UUID uuid = pl.getUniqueId();

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > 5) {
                    this.cancel();
                } else {

                    count += 1;
                    Vector vector = pl.getEyeLocation().getDirection().normalize().multiply(2);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
                    Arrow arrow = pl.launchProjectile(Arrow.class);
                    arrow.setVelocity(vector);
                    arrow.setShooter(pl);
                    vArrows.put(arrow, uuid);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location arrowLoc = arrow.getLocation();
                            arrow.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
                            arrow.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
                            if (arrow.isDead() || arrow.isOnGround()) {
                                this.cancel();
                            }
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
        if (!(e.getDamager() instanceof Arrow)) {
            return;
        }

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // deal magic damage if arrow in in the barrage hashmap
        if (vArrows.containsKey(arrow)) {

            e.setCancelled(true);

            if (!(e.getEntity() instanceof LivingEntity)) return;
            Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
            assert pl != null;
            LivingEntity le = (LivingEntity) e.getEntity();

            if (verifyEnemy(pl, le)) {
                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
                e.getEntity().getWorld().spawnParticle(Particle.CRIT_MAGIC, e.getEntity().getLocation(), 1, 0, 0, 0, 0);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1));
                DamageUtil.damageEntitySpell(DAMAGE, le, pl, true);
            }
        }
    }
}
