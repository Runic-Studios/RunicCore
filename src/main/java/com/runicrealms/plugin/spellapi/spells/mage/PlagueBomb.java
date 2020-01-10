package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class PlagueBomb extends Spell {

    // global variables
    private static final int DAMAGE_AMT = 2;
    private static final int DURATION = 6;
    private static final int PERIOD = 2;
    private static final int RADIUS = 5;
    private ThrownPotion thrownPotion;

    // constructor
    public PlagueBomb() {
        super("Plague Bomb",
                "You launch a magical vial of disease," +
                        "\ndealing " + (DAMAGE_AMT*DURATION/PERIOD) + " spellʔ damage over " +
                        "\n" + DURATION + " seconds to enemies within" +
                        "\n" + RADIUS + " blocks of the cloud." +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE,10, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        Objects.requireNonNull(meta).setColor(Color.GREEN);
        item.setItemMeta(meta);
        thrownPotion = pl.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(item);
        final Vector velocity = pl.getLocation().getDirection().normalize().multiply(1.25);
        thrownPotion.setVelocity(velocity);
        thrownPotion.setShooter(pl);
    }

    @EventHandler
    public void onPotionBreak(PotionSplashEvent e) {

        // only listen for our fireball
        if (!(e.getPotion().equals(this.thrownPotion))) return;
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        e.setCancelled(true);

        ThrownPotion expiredBomb = e.getPotion();
        Location loc = expiredBomb.getLocation();
        Player pl = (Player) e.getPotion().getShooter();

        expiredBomb.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_CAT_HISS, 0.5F, 0.5F);
        expiredBomb.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1.0F);

        expiredBomb.getWorld().spawnParticle(Particle.REDSTONE, loc,
                50, 1f, 1f, 1f, new Particle.DustOptions(Color.GREEN, 10));

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (!(en instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity) en;
            // ignore the caster
            if (en.equals(pl)) continue;
            // ignore NPCs, armor stands
            if (en.hasMetadata("NPC")) continue;
            if (en instanceof ArmorStand) continue;
            // skip party members
            if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                    && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(en.getUniqueId())) continue;
            // outlaw check
            if (en instanceof Player && (!OutlawManager.isOutlaw(((Player) en)) || !OutlawManager.isOutlaw(Objects.requireNonNull(pl)))) continue;
            damageOverTime(le, pl);
        }
    }

    private void damageOverTime(LivingEntity le, Player pl) {
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= DURATION) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    le.getWorld().playSound(le.getLocation(), Sound.ENTITY_CAT_HISS, 0.5F, 0.5F);
                    le.getWorld().spawnParticle(Particle.REDSTONE, le.getLocation(),
                            50, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.GREEN, 1));
                    DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, true);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD*20L);
    }
}

