package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cocoon extends Spell implements DistanceSpell, DurationSpell, PhysicalDamageSpell {
    private static final double BEAM_WIDTH = 1.0D;
    private final Map<UUID, Pair<UUID, Long>> lastTimeCocooned;
    private double duration;
    private double damage;
    private double damagePerLevel;
    private double distance;

    public Cocoon() {
        super("Cocoon", CharacterClass.ROGUE);
        this.setDescription("You launch a short-range string of web " +
                "that deals (" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ " +
                "damage to the first enemy hit within " + distance + " blocks, " +
                "then slows them and applies one stack of &usundered &7for " + duration + "s!" +
                "\n\n&2&lEFFECT &uSundered" +
                "\n&7&uSundered &7enemies suffer an additional " +
                        "(5 + 0.04x DEX)% physical damage. Can stack up to 3 times. Each stack expires after 4s.
");
        this.lastTimeCocooned = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            VectorUtil.drawLine(player, Material.COBWEB, player.getEyeLocation(),
                    location, 0.5D, 5, 0.05f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Material.COBWEB, player.getEyeLocation(),
                    livingEntity.getLocation(), 0.5D, 5, 0.05f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.25f, 2.0f);
            addStatusEffect(livingEntity, RunicStatusEffect.SLOW_III, duration, false);
            DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, false, this);
            this.lastTimeCocooned.put(livingEntity.getUniqueId(), Pair.pair(player.getUniqueId(), System.currentTimeMillis()));
        }
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
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
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    /**
     * A method used to check if an entity is still under the spells effect
     *
     * @param uuid     the uuid of the target entity
     * @param duration how long since the cocoon was applied
     * @return if an entity is still under the spells effect
     */
    public boolean isCocooned(@NotNull UUID uuid, double duration) {
        Pair<UUID, Long> data = this.lastTimeCocooned.get(uuid);

        if (data == null) {
            return false;
        }

        return (duration * 1000) + data.second > System.currentTimeMillis();
    }

    /**
     * A method used to check if an entity is still under the spells effect
     *
     * @param uuid the uuid of the target entity
     * @return if an entity is still under the spells effect
     */
    public boolean isCocooned(@NotNull UUID uuid) {
        return this.isCocooned(uuid, this.duration);
    }

    /**
     * A method used to get the uuid of who casted the Cocoon on the target
     *
     * @param target the target
     * @return the uuid of who casted the Cocoon on the target
     */
    @Nullable
    public UUID getCaster(@NotNull UUID target) {
        Pair<UUID, Long> data = this.lastTimeCocooned.get(target);

        if (data == null) {
            return null;
        }

        return data.first;
    }

    /**
     * A method used to get the target of Cocoon from the caster
     *
     * @param caster the caster
     * @return the target of Cocoon from the caster
     */
    @Nullable
    public UUID getTarget(@NotNull UUID caster) {
        for (Map.Entry<UUID, Pair<UUID, Long>> entry : this.lastTimeCocooned.entrySet()) {
            if (entry.getValue().first.equals(caster)) {
                return entry.getKey();
            }
        }

        return null;
    }
}

