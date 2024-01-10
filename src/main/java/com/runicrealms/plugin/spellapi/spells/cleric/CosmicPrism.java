package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Hexagon;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class CosmicPrism extends Spell implements DurationSpell, RadiusSpell, ShieldingSpell {
    private double duration;
    private double percent;
    private double period;
    private double radius;
    private double shield;
    private double shieldPerLevel;

    public CosmicPrism() {
        super("Cosmic Prism", CharacterClass.CLERIC);
        this.setDescription("You summon a prism of starlight that illuminates " +
                "the ground in a " + radius + " block radius for the next " + duration + "s. " +
                "Allies standing in the light receive a " +
                "stacking (" + shield + " + &f" + shieldPerLevel +
                "x&7 lvl) &eshield &7every " + period + "s! " +
                "When the prism expires, it releases an additional &eshield &7equal to " +
                (percent * 100) + "% of its base amount!");
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
        Number period = (Number) spellData.getOrDefault("period", 0);
        setPeriod(period.doubleValue());
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getLocation();
        new Hexagon(castLocation, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, duration, radius, Material.LAPIS_BLOCK).runTaskTimer(RunicCore.getInstance(), 0, 20L);
        new Hexagon(castLocation, Sound.ITEM_FIRECHARGE_USE, 0.25f, duration, radius, Particle.REDSTONE, Color.YELLOW).runTaskTimer(RunicCore.getInstance(), 0, 20L);
        Spell spell = this;
        new BukkitRunnable() {
            double count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                    new Hexagon(castLocation, Sound.ITEM_FIRECHARGE_USE, 0.25f, 1, radius, Particle.FIREWORKS_SPARK, Color.YELLOW).runTaskTimer(RunicCore.getInstance(), 0, 20L);
                    castLocation.getWorld().playSound(castLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.5f);
                    for (Entity entity : castLocation.getWorld().getNearbyEntities(castLocation, radius, radius, radius, target -> isValidAlly(player, target))) {
                        RunicCore.getSpellAPI().shieldPlayer(player, (Player) entity, percent * shield, spell);
                    }
                    return;
                }
                count += period;
                for (Entity entity : castLocation.getWorld().getNearbyEntities(castLocation, radius, radius, radius, target -> isValidAlly(player, target))) {
                    RunicCore.getSpellAPI().shieldPlayer(player, (Player) entity, shield, spell);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) period * 20L);
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
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

}

