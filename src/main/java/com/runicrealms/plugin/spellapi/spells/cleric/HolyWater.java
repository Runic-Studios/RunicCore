package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
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
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class HolyWater extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 15;
    private static final double DAMAGE_PER_LEVEL = 1.75;
    private static final int DURATION = 6;
    private static final int RADIUS = 5;
    private static final double PERCENT = 35;
    private static final double POTION_SPEED_MULT = 1.25;
    private ThrownPotion thrownPotion;
    private final HashMap<UUID, HashSet<UUID>> affectedPlayers;

    public HolyWater() {
        super("Holy Water",
                "You throw a magical vial of light! " +
                        "Allies within " + RADIUS + " blocks of the light " +
                        "receive an additional " + (int) PERCENT + "% health " +
                        "from all healing✦ spells for " + DURATION + "s! " +
                        "Against enemies, the vial deals (" + DAMAGE + " + &f" +
                        DAMAGE_PER_LEVEL + "x&7 lvl) spellʔ damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 10, 20);
        affectedPlayers = new HashMap<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        Objects.requireNonNull(meta).setColor(Color.AQUA);
        item.setItemMeta(meta);
        thrownPotion = pl.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(item);
        final Vector velocity = pl.getLocation().getDirection().normalize().multiply(POTION_SPEED_MULT);
        thrownPotion.setVelocity(velocity);
        thrownPotion.setShooter(pl);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (affectedPlayers.get(pl.getUniqueId()) != null) affectedPlayers.remove(pl.getUniqueId());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onPotionBreak(PotionSplashEvent e) {

        // only listen for our potion
        if (!(e.getPotion().equals(this.thrownPotion))) return;
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        e.setCancelled(true);

        ThrownPotion expiredBomb = e.getPotion();
        Location loc = expiredBomb.getLocation();
        Player pl = (Player) e.getPotion().getShooter();
        if (pl == null) return;

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.5F, 1.0F);

        expiredBomb.getWorld().spawnParticle(Particle.REDSTONE, loc,
                50, 1f, 1f, 1f, new Particle.DustOptions(Color.WHITE, 10));

        HashSet<UUID> allies = new HashSet<>();
        affectedPlayers.put(pl.getUniqueId(), allies);
        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (verifyAlly(pl, en))
                affectedPlayers.get(pl.getUniqueId()).add(en.getUniqueId());
            if (verifyEnemy(pl, en))
                DamageUtil.damageEntitySpell(DAMAGE, ((LivingEntity) en), pl, this);
        }
    }

    /**
     * Players receive extra healing from caster
     */
    @EventHandler
    public void onHeal(SpellHealEvent e) {
        Player caster = e.getPlayer();
        if (!affectedPlayers.containsKey(caster.getUniqueId())) return;
        if (!affectedPlayers.get(caster.getUniqueId()).contains(e.getEntity().getUniqueId())) return;
        double percent = PERCENT / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        if (extraAmt < 1) extraAmt = 1;
        e.setAmount(e.getAmount() + extraAmt);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

