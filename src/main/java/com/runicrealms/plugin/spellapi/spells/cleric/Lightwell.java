package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.event.ModeledSpellCollideEvent;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpellStationary;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class Lightwell extends Spell implements DurationSpell, HealingSpell, RadiusSpell {
    public static final String MODEL_ID = "healing_aura";
    private double duration;
    private double healAmt;
    private double radius;
    private double healingPerLevel;

    public Lightwell() {
        super("Lightwell", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Your &aHealing Sprite &7now leaves behind a pool of light for " + duration + "s, " +
                "healing✦ all allies for (" + healAmt + " + &f" + healingPerLevel +
                "x&7 lvl) per second while they stand inside it!");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
    }

    @Override
    public double getHeal() {
        return healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = (int) radius;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(ModeledSpellCollideEvent event) {
        if (!event.getModeledSpell().getModelId().equals(HealingSprite.MODEL_ID)) return;
        if (!hasPassive(event.getModeledSpell().getPlayer().getUniqueId(), this.getName())) return;
        Player player = event.getModeledSpell().getPlayer();
        Entity sprite = event.getModeledSpell().getEntity();
        summonLightwell(player, sprite.getLocation().clone().add(0, 0.3f, 0));
    }

    private void summonLightwell(Player player, Location location) {
        Spell spell = this;
        spawnModel(player, location);
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > duration)
                    this.cancel();

                player.getWorld().playSound(location, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 0.5f);
                player.playSound(
                        location,
                        "samus.bard.healing_aura",
                        SoundCategory.AMBIENT,
                        0.5f,
                        1.0f
                );
                
                player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 15, 2f, 0.75f, 2f, 0);

                for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                    if (TargetUtil.isValidAlly(player, entity)) {
                        healPlayer(player, (Player) entity, healAmt, spell);
                    } else if (TargetUtil.isValidEnemy(player, entity)) {
                        entity.getWorld().spawnParticle(Particle.REDSTONE, ((LivingEntity) entity).getEyeLocation(), 5, 0.5f, 0.5f, 0.5f,
                                new Particle.DustOptions(Color.BLACK, 1));
                    }
                }


            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    private void spawnModel(Player player, Location location) {
        ModeledSpellStationary modeledSpellStationary = new ModeledSpellStationary(
                player,
                MODEL_ID,
                location,
                1.0,
                this.duration,
                null
        );
        modeledSpellStationary.initialize();
        modeledSpellStationary.getModeledEntity().getModels().forEach((s, activeModel) -> activeModel.getAnimationHandler().playAnimation(
                "idle",
                0.5,
                0.5,
                1.0,
                false
        ));
    }
}

