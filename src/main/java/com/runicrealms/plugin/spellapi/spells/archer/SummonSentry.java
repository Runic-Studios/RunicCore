package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.api.ArmorStandAPI;
import com.runicrealms.plugin.classes.ClassEnum;
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
public class SummonSentry extends Spell {

    private static final int DAMAGE_AMOUNT = 25;
    private static final int DURATION = 8;
    private static final int POTION_DURATION = 1;
    private static final int RADIUS = 10;
    private Arrow arrow;

    // todo: fire an arrow to summon sentry. reduce damage, but increase arrow knockback
    // todo: add scaling
    public SummonSentry() {
        super("Summon Sentry",
                "You conjure an enchanted " +
                        "crossbow at your location! " +
                        "For " + DURATION + "s, the crossbow fires " +
                        "at all enemies within " + RADIUS + " blocks, " +
                        "dealing " + DAMAGE_AMOUNT + " weapon⚔ damage " +
                        "and blinding them for " + POTION_DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 16, 75);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        ArmorStand armorStand = ArmorStandAPI.spawnArmorStand(pl.getLocation());
        if (armorStand == null) return;
        if (armorStand.getEquipment() == null) return;
        armorStand.setCustomName(ChatColor.YELLOW + pl.getName() + "'s Turret");
        armorStand.setArms(true);
        armorStand.setRightArmPose(new EulerAngle(ArmorStandAPI.degreesToRadians(285), 0, 0));
        armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
        pl.getWorld().playSound(armorStand.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, armorStand.getLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 5));
        String name = pl.getName();
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
                    pl.getWorld().spawnParticle
                            (
                                    Particle.CRIT,
                                    standLocation.clone().add(0, 1, 0),
                                    25, 0.75f, 0.25f, 0.75f, 0
                            );
                    for (Entity en : armorStand.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (!verifyEnemy(pl, en)) continue;
                        Location newLocation = standLocation.clone().setDirection(en.getLocation().subtract(standLocation).toVector());
                        armorStand.teleport(newLocation);
                        final Vector direction =
                                ((LivingEntity) en).getEyeLocation().toVector()
                                .subtract(standLocation.clone().add(0, 1, 0).toVector());
                        arrow = pl.getWorld().spawnArrow
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
                                        new PotionEffect(PotionEffectType.BLINDNESS, POTION_DURATION * 20, 1)
                                );
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    /*
    Deal correct damage when a turret arrow is fired
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustomArrowHit(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata("player")) return;
        e.setCancelled(true);
        Player pl = Bukkit.getPlayer(e.getDamager().getMetadata("player").get(0).asString());
        DamageUtil.damageEntityWeapon(DAMAGE_AMOUNT, (LivingEntity) e.getEntity(), pl, false, true, true);
    }
}

