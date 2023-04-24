package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Adrenaline extends Spell implements AttributeSpell, DurationSpell {
    private final Map<UUID, AdrenalineTracker> adrenalineMap = new ConcurrentHashMap<>();
    private double baseValue;
    private double duration;
    private double maxStacks;
    private double multiplier;
    private String statName;

    public Adrenaline() {
        super("Adrenaline", CharacterClass.WARRIOR);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("For the next " + duration + "s, each time you deal damage, " +
                "gain a stack of Adrenaline, granting additional physical damage equal to" +
                " (" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7). " +
                "Each hit refreshes the duration of your stacks, " +
                "stacking up to " + maxStacks + " times. At max stacks, " +
                "extend the duration of this effect by 5s and gain Speed II " +
                "for the remainder of the effect!");
    }

    private int damageBasedOnStacks(Player player) {
        // If the player is at max stacks, give them the max stacks damage
        if (adrenalineMap.get(player.getUniqueId()).getStacks() >= maxStacks) {
//            Bukkit.broadcastMessage("max stacks damage");
            double damageBonus = multiplier * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
            damageBonus *= maxStacks;
            return (int) (baseValue + damageBonus);
        }
        // Otherwise increment stacks
        adrenalineMap.get(player.getUniqueId()).increment();
//        Bukkit.broadcastMessage(adrenalineMap.get(player.getUniqueId()).getStacks() + " is stacks");
        double damageBonus = multiplier * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
        damageBonus *= adrenalineMap.get(player.getUniqueId()).getStacks();
//        Bukkit.broadcastMessage("damage bonus is " + damageBonus + " percent");
        // If this current increment brought us to threshold, refresh duration
        if (adrenalineMap.get(player.getUniqueId()).getStacks() >= maxStacks) {
            refresh(player);
        }

        return (int) (baseValue + damageBonus);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
        new HelixParticleFrame(0.5F, 3, 2.5F).playParticle(player, Particle.REDSTONE, player.getLocation(), Color.RED);
        adrenalineMap.put(player.getUniqueId(), new AdrenalineTracker(player.getUniqueId()));
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

    @Override
    public void loadAttributeData(Map<String, Object> spellData) {
        setStatName((String) spellData.getOrDefault("attribute", ""));
        Number baseValue = (Number) spellData.getOrDefault("attribute-base-value", 0);
        setBaseValue(baseValue.doubleValue());
        Number multiplier = (Number) spellData.getOrDefault("attribute-multiplier", 0);
        setMultiplier(multiplier.doubleValue());
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 0);
        setMaxStacks(maxStacks.doubleValue());
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMagicDamage(MagicDamageEvent event) {
        if (!adrenalineMap.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        event.setAmount(event.getAmount() + damageBasedOnStacks(player));
    }

    /**
     * Activate on-hit effects
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSuccessfulHit(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!adrenalineMap.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        event.setAmount(event.getAmount() + damageBasedOnStacks(player));
    }

    private void refresh(Player player) {
        adrenalineMap.get(player.getUniqueId()).refreshTask();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.REDSTONE, duration, 0, 20, Color.RED);
        addStatusEffect(player, RunicStatusEffect.SPEED_II, duration, false);
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    class AdrenalineTracker {
        private final UUID uuid;
        private final AtomicInteger stacks;
        private BukkitTask bukkitTask;

        public AdrenalineTracker(UUID uuid) {
            this.uuid = uuid;
            this.stacks = new AtomicInteger(0);
            this.bukkitTask =
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                            () -> {
                                adrenalineMap.remove(uuid);
//                                Bukkit.broadcastMessage("effect timeout");
                            }, (int) duration * 20L);
        }

        public int getStacks() {
            return stacks.get();
        }

        public UUID getUuid() {
            return uuid;
        }

        private void increment() {
            this.stacks.getAndIncrement();
        }

        /**
         * Refreshes the task that expires the buff
         */
        public void refreshTask() {
            this.bukkitTask.cancel();
            this.bukkitTask =
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                            () -> {
                                adrenalineMap.remove(uuid);
//                                Bukkit.broadcastMessage("effect timeout");
                            }, (int) duration * 20L);
        }
    }
}

