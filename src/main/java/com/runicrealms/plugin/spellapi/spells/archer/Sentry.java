package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.api.ArmorStandAPI;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Sentry extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 3;
    private static final int DURATION = 8;
    private static final int POTION_DURATION = 3;
    private static final int RADIUS = 10;
    private static final double DAMAGE_PER_LEVEL = 0.15;
    private Arrow arrow;

    public Sentry() {
        super("Sentry",
                "You build an enchanted " +
                        "crossbow at your location! " +
                        "For " + DURATION + "s, the crossbow fires " +
                        "at all enemies within " + RADIUS + " blocks, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) physical⚔ damage and slowing them for " +
                        POTION_DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ARCHER, 30, 75);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        ArmorStand armorStand = ArmorStandAPI.spawnArmorStand(player.getLocation());
        if (armorStand == null) return;
        if (armorStand.getEquipment() == null) return;
        armorStand.setCustomName(ChatColor.YELLOW + player.getName() + "'s Sentry");
        armorStand.setArms(true);
        armorStand.setRightArmPose(new EulerAngle(ArmorStandAPI.degreesToRadians(285), 0, 0));
        armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
        player.getWorld().playSound(armorStand.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
        player.getWorld().spawnParticle(Particle.REDSTONE, armorStand.getLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 5));
        String name = player.getName();
        Location standLocation = armorStand.getEyeLocation();

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    armorStand.remove();
                } else {
                    count += 1;
                    player.getWorld().spawnParticle
                            (
                                    Particle.CRIT,
                                    standLocation.clone().add(0, 1, 0),
                                    25, 0.75f, 0.25f, 0.75f, 0
                            );
                    for (Entity en : armorStand.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (!isValidEnemy(player, en)) continue;
                        Location newLocation = standLocation.clone().setDirection(en.getLocation().subtract(standLocation).toVector());
                        armorStand.teleport(newLocation);
                        final Vector direction =
                                ((LivingEntity) en).getEyeLocation().toVector()
                                        .subtract(standLocation.clone().add(0, 1, 0).toVector());
                        arrow = player.getWorld().spawnArrow
                                (
                                        standLocation.clone().add(0, 1, 0), direction, (float) 2, (float) 0
                                );
                        // DON'T set the shooter here so the DamageListener class won't take over, use meta instead
                        arrow.setMetadata("player", new FixedMetadataValue(plugin, name));
                        arrow.setCustomNameVisible(false);
                        arrow.setCustomName("autoAttack");
                        arrow.setBounce(false);
                        EntityTrail.entityTrail(arrow, Particle.SPELL_WITCH);
                        ((LivingEntity) en).addPotionEffect
                                (
                                        new PotionEffect(PotionEffectType.SLOW, POTION_DURATION * 20, 2)
                                );
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    /*
    Deal correct damage when a turret arrow is fired
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustomArrowHit(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata("player")) return;
        e.setCancelled(true);
        Player pl = Bukkit.getPlayer(e.getDamager().getMetadata("player").get(0).asString());
        DamageUtil.damageEntityPhysical(DAMAGE, (LivingEntity) e.getEntity(), pl, false, true, this);
    }
}

