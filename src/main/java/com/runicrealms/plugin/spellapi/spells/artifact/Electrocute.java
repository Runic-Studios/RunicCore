package com.runicrealms.plugin.spellapi.spells.artifact;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Electrocute extends Spell {

    private static final String ARTIFACT_ID = "lost-runeblade";
    private static final int MANA_REGEN_AMOUNT = 15;
    private static final int RADIUS = 3;
    private static final double DAMAGE_PERCENT = 0.75;

    public Electrocute() {
        super("Electrocute", "", ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onArtifactUse(RunicItemArtifactTriggerEvent e) {
        if (!e.getRunicItemArtifact().getTemplateId().equals(ARTIFACT_ID)) return;
        int damage = (int) ((e.getRunicItemArtifact().getWeaponDamage().getRandomValue() * DAMAGE_PERCENT) + RunicCoreAPI.getPlayerStrength(e.getPlayer().getUniqueId()));
        RunicCore.getRegenManager().addMana(e.getPlayer(), MANA_REGEN_AMOUNT);
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.5f, 1.0f);
        e.getPlayer().getWorld().playSound(e.getVictim().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 1.0f);
        e.getVictim().getWorld().spawnParticle(Particle.CRIT_MAGIC, e.getVictim().getLocation(), 25, 0.5F, 0.5F, 0.5F, 0);
        for (Entity en : e.getVictim().getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(e.getPlayer(), en)) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) en, e.getPlayer());
        }
    }
}

