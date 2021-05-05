package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class PlayerStatListener implements Listener {

    private static final float DEFAULT_WALKSPEED = 0.2f;
    // todo: update runic artifacts using runic items api and new spells (maybe not - prob in items)
    // todo: add new magicspell, healingspell sub classes with just like 1-2 fields. then let them scale based on LEVEL

    @EventHandler
    public void onHealthRegen(HealthRegenEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double healthRegenBonusPercent = PlayerStatEnum.getHealthRegenMult() * RunicCoreAPI.getPlayerVitality(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * healthRegenBonusPercent)));
    }

    @EventHandler
    public void onManaRegen(ManaRegenEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double manaRegenBonusPercent = PlayerStatEnum.getManaRegenMult() * RunicCoreAPI.getPlayerWisdom(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * manaRegenBonusPercent)));
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        UUID uuid = e.getVictim().getUniqueId();
        double damageMitigationPercent = PlayerStatEnum.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuid);
        if (damageMitigationPercent > PlayerStatEnum.getDamageReductionCap())
            damageMitigationPercent = PlayerStatEnum.getDamageReductionCap(); // cap it
        e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
    }

    @EventHandler
    public void onStatChangeEvent(StatChangeEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double walkBonusPercent = PlayerStatEnum.getMovementSpeedMult() * RunicCoreAPI.getPlayerDexterity(uuid);
        e.getPlayer().setWalkSpeed((float) (DEFAULT_WALKSPEED + (DEFAULT_WALKSPEED * walkBonusPercent)));
        RunicCoreAPI.updateMaxMana(e.getPlayer());
    }

    @EventHandler
    public void onSpellHealing(SpellHealEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double healAmountBonusPercent = PlayerStatEnum.getSpellHealingMult() * RunicCoreAPI.getPlayerWisdom(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * healAmountBonusPercent)));
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        double magicDamageBonusPercent = PlayerStatEnum.getMagicDmgMult() * RunicCoreAPI.getPlayerIntelligence(uuid);
        e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * magicDamageBonusPercent)));
        /*
        Defense
         */
        UUID uuidVictim = e.getEntity().getUniqueId();
        double damageMitigationPercent = PlayerStatEnum.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuidVictim);
        if (damageMitigationPercent > PlayerStatEnum.getDamageReductionCap())
            damageMitigationPercent = PlayerStatEnum.getDamageReductionCap(); // cap it
        e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
    }

    @EventHandler
    public void onRangedDamage(WeaponDamageEvent e) {
        if (e.isRanged()) {
            UUID uuid = e.getPlayer().getUniqueId();
            double rangedDamageBonusPercent = PlayerStatEnum.getRangedDmgMult() * RunicCoreAPI.getPlayerDexterity(uuid);
            e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * rangedDamageBonusPercent)));
        } else {
            UUID uuid = e.getPlayer().getUniqueId();
            double meleeDamageBonusPercent = PlayerStatEnum.getMeleeDmgMult() * RunicCoreAPI.getPlayerStrength(uuid);
            e.setAmount((int) (e.getAmount() + Math.ceil(e.getAmount() * meleeDamageBonusPercent)));
        }
        /*
        Defense
         */
        UUID uuidVictim = e.getEntity().getUniqueId();
        double damageMitigationPercent = PlayerStatEnum.getDamageReductionMult() * RunicCoreAPI.getPlayerVitality(uuidVictim);
        if (damageMitigationPercent > PlayerStatEnum.getDamageReductionCap())
            damageMitigationPercent = PlayerStatEnum.getDamageReductionCap(); // cap it
        e.setAmount((int) (e.getAmount() - Math.ceil(e.getAmount() * damageMitigationPercent)));
    }
}
