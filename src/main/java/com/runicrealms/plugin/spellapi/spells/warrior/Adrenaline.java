package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

public class Adrenaline extends Spell implements PhysicalDamageSpell {
    private static final int DURATION = 5;
    private static final int MAX_STACKS = 10;
    private static final double DAMAGE_PER_LEVEL = 0.25;
    private final HashSet<UUID> adrenalineSet = new HashSet<>();

    public Adrenaline() {
        super("Adrenaline",
                "For the next " + DURATION + "s, each time you deal damage, " +
                        "gain a stack of Adrenaline, granting additional physical damage equal to" +
                        " (.1* STR). Each hit refreshes the duration of your stacks. " +
                        "Adrenaline can stack up to 10 times. " +
                        "At " + MAX_STACKS + " stacks, extend the duration of this effect by 5s and gain Speed II " +
                        "for the remainder of the effect!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 20, 30);
    }

    private void enrage(Player player) {

        // particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 20));

        // potion effect, damage effect
//        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, BUFF_DURATION * 20, 1));
//        adrenalineSet.add(player.getUniqueId());

        // remove damage buff
//        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
//            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.0f);
//            adrenalineSet.remove(player.getUniqueId());
//        }, BUFF_DURATION * 20L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Apply preliminary particle effects
//        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, CHANNEL_DURATION * 20, 2));
//        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CHANNEL_DURATION * 20, 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(player, Particle.REDSTONE, player.getLocation(), Color.RED);
        // After the player has channeled the spell
//        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> enrage(player), CHANNEL_DURATION * 20L);
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
        if (!adrenalineSet.contains(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        LivingEntity livingEntity = event.getVictim();
        livingEntity.getWorld().spawnParticle(Particle.CRIT_MAGIC, livingEntity.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_WITCH_HURT, 0.5f, 0.8f);
//        DamageUtil.damageEntityPhysical(DAMAGE_AMT, livingEntity, player, false, false, this);
    }
}

