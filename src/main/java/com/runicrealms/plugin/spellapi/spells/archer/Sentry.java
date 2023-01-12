package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.ArmorStandAPI;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class Sentry extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 25;
    private static final int DURATION = 8;
    private static final int RADIUS = 8;
    private static final double DAMAGE_PER_LEVEL = 1.15;
    private Arrow arrow;

    public Sentry() {
        super("Sentry",
                "You build an enchanted " +
                        "crossbow at your location! " +
                        "For " + DURATION + "s, the crossbow fires " +
                        "at all enemies within " + RADIUS + " blocks, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 30, 75);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        SentryCrossbow sentryCrossbow = new SentryCrossbow(player);
        sentryCrossbow.summonCrossbow();
        player.getWorld().playSound(sentryCrossbow.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
        player.getWorld().spawnParticle(Particle.REDSTONE, sentryCrossbow.getLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.AQUA, 5));
        String name = player.getName();
        Location standLocation = sentryCrossbow.getArmorStand().getEyeLocation();

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    sentryCrossbow.destroy();
                } else {
                    count += 1;
                    player.getWorld().spawnParticle
                            (
                                    Particle.CRIT_MAGIC,
                                    standLocation.clone().add(0, 1, 0),
                                    25, 0.25f, 0.25f, 0.25f, 0
                            );
                    for (Entity en : sentryCrossbow.getArmorStand().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (!isValidEnemy(player, en)) continue;
                        Location newLocation = standLocation.clone().setDirection(en.getLocation().subtract(standLocation).toVector());
                        sentryCrossbow.getArmorStand().teleport(newLocation);
                        final Vector direction =
                                ((LivingEntity) en).getEyeLocation().toVector()
                                        .subtract(standLocation.clone().add(0, 1, 0).toVector());
                        arrow = player.getWorld().spawnArrow
                                (
                                        standLocation.clone().add(0, 1, 0), direction, (float) 2, (float) 0
                                );
                        EntityTrail.entityTrail(arrow, Particle.CRIT_MAGIC);
                        // DON'T set the shooter here so the DamageListener class won't take over, use meta instead
                        arrow.setMetadata("player", new FixedMetadataValue(plugin, name));
                        arrow.setCustomNameVisible(false);
                        arrow.setCustomName("autoAttack");
                        arrow.setBounce(false);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    /**
     * Deal correct damage when a turret arrow is fired
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustomArrowHit(EntityDamageByEntityEvent event) {
        if (!event.getDamager().hasMetadata("player")) return;
        event.setCancelled(true);
        Player caster = Bukkit.getPlayer(event.getDamager().getMetadata("player").get(0).asString());
        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) event.getEntity(), caster, this);
    }

    /**
     * An armor stand with a hologram and crossbow
     */
    static class SentryCrossbow {
        private final ArmorStand armorStand;
        private final Hologram hologram;
        private final Location location;

        public SentryCrossbow(Player player) {
            this.armorStand = ArmorStandAPI.spawnArmorStand(player.getLocation());
            this.hologram = HologramsAPI.createHologram
                    (
                            RunicCore.getInstance(),
                            player.getLocation().getBlock().getLocation().clone().add(0.5, 2.0, 0.5)
                    );
            this.location = player.getLocation();
            hologram.appendTextLine(ChatColor.WHITE + player.getName() + "'s " + ChatColor.YELLOW + "Sentry");
        }

        public void destroy() {
            this.hologram.delete();
            this.armorStand.remove();
        }

        public ArmorStand getArmorStand() {
            return armorStand;
        }

        public Location getLocation() {
            return location;
        }

        public void summonCrossbow() {
            if (armorStand == null) return;
            if (armorStand.getEquipment() == null) return;
            armorStand.setCustomNameVisible(false);
            armorStand.setArms(true);
            armorStand.setRightArmPose(new EulerAngle(ArmorStandAPI.degreesToRadians(285), 0, 0));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
        }
    }
}

