package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Riftwalk extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double damage;
    private double duration;
    private double radius;
    private double damagePerLevel;

    public Riftwalk() {
        super("Riftwalk", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Upon exiting your &aBlink &7spell, " +
                "you blast all enemies within " + radius + " blocks " +
                "with arcane magic, dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage! If Riftwalk successfully hits " +
                "a target, reduce the cooldown of &aBlink &7by " +
                duration + "s!");
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlinkCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Blink)) return;
        Player caster = event.getCaster();

        // Delay CDR, particle by one tick
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> {
                    boolean foundEnemy = false;
                    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.2f);
                    for (Entity entity : event.getCaster().getWorld().getNearbyEntities
                            (event.getCaster().getLocation(), radius, radius, radius,
                                    target -> isValidEnemy(event.getCaster(), target))) {
                        DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, event.getCaster(), this);
                        foundEnemy = true;
                    }
                    if (foundEnemy) {
                        RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), "Blink", duration);
                    }

                }
                , 1L);
    }

}

