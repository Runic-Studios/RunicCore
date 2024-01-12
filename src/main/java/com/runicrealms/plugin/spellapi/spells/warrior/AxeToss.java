package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.BleedEffect;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.FloatingItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AxeToss extends Spell implements DurationSpell, PhysicalDamageSpell {
    private final HashMap<UUID, UUID> hasBeenHit;
    private double damage;
    private double damagePerLevel;
    private double slowDuration;

    public AxeToss() {
        super("Axe Toss", CharacterClass.WARRIOR);
        hasBeenHit = new HashMap<>();
        this.setDescription("You throw your weapon, dealing (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage and applying &cbleed &7to the first enemy hit. " +
                "If the enemy is already &cbleeding&7, they are slowed for " + this.slowDuration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        ItemStack artifact = player.getInventory().getItemInMainHand();
        Material artifactType = artifact.getType();
        int durability = ((Damageable) Objects.requireNonNull(artifact.getItemMeta())).getDamage();
        Vector path = player.getEyeLocation().getDirection().normalize().multiply(1.5);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 0.5f, 1.0f);
        Entity projectile = FloatingItemUtil.spawnFloatingItem(player.getEyeLocation(), artifactType, 0, path, durability);
        projectile.setTicksLived(1);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (projectile.isOnGround() || projectile.isDead()) {
                if (projectile.isOnGround()) {
                    projectile.remove();
                }
                task.cancel();
                return;
            }

            Location loc = projectile.getLocation();
            projectile.getWorld().spawnParticle(Particle.CRIT, projectile.getLocation(), 1, 0, 0, 0, 0);

            for (Entity entity : projectile.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5, target -> isValidEnemy(player, target))) {
                if (hasBeenHit.get(player.getUniqueId()) == entity.getUniqueId()) continue;
                hasBeenHit.put(player.getUniqueId(), entity.getUniqueId()); // prevent concussive hits


                Optional<SpellEffect> bleedEffect = this.getSpellEffect(player.getUniqueId(), entity.getUniqueId(), SpellEffectType.BLEED);
                if (bleedEffect.isEmpty()) {
                    new BleedEffect(player, (LivingEntity) entity).initialize();
                } else {
                    ((BleedEffect) bleedEffect.get()).refreshStacks();
                    addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_III, slowDuration, true);
                    entity.getWorld().spawnParticle
                            (Particle.VILLAGER_ANGRY, entity.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                }

                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, player, false, false, this);
                projectile.remove();
            }
        }, 0, 1);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                hasBeenHit::clear, (int) slowDuration * 20L);
    }

    @Override
    public double getDuration() {
        return slowDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.slowDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("slow-duration", 0);
        setDuration(duration.doubleValue());
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
}

