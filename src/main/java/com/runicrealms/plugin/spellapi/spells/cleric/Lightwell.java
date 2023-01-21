package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Lightwell extends Spell implements HealingSpell {
    private static final int BLIND_DURATION = 2;
    private static final int DURATION = 3; // seconds
    private static final int HEAL_AMT = 20;
    private static final int RADIUS = 2;
    private static final double HEALING_PER_LEVEL = 1.25;

    public Lightwell() {
        super("Lightwell",
                "Your Holy Water now leaves behind a pool of light for " + DURATION + "s, " +
                        "healing✦ all other allies for (" + HEAL_AMT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) per second while they stand inside it. Enemy players who stand inside " +
                        "the pool are blinded for " + BLIND_DURATION + "s!",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public int getHeal() {
        return HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPotionBreak(PotionSplashEvent event) {
        if (event.isCancelled()) return;
        if (!HolyWater.getThrownPotionSet().contains(event.getPotion())) return;
        if (!(event.getPotion().getShooter() instanceof Player)) return;
        Player player = (Player) event.getPotion().getShooter();
        if (!hasPassive(player.getUniqueId(), this.getName())) return;

        Location location = event.getPotion().getLocation();

        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > DURATION)
                    this.cancel();

                Circle.createParticleCircle(player, location, RADIUS, Particle.SPELL_INSTANT, Color.WHITE);
                player.getWorld().playSound(location, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 0.5f);
                player.getWorld().playSound(location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 25, 0.75f, 0.75f, 0.75f, 0);

                for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                    if (isValidAlly(player, entity)) {
                        if (entity.equals(player)) continue; // does not heal self
                        HealUtil.healPlayer(HEAL_AMT, (Player) entity, player, false, spell);
                    } else if (isValidEnemy(player, entity)) {
                        entity.getWorld().spawnParticle(Particle.REDSTONE, ((LivingEntity) entity).getEyeLocation(), 5, 0.5f, 0.5f, 0.5f,
                                new Particle.DustOptions(Color.BLACK, 1));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BLIND_DURATION * 20, 2));
                    }
                }


            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);

    }
}

