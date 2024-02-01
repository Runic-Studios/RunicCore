package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Scurvy extends Spell implements DurationSpell, PhysicalDamageSpell {
    private final Set<UUID> buffed;
    private double duration;
    private double damage;
    private double damagePerLevel;

    public Scurvy() {
        super("Scurvy", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Your first basic attack after casting &aDash &7is laden with disease! " +
                "Your target receives nausea for the next " + duration + "s. " +
                "Against mobs, you deal (" + this.damage + " +&f " + this.damagePerLevel + "x&7 lvl) extra physical⚔ damage!");
        this.buffed = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.buffed.contains(event.getPlayer().getUniqueId()) || !event.isBasicAttack()) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new DebuffEvent(event.getPlayer(), event.getVictim()));

        if (event.getVictim() instanceof Player) {
            event.getVictim().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) (this.duration * 20), 3, false, false, false));
            return;
        }

        double damage = this.damage + (this.damagePerLevel * event.getPlayer().getLevel());

        event.setAmount(event.getAmount() + (int) damage);
        this.buffed.remove(event.getPlayer().getUniqueId());

        event.getPlayer().getWorld().playSound(event.getVictim().getLocation(), Sound.ENTITY_DROWNED_HURT, 0.5F, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSpellCast(SpellCastEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName()) || !(event.getSpell() instanceof Dash)) {
            return;
        }

        this.buffed.add(event.getCaster().getUniqueId());
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.buffed.remove(event.getPlayer().getUniqueId());
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
        return this.damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    public static class DebuffEvent extends Event {
        private static final HandlerList HANDLER_LIST = new HandlerList();
        private final Player caster;
        private final LivingEntity victim;

        public DebuffEvent(@NotNull Player caster, @NotNull LivingEntity victim) {
            this.caster = caster;
            this.victim = victim;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }

        @NotNull
        public Player getCaster() {
            return this.caster;
        }

        @NotNull
        public LivingEntity getVictim() {
            return this.victim;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }
    }
}

