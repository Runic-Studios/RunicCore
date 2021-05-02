package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class BaseStatListener implements Listener {

    private static final float DEFAULT_WALKSPEED = 0.2f;
    // todo: add vitality to spell, weapon damage event
    // todo: add hard cap to dmg reduction from vitality
    // todo: update gear scanner, add runic items api
    // todo: update runic artifacts using runic items api and new spells (maybe not - prob in items)

    @EventHandler
    public void onHealthRegen(HealthRegenEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double healthRegenBonusPercent = BaseStatEnum.getHealthRegenMult() * RunicCoreAPI.getPlayerVitality(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * healthRegenBonusPercent)));
    }

    @EventHandler
    public void onManaRegen(ManaRegenEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double manaRegenBonusPercent = BaseStatEnum.getManaRegenMult() * RunicCoreAPI.getPlayerWisdom(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * manaRegenBonusPercent)));
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        UUID uuid = e.getVictim().getUniqueId();
        double damageMitigationPercent = BaseStatEnum.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuid);
        if (damageMitigationPercent > BaseStatEnum.getDamageReductionCap())
            damageMitigationPercent = BaseStatEnum.getDamageReductionCap(); // cap it
        e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
    }

    @EventHandler
    public void onStatChangeEvent(StatChangeEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double walkBonusPercent = BaseStatEnum.getMovementSpeedMult() * RunicCoreAPI.getPlayerDexterity(uuid);
        e.getPlayer().setWalkSpeed((float) (DEFAULT_WALKSPEED + (DEFAULT_WALKSPEED * walkBonusPercent)));
        RunicCoreAPI.updateMaxMana(e.getPlayer());
    }

    @EventHandler
    public void onSpellHealing(SpellHealEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double healAmountBonusPercent = BaseStatEnum.getSpellHealingMult() * RunicCoreAPI.getPlayerWisdom(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * healAmountBonusPercent)));
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double magicDamageBonusPercent = BaseStatEnum.getMagicDmgMult() * RunicCoreAPI.getPlayerIntelligence(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * magicDamageBonusPercent)));
    }

    @EventHandler
    public void onRangedDamage(WeaponDamageEvent e) {
        if (e.isRanged()) {
            UUID uuid = e.getPlayer().getUniqueId();
            double rangedDamageBonusPercent = BaseStatEnum.getRangedDmgMult() * RunicCoreAPI.getPlayerDexterity(uuid);
            e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * rangedDamageBonusPercent)));
        } else {
            UUID uuid = e.getPlayer().getUniqueId();
            double meleeDamageBonusPercent = BaseStatEnum.getMeleeDmgMult() * RunicCoreAPI.getPlayerStrength(uuid);
            e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * meleeDamageBonusPercent)));
        }
    }
}
