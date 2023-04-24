package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicArtifactOnHitEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
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
        super("Maelstrom", CharacterClass.MAGE);
        this.setIsPassive(false);
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
                addStatusEffect(livingEntity, RunicStatusEffect.STUN, DURATION, true);
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

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent event) {
        if (!event.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
        if (!(event instanceof RunicArtifactOnHitEvent onHitEvent)) return;
        if (isOnCooldown(event.getPlayer())) return;
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > getChance()) return;
        int damage = (int) ((event.getRunicItemArtifact().getWeaponDamage().getRandomValue() * DAMAGE_PERCENT) + RunicCore.getStatAPI().getPlayerStrength(event.getPlayer().getUniqueId()));
        createMaelstrom(event.getPlayer(), onHitEvent.getVictim(), damage);
        event.setArtifactSpellToCast(this);
    }
}

