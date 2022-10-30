package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicArtifactOnHitEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.concurrent.ThreadLocalRandom;

public class Maelstrom extends Spell implements ArtifactSpell {

    private static final int DURATION = 3;
    private static final int RADIUS = 2;
    private static final double CHANCE = 0.25;
    private static final double DAMAGE_PERCENT = 2.0;
    private static final String ARTIFACT_ID = "runeforged-scepter";

    public Maelstrom() {
        super("Maelstrom", "", ChatColor.WHITE, ClassEnum.MAGE, 30, 0);
        this.setIsPassive(false);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        if (!(e instanceof RunicArtifactOnHitEvent)) return;
        RunicArtifactOnHitEvent onHitEvent = (RunicArtifactOnHitEvent) e;
        if (isOnCooldown(e.getPlayer())) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
        int damage = (int) ((e.getRunicItemArtifact().getWeaponDamage().getRandomValue() * DAMAGE_PERCENT) + RunicCoreAPI.getPlayerStrength(e.getPlayer().getUniqueId()));
        createMaelstrom(e.getPlayer(), onHitEvent.getVictim(), damage);
        e.setArtifactSpellToCast(this);
    }

    /**
     * Create a maelstrom effect at the location of the victim
     *
     * @param player who cast the spell
     * @param victim to be stunned
     * @param damage of the spell
     */
    private void createMaelstrom(Player player, Entity victim, int damage) {
        player.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        for (Entity en : player.getWorld().getNearbyEntities(victim.getLocation(), RADIUS, RADIUS, RADIUS)) {
            if (!(isValidEnemy(player, en))) continue;
            LivingEntity livingEntity = (LivingEntity) en;
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 2.0f);
            livingEntity.getWorld().spawnParticle(Particle.CRIT_MAGIC, livingEntity.getLocation(), 25, 0.5f, 0.5f, 0.5f, 0);
            DamageUtil.damageEntitySpell(damage, livingEntity, player);
            if (!(livingEntity instanceof Player)) // doesn't stun players
                addStatusEffect(livingEntity, EffectEnum.STUN, DURATION);
        }
    }

    @Override
    public String getArtifactId() {
        return ARTIFACT_ID;
    }

    @Override
    public double getChance() {
        return CHANCE;
    }
}

