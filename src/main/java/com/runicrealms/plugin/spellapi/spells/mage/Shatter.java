package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * First passive for Cryomancer
 *
 * @author BoBoBalloon
 */
public class Shatter extends Spell implements MagicDamageSpell, ShieldingSpell {
    private final Map<UUID, Long> cooldown;
    private double damage;
    private double damagePerLevel;
    private double shield;
    private double shieldPerLevel;

    public Shatter() {
        super("Shatter", CharacterClass.MAGE);
        this.cooldown = new HashMap<>();
        this.setIsPassive(true);
        this.setDescription("When you land a basic attack on an enemy that has been rooted or stunned, shatter their ice " +
                "dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage granting you a (" + this.shieldPerLevel + " + &f" + this.shieldPerLevel + "x&7 lvl) health✦ shield");
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getShield() {
        return this.shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return this.shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) ||
                (!this.hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.ROOT) && !this.hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.STUN))) {
            return;
        }

        Long cooldown = this.cooldown.get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();

        if (cooldown != null && cooldown + (this.getCooldown() * 1000) > now) { //multiply by 1000 to convert seconds to milliseconds
            return;
        }

        this.cooldown.put(event.getPlayer().getUniqueId(), now);

        this.removeStatusEffect(event.getVictim(), RunicStatusEffect.ROOT);
        this.removeStatusEffect(event.getVictim(), RunicStatusEffect.STUN);

        DamageUtil.damageEntitySpell(this.damage, event.getVictim(), event.getPlayer(), false, this);
        this.shieldPlayer(event.getPlayer(), event.getPlayer(), this.shield, this);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }
}

