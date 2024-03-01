package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.modeled.ModeledStandAnimated;
import com.runicrealms.plugin.spellapi.modeled.StandSlot;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

public class Riftwalk extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int MODEL_DATA = 2732;
    private static final int[] MODEL_DATA_ARRAY = new int[]{
            MODEL_DATA,
            2733,
            2734,
            2735,
            2736,
            2737,
            MODEL_DATA,
            2733,
            2734,
            2735,
            2736,
            2737,
            MODEL_DATA,
            2733,
            2734,
            2735,
            2736,
            2737,
    };
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

    public static void spawnParticle(Player player) {
        new ModeledStandAnimated(
                player,
                player.getLocation().clone().add(0, 0.3f, 0),
                new Vector(0, 0, 0),
                MODEL_DATA,
                3.0,
                1.0,
                StandSlot.HEAD,
                target -> false,
                MODEL_DATA_ARRAY
        );
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
                    spawnParticle(caster);
                    caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.2f);
                    for (Entity entity : event.getCaster().getWorld().getNearbyEntities
                            (event.getCaster().getLocation(), radius, radius, radius,
                                    target -> TargetUtil.isValidEnemy(event.getCaster(), target))) {
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

