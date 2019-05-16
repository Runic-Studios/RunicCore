package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.formats.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.util.HashMap;
import java.util.UUID;

public class Enrage extends Spell {

    // instance variables
    private static final int CHANNEL_DURATION = 4;
    private static final int BUFF_DURATION = 10;
    private static final int DAMAGE_AMT = 3;
    private HashMap<UUID, Long> ragers = new HashMap<>();

    // constructor
    public Enrage() {
        super("Enrage",
                "For " + CHANNEL_DURATION + " seconds, you channel a powerful" +
                        "\nrage and may not move. After, you gain" +
                        "\nan immense boost of speed and your attacks" +
                        "\ndeal" + DAMAGE_AMT + "additional spell damage" + " for " + BUFF_DURATION + " seconds!",
                ChatColor.WHITE, 1, 5);
    }

    // skill execute code
    @Override
    public void executeSkill(Player pl, SpellItemType type) {

        UUID uuid = pl.getUniqueId();

        // apply preliminary particle effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION * 20, 99));
        pl.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, CHANNEL_DURATION * 20, 128));
        pl.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION * 40, 2));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        Cone.coneEffect(pl, Particle.REDSTONE, CHANNEL_DURATION - 1, 0, 3);
        pl.sendMessage(ChatColor.GRAY + "You begin to feel a surge of power!");

        // after the player has channeled the spell
        new BukkitRunnable() {
            @Override
            public void run() {
                pl.sendMessage(ChatColor.GREEN + "You become enraged!");

                // particles, sounds
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
                pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                        25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 20));

                // potion effect, dmg effect
                pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, 1));
                ragers.put(uuid, System.currentTimeMillis());
                pl.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, BUFF_DURATION * 20, 1));

                // remove damage buff
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ragers.remove(uuid);
                        pl.sendMessage(ChatColor.GRAY + "You no longer feel enraged.");
                    }
                }.runTaskLater(plugin, BUFF_DURATION * 20);

            }
        }.runTaskLater(RunicCore.getInstance(), CHANNEL_DURATION * 20);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        Player player = (Player) e.getDamager();
        UUID uuid = player.getUniqueId();
        LivingEntity le = (LivingEntity) e.getEntity();

        if (!ragers.containsKey(uuid)) return;

        // prevent infinite damage loop
        e.setCancelled(true);
        DamageUtil.damageEntityMagic(DAMAGE_AMT, le, player);

        le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);

        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);
    }
}

