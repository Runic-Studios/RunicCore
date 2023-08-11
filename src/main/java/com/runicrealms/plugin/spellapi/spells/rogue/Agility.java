package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Agility extends Spell implements AttributeSpell {
    private static final Set<UUID> agilityPlayers = new HashSet<>();
    private double baseValue;
    private double multiplier;
    private String statName;

    public Agility() {
        super("Agility", CharacterClass.ROGUE);
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("You've learned to use your speed to your advantage! " +
                "While &aDash &7is active, you gain bonus basic attack damage " +
                "equal to (" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7)!");
    }

    /**
     * Empower on dash cast
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSprintCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof Dash dash)) return;
        agilityPlayers.add(event.getCaster().getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> agilityPlayers.remove(event.getCaster().getUniqueId()), (long) dash.getDuration() * 20L);
    }

    @EventHandler(priority = EventPriority.HIGH) // runs last
    public void onAgilityHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!agilityPlayers.contains(event.getPlayer().getUniqueId())) return;
        int bonus = (int) (multiplier * RunicCore.getStatAPI().getStat(event.getPlayer().getUniqueId(), statName));
        event.setAmount((int) (event.getAmount() + baseValue + Math.max(0, bonus))); // Bonus cannot be negative
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 0.25F);
        event.getVictim().getWorld().spawnParticle
                (Particle.CLOUD, event.getVictim().getEyeLocation(), 5, 1.0F, 0, 0, 0); // 0.3F
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }
}

