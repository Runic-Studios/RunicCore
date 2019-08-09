package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    private static HashMap<UUID, Long> ragers = new HashMap<>();

    // constructor
    public Enrage() {
        super("Enrage",
                "For " + CHANNEL_DURATION + " seconds, you channel a powerful rage," +
                        "\nslowing your movement speed. After, you gain" +
                        "\nan immense boost of speed and your melee" +
                        "\nweapon attacks deal " + DAMAGE_AMT + " additional spellʔ damage" +
                        "\nfor " + BUFF_DURATION + " seconds!",
                ChatColor.WHITE, 10, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        UUID uuid = pl.getUniqueId();

        // apply preliminary particle effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION * 20, 2));
        pl.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION * 20, 2));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.REDSTONE, pl.getLocation(), Color.RED);

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

    public void createHelix(Player player) {
        Location loc = player.getLocation();
        double radius = 0.5;
        for (double y = 0; y <= 3; y += 0.05) {
            double x = radius * Math.cos(y);
            double z = radius * Math.sin(y);
            player.getWorld().spawnParticle(Particle.FLAME, (float) (loc.getX() + x), (float) (loc.getY() + y), (float) (loc.getZ() + z), 1, 0, 0, 0, 0);
        }
    }

            /**
             * Activate on-hit effects
             */
    @EventHandler
    public void onSuccessfulHit(WeaponDamageEvent e) {

        if (!ragers.containsKey(e.getPlayer().getUniqueId())) return;

        if(e.isCancelled()) return;

        Player pl = e.getPlayer();
        Entity en = e.getEntity();

        if (!(en instanceof LivingEntity)) return;

        LivingEntity le = (LivingEntity) en;

        DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, false);
        le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
        le.getWorld().playSound(le.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);
    }
}

