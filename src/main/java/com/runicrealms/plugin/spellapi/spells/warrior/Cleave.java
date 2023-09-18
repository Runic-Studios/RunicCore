package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.EntityUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * New spell 1 for berserker
 *
 * @author BoBoBalloon
 */
public class Cleave extends Spell implements PhysicalDamageSpell, DurationSpell, DistanceSpell {
    private static final double ANGLE = Math.PI / 3; //60 degrees
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double distance;
    private double tick;

    public Cleave() {
        super("Cleave", CharacterClass.WARRIOR);
        this.setDescription("You brutally slash around yourself, dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) physical⚔ damage every " + this.tick + "s for " + this.duration + "s! " +
                "The last wound causes enemies to bleed!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        AtomicInteger count = new AtomicInteger(0);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() >= this.duration) {
                task.cancel();
                return;
            }

            Vector direction = player.getLocation().getDirection();
            Location origin = player.getLocation().clone().add(direction.getX(), 0, direction.getZ());

            SlashEffect.slashHorizontal(origin);

            for (Entity entity : EntityUtil.getEnemiesInCone(player, (int) this.distance, Cleave.ANGLE, entity -> this.isValidEnemy(player, entity))) {
                if (!(entity instanceof LivingEntity target)) {
                    continue;
                }

                DamageUtil.damageEntityPhysical(this.damage, target, player, false, false, this);

                if (count.get() >= this.duration - 1) {
                    this.addStatusEffect(target, RunicStatusEffect.BLEED, 6, true, player);
                }
            }

            count.set(count.get() + 1);
        }, 0, (long) (this.tick * 20));
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number tick = (Number) spellData.getOrDefault("tick", 1);
        this.tick = tick.doubleValue();
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getPhysicalDamage() {
        return this.damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }
}
