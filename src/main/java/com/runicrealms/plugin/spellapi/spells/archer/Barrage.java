package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class Barrage extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 4;
    private static final double DAMAGE_PER_LEVEL = 0.4;
    private final HashMap<Arrow, UUID> bArrows;

    public Barrage() {
        super("Barrage",
                "You rapid-fire a volley of five arrows, " +
                        "each dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) physical⚔ damage on-hit!",
                ChatColor.WHITE, CharacterClass.ARCHER, 6, 10);
        this.bArrows = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > 5) {
                    this.cancel();
                } else {
                    count += 1;
                    Vector vector = player.getEyeLocation().getDirection().normalize().multiply(2);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(vector);
                    arrow.setShooter(player);
                    bArrows.put(arrow, uuid);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location arrowLoc = arrow.getLocation();
                            arrow.getWorld().spawnParticle(Particle.CRIT, arrowLoc, 5, 0, 0, 0, 0);
                            if (arrow.isDead() || arrow.isOnGround()) {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 3L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    // deal bonus damage if arrow is a barrage arrow
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent event) {

        // only listen for arrows
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        // Listen for player fired arrow
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // Deal magic damage if arrow is in the barrage hashmap
        if (bArrows.containsKey(arrow)) {

            event.setCancelled(true);

            if (!(event.getEntity() instanceof LivingEntity)) return;
            Player player = (Player) ((Arrow) event.getDamager()).getShooter();
            assert player != null;
            LivingEntity livingEntity = (LivingEntity) event.getEntity();

            if (isValidEnemy(player, livingEntity)) {
                event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 2.0f);
                event.getEntity().getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation(), 1, 0, 0, 0, 0);
                DamageUtil.damageEntityPhysical(DAMAGE, livingEntity, player, false, true, this);
            }
        }
    }
}
