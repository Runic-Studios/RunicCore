package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UnholyWater extends Spell implements MagicDamageSpell {
    public static final int DAMAGE = 30;
    public static final int DURATION = 4;
    public static final double HEALING_REDUCTION = 0.5D;
    public static final double DAMAGE_PER_LEVEL = 1.0D;
    private static final int RADIUS = 5;
    private static final double PERIOD = 2.0; // seconds
    private static final double POTION_SPEED_MULT = 1.25;
    private static final Set<ThrownPotion> thrownPotionSet = new HashSet<>();
    private static final Set<UUID> unholyPlayers = new HashSet<>();

    public UnholyWater() {
        super("Unholy Water", "", ChatColor.WHITE, CharacterClass.CLERIC, 10, 15);
    }

    public static Set<ThrownPotion> getThrownPotionSet() {
        return thrownPotionSet;
    }

    private void applyUnholyWater(Player player, LivingEntity livingEntity) {
        unholyPlayers.add(livingEntity.getUniqueId());
        livingEntity.getWorld().playSound(livingEntity.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 0.5F, 1.0F);
        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    DamageUtil.damageEntitySpell(DAMAGE / PERIOD, livingEntity, player, spell);
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
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionBreak(PotionSplashEvent event) {

        // only listen for our potion
        if (!thrownPotionSet.contains(event.getPotion())) return;
        if (!(event.getPotion().getShooter() instanceof Player)) return;

        thrownPotionSet.remove(event.getPotion());
        event.setCancelled(true);

        ThrownPotion expiredBomb = event.getPotion();
        Location location = expiredBomb.getLocation();
        Player player = (Player) event.getPotion().getShooter();
        if (player == null) return;

        expiredBomb.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.5F, 1.0F);

        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            applyUnholyWater(player, (LivingEntity) entity);
        }
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (!unholyPlayers.contains(event.getEntity().getUniqueId())) return;
        event.setAmount((int) (event.getAmount() * HEALING_REDUCTION));
    }
}

