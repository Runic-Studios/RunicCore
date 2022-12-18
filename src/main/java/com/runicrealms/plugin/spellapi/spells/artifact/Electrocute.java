package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicArtifactOnKillEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Electrocute extends Spell implements ArtifactSpell {

    private static final int MANA_REGEN_AMOUNT = 15;
    private static final int RADIUS = 3;
    private static final double CHANCE = 1.0;
    private static final double DAMAGE_PERCENT = 0.75;
    private static final String ARTIFACT_ID = "lost-runeblade";

    public Electrocute() {
        super("Electrocute", "", ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
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
        if (!(event instanceof RunicArtifactOnKillEvent)) return;
        RunicArtifactOnKillEvent onKillEvent = (RunicArtifactOnKillEvent) event;
        int damage = (int) ((event.getRunicItemArtifact().getWeaponDamage().getRandomValue() * DAMAGE_PERCENT) + RunicCore.getStatAPI().getPlayerStrength(event.getPlayer().getUniqueId()));
        RunicCore.getRegenManager().addMana(event.getPlayer(), MANA_REGEN_AMOUNT);
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.5f, 1.0f);
        event.getPlayer().getWorld().playSound(onKillEvent.getVictim().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 1.0f);
        onKillEvent.getVictim().getWorld().spawnParticle(Particle.CRIT_MAGIC, onKillEvent.getVictim().getLocation(), 25, 0.5F, 0.5F, 0.5F, 0);
        for (Entity en : onKillEvent.getVictim().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(event.getPlayer(), en)) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) en, event.getPlayer());
        }
    }
}

