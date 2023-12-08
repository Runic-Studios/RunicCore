package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * New ultimate spell for Berserker
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Adrenaline extends Spell implements DurationSpell {
    private final Map<UUID, RagePayload> rageMap;
    private double buffDuration; // TODO: do we ever remove them from the rage map?
    private int maxStacks;
    private double percent;

    public Adrenaline() {
        super("Adrenaline", CharacterClass.WARRIOR);
        this.setDescription("For the next " + this.buffDuration + "s, gain Speed II! " +
                "For the duration, each of your basic attacks against &cbleeding &7enemies grant you a stack of rage, " +
                "causing you to deal " + (this.percent * 100) + "% more physical⚔ damage, " +
                "stacking up to " + this.maxStacks + " times. " +
                "If you reach maximum rage before the speed wears off, cleanse yourself of all debuffs " +
                "and reset the duration of the speed bonus!");
        this.rageMap = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(1.0F, 30, 10.0F).playParticle(player, Particle.REDSTONE, player.getLocation(), Color.RED);
        this.rageMap.put(player.getUniqueId(), new RagePayload());
        this.addStatusEffect(player, RunicStatusEffect.SPEED_II, this.buffDuration, false);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            this.rageMap.remove(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.0f);
        }, (long) buffDuration * 20);
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 5);
        this.maxStacks = maxStacks.intValue();
        Number percent = (Number) spellData.getOrDefault("percent", .03);
        this.percent = percent.doubleValue();
    }

    @Override
    public double getDuration() {
        return buffDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.buffDuration = duration;
    }

    public void addStack(Player caster) {
        RagePayload ragePayload = this.rageMap.get(caster.getUniqueId());
        int currentStacks = ragePayload.getStacks();
        int newStacks = Math.min(currentStacks + 1, this.maxStacks);

        ragePayload.setStacks(newStacks);
        this.rageMap.put(caster.getUniqueId(), ragePayload);

        caster.sendMessage(ColorUtil.format("&aYou have " + newStacks + " stacks of rage!"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!this.rageMap.containsKey(playerId) || !event.isBasicAttack()) return;

        RagePayload ragePayload = this.rageMap.get(playerId);
        int stacks = ragePayload.getStacks();

        if (this.hasSpellEffect(event.getVictim().getUniqueId(), SpellEffectType.BLEED) && stacks < this.maxStacks) {
            this.addStack(event.getPlayer());
            stacks = ragePayload.getStacks();

            if (!ragePayload.isEnraged() && stacks >= this.maxStacks) {
                ragePayload.setEnraged(true);
                event.getPlayer().sendMessage(ColorUtil.format("&aYou are &c&lenraged&r&a!"));
                RunicCore.getStatusEffectAPI().cleanse(playerId);
                this.addStatusEffect(event.getPlayer(), RunicStatusEffect.SPEED_II, this.buffDuration, false);
            }
        }

        // Apply damage multiplier
        double multiplier = 1 + (this.percent * stacks);
        event.setAmount((int) (event.getAmount() * multiplier));
    }

    static class RagePayload {
        private AtomicInteger stacks;
        private boolean isEnraged;

        public RagePayload() {
            this.stacks = new AtomicInteger(0);
            this.isEnraged = false;
        }

        public int getStacks() {
            return stacks.get();
        }

        public void setStacks(int stacks) {
            this.stacks = new AtomicInteger(stacks);
        }

        public boolean isEnraged() {
            return isEnraged;
        }

        public void setEnraged(boolean enraged) {
            isEnraged = enraged;
        }
    }
}

