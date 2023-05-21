package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
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
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SacredSpring extends Spell implements HealingSpell, MagicDamageSpell, RadiusSpell {
    private static final double POTION_SPEED_MULT = 1.25;
    private static Set<ThrownPotion> thrownPotionSet;
    private double damage;
    private double healAmt;
    private double damagePerLevel;
    private double radius;
    private double healingPerLevel;

    public SacredSpring() {
        super("Sacred Spring", CharacterClass.CLERIC);
        thrownPotionSet = new HashSet<>();
        this.setDescription("You throw a magical vial of water! " +
                "Allies within " + radius + " blocks of the spring " +
                "are healed✦ for (" + healAmt + " + &f" +
                healingPerLevel + "x&7 lvl) health! " +
                "Against enemies, the vial deals (" + damage + " + &f" +
                damagePerLevel + "x&7 lvl) magicʔ damage!");
    }

    public static Set<ThrownPotion> getThrownPotionSet() {
        return thrownPotionSet;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        Objects.requireNonNull(meta).setColor(Color.AQUA);
        item.setItemMeta(meta);
        ThrownPotion thrownPotion = player.launchProjectile(ThrownPotion.class);
        thrownPotionSet.add(thrownPotion);
        thrownPotion.setItem(item);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(POTION_SPEED_MULT);
        thrownPotion.setVelocity(velocity);
        thrownPotion.setShooter(player);

    }

    @Override
    public double getHeal() {
        return healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
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
        Location loc = expiredBomb.getLocation();

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.5F, 1.0F);

        for (Entity entity : player.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (isValidAlly(player, entity))
                healPlayer(player, (Player) entity, healAmt, this);
            if (isValidEnemy(player, entity))
                DamageUtil.damageEntitySpell(damage, ((LivingEntity) entity), player, this);
        }
    }
}

