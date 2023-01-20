package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
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

public class HolyWater extends Spell implements HealingSpell, MagicDamageSpell {

    private static final int DAMAGE = 12;
    private static final int HEAL_AMT = 20;
    private static final double DAMAGE_PER_LEVEL = 1.25;
    private static final int RADIUS = 5;
    private static final double HEALING_PER_LEVEL = 1.25;
    private static final double POTION_SPEED_MULT = 1.25;
    private static Set<ThrownPotion> thrownPotionSet;

    public HolyWater() {
        super("Holy Water",
                "You throw a magical vial of light! " +
                        "Allies within " + RADIUS + " blocks of the light " +
                        "are healed✦ for (" + HEAL_AMT + " + &f" +
                        HEALING_PER_LEVEL + "x&7 lvl) health! " +
                        "Against enemies, the vial deals (" + DAMAGE + " + &f" +
                        DAMAGE_PER_LEVEL + "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.CLERIC, 10, 15);
        thrownPotionSet = new HashSet<>();
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
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @Override
    public int getHeal() {
        return HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionBreak(PotionSplashEvent event) {

        // only listen for our potion
        if (!thrownPotionSet.contains(event.getPotion())) return;
        if (!(event.getPotion().getShooter() instanceof Player)) return;

        thrownPotionSet.remove(event.getPotion());
        event.setCancelled(true);

        ThrownPotion expiredBomb = event.getPotion();
        Location loc = expiredBomb.getLocation();
        Player player = (Player) event.getPotion().getShooter();
        if (player == null) return;

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.5F, 1.0F);

        expiredBomb.getWorld().spawnParticle(Particle.REDSTONE, loc,
                50, 1f, 1f, 1f, new Particle.DustOptions(Color.WHITE, 10));

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (isValidAlly(player, en))
                HealUtil.healPlayer(HEAL_AMT, (Player) en, player, false, this);
            if (isValidEnemy(player, en))
                DamageUtil.damageEntitySpell(DAMAGE, ((LivingEntity) en), player, this);
        }
    }
}

