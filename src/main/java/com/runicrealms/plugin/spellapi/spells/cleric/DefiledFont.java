package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DefiledFont extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final double PERIOD = 2.0; // seconds
    private static final double POTION_SPEED_MULT = 1.25;
    private static final Set<ThrownPotion> thrownPotionSet = new HashSet<>();
    private static final Set<UUID> defiledPlayers = new HashSet<>();
    private double damage;
    private double damagePerLevel;
    private double healingReduction;
    private double duration;
    private double radius;

    public DefiledFont() {
        super("Defiled Font", CharacterClass.CLERIC);
    }

    public static Set<ThrownPotion> getThrownPotionSet() {
        return thrownPotionSet;
    }

    private void applyDefiledFont(Player player, LivingEntity livingEntity) {
        defiledPlayers.add(livingEntity.getUniqueId());
        livingEntity.getWorld().playSound(livingEntity.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 0.5F, 1.0F);
        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    DamageUtil.damageEntitySpell(damage / PERIOD, livingEntity, player, spell);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        assert meta != null;
        meta.setColor(Color.LIME);
        item.setItemMeta(meta);
        ThrownPotion thrownPotion = player.launchProjectile(ThrownPotion.class);
        thrownPotionSet.add(thrownPotion);
        thrownPotion.setItem(item);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(POTION_SPEED_MULT);
        thrownPotion.setVelocity(velocity);
        thrownPotion.setShooter(player);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getHealingReduction() {
        return healingReduction;
    }

    public void setHealingReduction(double healingReduction) {
        this.healingReduction = healingReduction;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number magicDamagePerLevel = (Number) spellData.getOrDefault("magic-damage-per-level", 0);
        setMagicDamagePerLevel(magicDamagePerLevel.doubleValue());
        Number healingReduction = (Number) spellData.getOrDefault("healing-reduction", 0);
        setHealingReduction(healingReduction.doubleValue());
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionBreak(PotionSplashEvent event) {

        // only listen for our potion
        if (!thrownPotionSet.contains(event.getPotion())) return;
        if (!(event.getPotion().getShooter() instanceof Player player)) return;

        thrownPotionSet.remove(event.getPotion());
        event.setCancelled(true);

        ThrownPotion expiredBomb = event.getPotion();
        Location location = expiredBomb.getLocation();

        expiredBomb.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.5F, 1.0F);

        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
            applyDefiledFont(player, (LivingEntity) entity);
        }
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (!defiledPlayers.contains(event.getEntity().getUniqueId())) return;
        event.setAmount((int) (event.getAmount() * healingReduction));
    }
}

