package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class Enrage extends Spell {

    private static final int CHANNEL_DURATION = 3;
    private static final int BUFF_DURATION = 10;
    private static final int DAMAGE_AMT = 5;
    private static HashMap<UUID, Long> ragers = new HashMap<>();

    public Enrage() {
        super("Enrage",
                "For " + CHANNEL_DURATION + " seconds, you channel a deep" +
                        "\nrage, slowing your speed. After," +
                        "\nyou gain an immense boost of speed" +
                        "\nand your weapon⚔ attacks deal " + DAMAGE_AMT + " extra" +
                        "\nspellʔ damage for " + BUFF_DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 10, 25);
    }

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

    /*
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

