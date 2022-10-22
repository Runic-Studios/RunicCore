package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.*;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StatListener implements Listener {

    private static final float DEFAULT_SPEED = 0.2f;

    @EventHandler
    public void onHealthRegen(HealthRegenEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double healthRegenBonusPercent = Stat.getHealthRegenMult() * RunicCoreAPI.getPlayerVitality(uuid);
        int bonusHealth = (int) (e.getAmount() * healthRegenBonusPercent);
        e.setAmount(e.getAmount() + bonusHealth);
    }

    @EventHandler
    public void onManaRegen(ManaRegenEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double manaRegenBonusPercent = Stat.getManaRegenMult() * RunicCoreAPI.getPlayerIntelligence(uuid);
        int bonusMana = (int) (e.getAmount() * manaRegenBonusPercent);
        e.setAmount(e.getAmount() + bonusMana);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        UUID uuid = e.getVictim().getUniqueId();
        double chanceToDodge = ThreadLocalRandom.current().nextDouble();
        if (chanceToDodge <= (Stat.getDodgeChance() * RunicCoreAPI.getPlayerDexterity(uuid))) {
            e.setCancelled(true);
            ((Player) e.getVictim()).playSound(e.getVictim().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
            e.getVictim().getWorld().spawnParticle(Particle.CLOUD, e.getVictim().getLocation(), 15, 0.25f, 0.25f, 0.25f, 0);
            return;
        }
        double damageMitigationPercent = Stat.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuid);
        if (damageMitigationPercent > (Stat.getDamageReductionCap() / 100))
            damageMitigationPercent = Stat.getDamageReductionCap() / 100; // cap it
        e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
    }

    @EventHandler
    public void onStatChangeEvent(StatChangeEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double walkBonusPercent = Stat.getMovementSpeedMult() * RunicCoreAPI.getPlayerDexterity(uuid);
        float newSpeed = (float) (DEFAULT_SPEED + (DEFAULT_SPEED * walkBonusPercent));
        if (newSpeed > (Stat.getMovementSpeedCap() / 100))
            newSpeed = (float) (Stat.getMovementSpeedCap() / 100);
        e.getPlayer().setWalkSpeed(newSpeed);
        RunicCoreAPI.calculateMaxMana(e.getPlayer());
    }

    @EventHandler
    public void onSpellHealing(SpellHealEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double healAmountBonusPercent = Stat.getSpellHealingMult() * RunicCoreAPI.getPlayerWisdom(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * healAmountBonusPercent)));
        double chanceToCrit = ThreadLocalRandom.current().nextDouble();
        if (chanceToCrit <= (Stat.getCriticalChance() * RunicCoreAPI.getPlayerStrength(uuid))) {
            e.setCritical(true);
            e.setAmount((int) (e.getAmount() * Stat.getCriticalDamageMultiplier()));
        }
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double magicDamageBonusPercent = Stat.getMagicDmgMult() * RunicCoreAPI.getPlayerIntelligence(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * magicDamageBonusPercent)));
        double chanceToCrit = ThreadLocalRandom.current().nextDouble();
        if (chanceToCrit <= (Stat.getCriticalChance() * RunicCoreAPI.getPlayerStrength(uuid))) {
            e.setCritical(true);
            e.setAmount((int) (e.getAmount() * Stat.getCriticalDamageMultiplier()));
        }
        /*
        Defense calculated last
         */
        if (e.getVictim() instanceof Player) {
            UUID uuidVictim = e.getVictim().getUniqueId();
            double chanceToDodge = ThreadLocalRandom.current().nextDouble();
            if (chanceToDodge <= (Stat.getDodgeChance() * RunicCoreAPI.getPlayerDexterity(uuidVictim))) {
                e.setCancelled(true);
                ((Player) e.getVictim()).playSound(e.getVictim().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
                e.getVictim().getWorld().spawnParticle(Particle.CLOUD, e.getVictim().getLocation(), 15, 0.25f, 0.25f, 0.25f, 0);
                return;
            }
            double damageMitigationPercent = Stat.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuidVictim);
            if (damageMitigationPercent > (Stat.getDamageReductionCap() / 100))
                damageMitigationPercent = (Stat.getDamageReductionCap() / 100); // cap it
            e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
        }
    }

    @EventHandler
    public void onRangedDamage(PhysicalDamageEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (e.isRanged()) {
            double rangedDamageBonusPercent = Stat.getRangedDmgMult() * RunicCoreAPI.getPlayerDexterity(uuid);
            e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * rangedDamageBonusPercent)));
        } else {
            double meleeDamageBonusPercent = Stat.getMeleeDmgMult() * RunicCoreAPI.getPlayerStrength(uuid);
            e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * meleeDamageBonusPercent)));
        }
        double chanceToCrit = ThreadLocalRandom.current().nextDouble();
        if (chanceToCrit <= (Stat.getCriticalChance() * RunicCoreAPI.getPlayerStrength(uuid))) {
            e.setCritical(true);
            e.setAmount((int) (e.getAmount() * Stat.getCriticalDamageMultiplier()));
        }
        /*
        Defense calculated last
         */
        if (e.getVictim() instanceof Player) {
            UUID uuidVictim = e.getVictim().getUniqueId();
            double chanceToDodge = ThreadLocalRandom.current().nextDouble();
            if (chanceToDodge <= (Stat.getDodgeChance() * RunicCoreAPI.getPlayerDexterity(uuidVictim))) {
                e.setCancelled(true);
                ((Player) e.getVictim()).playSound(e.getVictim().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.0f);
                return;
            }
            double damageMitigationPercent = Stat.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuidVictim);
            if (damageMitigationPercent > (Stat.getDamageReductionCap() / 100))
                damageMitigationPercent = (Stat.getDamageReductionCap() / 100); // cap it
            e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
        }
    }
}
