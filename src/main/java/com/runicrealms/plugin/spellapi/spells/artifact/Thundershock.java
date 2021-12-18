package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicArtifactOnCastEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Thundershock extends Spell implements ArtifactSpell {

    private static final int RADIUS = 4;
    private static final double CHANCE = 1.0;
    private static final double DAMAGE_PERCENT = 1.0;
    private static final String ARTIFACT_ID = "runeforged-crusher";

    public Thundershock() {
        super("Thundershock", "", ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(ARTIFACT_ID)) return;
        if (!(e instanceof RunicArtifactOnCastEvent)) return;
        Spell trigger = ((RunicArtifactOnCastEvent) e).getSpellTrigger();
        if (!(trigger instanceof HealingSpell)) return;
        int healingAmount = ((HealingSpell) trigger).getHeal();
        for (Entity entity : e.getPlayer().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(e.getPlayer(), entity)) continue;
            e.getPlayer().getWorld().playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 1.0f);
            entity.getWorld().spawnParticle(Particle.CRIT_MAGIC, entity.getLocation(), 25, 0.5F, 0.5F, 0.5F, 0);
            DamageUtil.damageEntitySpell((healingAmount * DAMAGE_PERCENT), (LivingEntity) entity, e.getPlayer());
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

