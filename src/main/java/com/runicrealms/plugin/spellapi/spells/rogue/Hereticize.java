package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Hereticize extends Spell implements DurationSpell, MagicDamageSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;

    public Hereticize() {
        super("Hereticize", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Anytime a &7&obranded &7enemy uses an ability, " +
                "all of your active ability cooldowns are reduced by " + duration + "s! " +
                "&7&oBranded &7enemies take an additional " +
                "(" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage on " +
                "hit whenever they suffer damage from basic attacks!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (SilverBolt.getBrandedEnemiesMap().isEmpty()) return;
        for (UUID witchHunterUuid : SilverBolt.getBrandedEnemiesMap().keySet()) {
            UUID uuidBranded = SilverBolt.getBrandedEnemiesMap().get(witchHunterUuid);
            // Check to find our branded victim
            if (!uuidBranded.equals(event.getCaster().getUniqueId())) continue;
            Player witchHunter = Bukkit.getPlayer(witchHunterUuid);
            if (witchHunter == null) continue;
            ConcurrentHashMap.KeySetView<Spell, Long> spellsOnCooldown = RunicCore.getSpellAPI().getSpellsOnCooldown(witchHunterUuid);
            if (spellsOnCooldown == null) continue; // No spells on cooldown for Witch Hunter
            spellsOnCooldown.forEach(spell -> RunicCore.getSpellAPI().reduceCooldown(witchHunter, spell, duration));
        }
    }

    private void hereticDamage(LivingEntity livingEntity) {
        SilverBolt.getBrandedEnemiesMap().forEach((uuidCaster, uuidVictim) -> {
            if (uuidVictim.equals(livingEntity.getUniqueId())) {
                Player player = Bukkit.getPlayer(uuidCaster);
                if (player != null) {
                    livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_WITCH_HURT, 0.25f, 2.0f);
                    DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (SilverBolt.getBrandedEnemiesMap().isEmpty()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        hereticDamage(event.getVictim());
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }
}
