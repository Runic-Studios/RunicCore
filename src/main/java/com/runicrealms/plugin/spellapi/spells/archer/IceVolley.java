package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TeleportUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class IceVolley extends Spell<SubClassEnum> {

    private final boolean pullOnFinalShot;
    private static final int RADIUS = 5;
    private static final int DAMAGE = 15;
    private static final double GEM_BOOST = 50;
    private final HashMap<Arrow, UUID> vArrows;

    public IceVolley() {
        super("Ice Volley",
                "You rapid-fire a volley of five arrows," +
                        "\neach dealing " + DAMAGE + " spellʔ damage" +
                        "\nand slowing enemies hit!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: " + (int) GEM_BOOST + "%",
                ChatColor.WHITE, SubClassEnum.WARDEN, 6, 30);
        this.vArrows = new HashMap<>();
        this.pullOnFinalShot = false;
    }

    public IceVolley(boolean pullOnFinalShot) {
        super("Ice Volley",
                "You rapid-fire a volley of five arrows," +
                        "\neach dealing " + DAMAGE + " spellʔ damage" +
                        "\nand slowing enemies hit!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: " + (int) GEM_BOOST + "%",
                ChatColor.WHITE, SubClassEnum.WARDEN, 6, 30);
        this.vArrows = new HashMap<>();
        this.pullOnFinalShot = pullOnFinalShot;
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
                    if (count == 5) // final arrow
                        arrow.setMetadata("final", new FixedMetadataValue(plugin, ""));
                    arrow.setVelocity(vector);
                    arrow.setShooter(pl);
                    vArrows.put(arrow, uuid);

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
        if (!vArrows.containsKey(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        if (pl == null) return;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (verifyEnemy(pl, le)) {
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
            e.getEntity().getWorld().spawnParticle(Particle.CRIT_MAGIC, e.getEntity().getLocation(), 1, 0, 0, 0, 0);
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1));
            DamageUtil.damageEntitySpell(DAMAGE, le, pl, 50);
            if (pullOnFinalShot && arrow.hasMetadata("final"))
                pullNearby(pl, le.getLocation());
        }
    }

    /**
     * This method pulls nearby entites to the target entity.
     * @param player who fired the arrow
     * @param location of the 'target' enemy, to which all OTHER enemies will be pulled
     */
    private void pullNearby(Player player, Location location) {
        for (Entity en : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
            if (verifyEnemy(player, en)) {
                en.getWorld().playSound(en.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
                en.getWorld().spawnParticle(Particle.SPELL_WITCH, en.getLocation(), 25, 0, 0, 0, 0);
                TeleportUtil.teleportEntity(en, location);
            }
        }
    }

    public static int getRadius() {
        return RADIUS;
    }
}
