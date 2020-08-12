package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ArcaneShot extends Spell {

    private static final int DAMAGE = 15;
    private static final int DURATION = 5;
    private static final double PERCENT = 50;
    private Arrow arcaneArrow;
    private final Set<UUID> silencedEntities;

    public ArcaneShot() {
        super("Arcane Shot",
                "You fire a magic arrow which" +
                        "\ndeals " + DAMAGE + " spellʔ damage to its" +
                        "\ntarget and silences it for " + DURATION +
                        "\nseconds, preventing it from" +
                        "\ndealing damage!",
                ChatColor.WHITE, ClassEnum.ARCHER, 12, 25);
        silencedEntities = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 2f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2f);

        arcaneArrow = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        arcaneArrow.setVelocity(vec);
        arcaneArrow.setShooter(pl);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = arcaneArrow.getLocation();
                pl.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc,
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.FUCHSIA, 2));
                if (arcaneArrow.isDead() || arcaneArrow.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    @EventHandler
    public void onPoisArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!e.getDamager().equals(this.arcaneArrow)) return;
        e.setCancelled(true);
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        LivingEntity le = (LivingEntity) e.getEntity();
        if (!verifyEnemy(pl, le)) return;

        // spell effect
        silencedEntities.add(le.getUniqueId());
        DamageUtil.damageEntitySpell(DAMAGE, le, pl, 100);

        // particles, sounds
        le.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "silenced!");

        le.getWorld().playSound(le.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);
        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.5f, 2.0f);

        new BukkitRunnable() {
            @Override
            public void run() {
                silencedEntities.remove(le.getUniqueId());
                le.sendMessage(ChatColor.GREEN + "You are no longer silenced!");
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), DURATION*20L);
    }

    @EventHandler
    public void onS(SpellHealEvent e) {
        if (silencedEntities.contains(e.getEntity().getUniqueId())) {
            double percent = PERCENT/100;
            int newAmt = (int) (e.getAmount()*percent);
            e.setAmount(newAmt);
        }
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (silencedEntities.contains(e.getDamager().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (silencedEntities.contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (silencedEntities.contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }
}
