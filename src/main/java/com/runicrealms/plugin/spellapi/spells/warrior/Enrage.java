package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.UUID;

public class Enrage extends Spell implements PhysicalDamageSpell {

    private static final int BUFF_DURATION = 8;
    private static final int CHANNEL_DURATION = 2;
    private static final int DAMAGE_AMT = 2;
    private static final double DAMAGE_PER_LEVEL = 0.25;
    private static final HashSet<UUID> ragers = new HashSet<>();

    public Enrage() {
        super("Enrage",
                "For " + CHANNEL_DURATION + "s, you channel a deep " +
                        "rage, slowing your speed. After, " +
                        "you gain an immense boost of speed " +
                        "and your basic attacks deal (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) extra physical⚔ damage for " + BUFF_DURATION + "s!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 12, 25);
    }

    public static HashSet<UUID> getRagers() {
        return ragers;
    }

    private void enrage(Player player) {

        // particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 20));

        // potion effect, damage effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, 1));
        ragers.add(player.getUniqueId());

        // remove damage buff
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.0f);
            ragers.remove(player.getUniqueId());
        }, BUFF_DURATION * 20L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Apply preliminary particle effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION * 20, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION * 20, 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(Particle.REDSTONE, player.getLocation(), Color.RED);
        // After the player has channeled the spell
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> enrage(player), CHANNEL_DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    /*
     * Activate on-hit effects
     */
    @EventHandler
    public void onSuccessfulHit(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!ragers.contains(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        LivingEntity livingEntity = event.getVictim();
        livingEntity.getWorld().spawnParticle(Particle.CRIT_MAGIC, livingEntity.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);
        DamageUtil.damageEntityPhysical(DAMAGE_AMT, livingEntity, player, false, false, this);
    }
}

