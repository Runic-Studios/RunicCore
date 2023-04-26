package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlessedBlade extends Spell implements HealingSpell, MagicDamageSpell, RadiusSpell {
    private final Map<UUID, Integer> blessedBladeMap = new HashMap<>();
    private double heal;
    private double healingPerLevel;
    private double magicDamage;
    private double magicDamagePerLevel;
    private double maxTargets;
    private double radius;

    public BlessedBlade() {
        super("Blessed Blade", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Each time you cast a spell, your next two basic attacks " +
                "deal an additional (" + magicDamage + " + &f" + magicDamagePerLevel + "x&7 lvl) " +
                "magicʔ damage! They also heal you and up to " + maxTargets + " allies " +
                "within " + radius + " blocks for (" + heal + " + &f" + healingPerLevel + "x&7 lvl) health!");
    }

    @Override
    public double getHeal() {
        return heal;
    }

    @Override
    public void setHeal(double heal) {
        this.heal = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return this.healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

    @Override
    public double getMagicDamage() {
        return magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @Override
    public void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number magicDamagePerLevel = (Number) spellData.getOrDefault("magic-damage-per-level", 0);
        setMagicDamagePerLevel(magicDamagePerLevel.doubleValue());
        Number maxTargets = (Number) spellData.getOrDefault("max-targets", 0);
        setMaxTargets(maxTargets.doubleValue());
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!this.blessedBladeMap.containsKey(event.getPlayer().getUniqueId())) return;
        Player player = event.getPlayer();
        int current = this.blessedBladeMap.get(player.getUniqueId());
        this.blessedBladeMap.put(player.getUniqueId(), current - 1);
        // Additional damage
        DamageUtil.damageEntitySpell(magicDamage, event.getVictim(), player, this);
        // Heal caster and allies
        healPlayer(player, player, heal, this);
        int alliesHealed = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            if (entity.equals(player)) continue;
            healPlayer(player, (Player) entity, heal, this);
            alliesHealed++;
            if (alliesHealed >= maxTargets)
                break;
        }
        // Remove player if needed
        if (this.blessedBladeMap.get(player.getUniqueId()) <= 0) {
            this.blessedBladeMap.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        this.blessedBladeMap.put(event.getCaster().getUniqueId(), 2);
    }

    public void setMaxTargets(double maxTargets) {
        this.maxTargets = maxTargets;
    }

}
