package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.runicitems.Stat;
import org.bukkit.*;
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
            double damageBonus = multiplier * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
            damageBonus *= maxStacks;
            return (int) (baseValue + damageBonus);
        }
        // Otherwise increment stacks
        adrenalineMap.get(player.getUniqueId()).increment();
        double damageBonus = multiplier * RunicCore.getStatAPI().getPlayerStrength(player.getUniqueId());
        damageBonus *= adrenalineMap.get(player.getUniqueId()).getStacks();
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
        new HelixParticleFrame(1.0F, 30, 10.0F).playParticle(player, Particle.REDSTONE, player.getLocation(), Color.RED);
        adrenalineMap.put(player.getUniqueId(), new AdrenalineTracker(player));
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
        private final Player player;
        private final AtomicInteger stacks;
        private BukkitTask bukkitTask;

        public AdrenalineTracker(Player player) {
            this.player = player;
            this.stacks = new AtomicInteger(0);
            this.bukkitTask =
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                            () -> {
                                adrenalineMap.remove(player.getUniqueId());
                                player.sendMessage(ChatColor.GRAY + "Adrenaline has expired.");
                            }, (int) duration * 20L);
        }

        public Player getPlayer() {
            return player;
        }

        public int getStacks() {
            return stacks.get();
        }

        private void increment() {
            this.stacks.getAndIncrement();
            // Send message feedback
            player.sendMessage(ChatColor.GRAY + "Adrenaline stacks: " + ChatColor.YELLOW + this.stacks.get());
        }

        /**
         * Refreshes the task that expires the buff
         */
        public void refreshTask() {
            this.bukkitTask.cancel();
            this.bukkitTask =
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                            () -> {
                                adrenalineMap.remove(player.getUniqueId());
                                player.sendMessage(ChatColor.GRAY + "Adrenaline has expired.");
                            }, (int) duration * 20L);
        }
    }
}

