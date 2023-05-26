package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.SpellShieldEvent;
import com.runicrealms.plugin.events.*;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public class StatListener implements Listener {

    @EventHandler
    public void onHealthRegen(HealthRegenEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        double healthRegenBonusPercent = Stat.getHealthRegenMult() * RunicCore.getStatAPI().getPlayerVitality(uuid);
        int bonusHealth = (int) (event.getAmount() * healthRegenBonusPercent);
        event.setAmount(event.getAmount() + bonusHealth);
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Calculate intelligence bonus first
        double magicDamageBonusPercent = Stat.getMagicDmgMult() * RunicCore.getStatAPI().getPlayerIntelligence(uuid);
        event.setAmount((int) (event.getAmount() + Math.ceil(event.getAmount() * magicDamageBonusPercent)));
        // Defense calculated last
        if (event.getVictim() instanceof Player) {
            UUID uuidVictim = event.getVictim().getUniqueId();
            double damageMitigationPercent = Stat.getDamageReductionMult() * RunicCore.getStatAPI().getPlayerVitality(uuidVictim);
            if (damageMitigationPercent > (Stat.getDamageReductionCap() / 100))
                damageMitigationPercent = (Stat.getDamageReductionCap() / 100); // cap it
            int damage = (int) (event.getAmount() - Math.ceil(event.getAmount() * damageMitigationPercent));
            if (damage < 1)
                damage = 1; // Damage can't drop below 0
            event.setAmount(damage);
        }
    }

    @EventHandler
    public void onManaRegen(ManaRegenEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        double manaRegenBonusPercent = Stat.getManaRegenMult() * RunicCore.getStatAPI().getPlayerIntelligence(uuid);
        int bonusMana = (int) (event.getAmount() * manaRegenBonusPercent);
        event.setAmount(event.getAmount() + bonusMana);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player)) return;
        UUID uuid = event.getVictim().getUniqueId();
        double damageMitigationPercent = Stat.getDamageReductionMult() * RunicCore.getStatAPI().getPlayerVitality(uuid);
        if (damageMitigationPercent > (Stat.getDamageReductionCap() / 100))
            damageMitigationPercent = Stat.getDamageReductionCap() / 100; // cap it
        event.setAmount((int) (event.getAmount() - Math.ceil(event.getAmount() * damageMitigationPercent)));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Calculate strength bonus first
        double physicalDamageBonusPercent = Stat.getPhysicalDmgMult() * RunicCore.getStatAPI().getPlayerStrength(uuid);
        event.setAmount((int) (event.getAmount() + Math.ceil(event.getAmount() * physicalDamageBonusPercent)));
        // Defense calculated last
        if (event.getVictim() instanceof Player) {
            UUID uuidVictim = event.getVictim().getUniqueId();
            double damageMitigationPercent = Stat.getDamageReductionMult() * RunicCore.getStatAPI().getPlayerVitality(uuidVictim);
            if (damageMitigationPercent > (Stat.getDamageReductionCap() / 100))
                damageMitigationPercent = (Stat.getDamageReductionCap() / 100); // cap it
            int damage = (int) (event.getAmount() - Math.ceil(event.getAmount() * damageMitigationPercent));
            if (damage < 1)
                damage = 1; // Damage can't drop below 0
            event.setAmount(damage);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExp(RunicExpEvent event) {
        if (event.isCancelled()) return;
        // No exp boost to quest exp
        if (event.getRunicExpSource() == RunicExpEvent.RunicExpSource.QUEST) return;
        Bukkit.broadcastMessage("amount is " + event.getFinalAmount());
        UUID uuid = event.getPlayer().getUniqueId();
        double wisdomBonusPercent = Stat.getExpMult() * RunicCore.getStatAPI().getPlayerWisdom(uuid);
        double bonus = wisdomBonusPercent * event.getOriginalAmount();
        event.setFinalAmount((int) (event.getFinalAmount() + bonus));
        Bukkit.broadcastMessage("amount is now " + event.getFinalAmount());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpell() == null) return; // potions, other effects
        UUID uuid = event.getCaster().getUniqueId();
        double abilityHaste = Stat.getAbilityHaste() * RunicCore.getStatAPI().getPlayerDexterity(uuid);
        double secondsToReduce = event.getSpell().getCooldown() * abilityHaste;
        RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), event.getSpell(), secondsToReduce);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.getSpell() == null) return; // potions, other effects
        UUID uuid = event.getPlayer().getUniqueId();
        double healAmountBonusPercent = Stat.getSpellHealingMult() * RunicCore.getStatAPI().getPlayerWisdom(uuid);
        event.setAmount((int) (event.getAmount() + Math.ceil(event.getAmount() * healAmountBonusPercent)));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellShield(SpellShieldEvent event) {
        if (event.getSpell() == null) return; // potions, other effects
        UUID uuid = event.getPlayer().getUniqueId();
        double shieldAmountBonusPercent = Stat.getSpellShieldingMult() * RunicCore.getStatAPI().getPlayerWisdom(uuid);
        event.setAmount((int) (event.getAmount() + Math.ceil(event.getAmount() * shieldAmountBonusPercent)));
    }

    @EventHandler
    public void onStatChangeEvent(StatChangeEvent event) {
        ManaListener.calculateMaxMana(event.getPlayer());
    }
}
