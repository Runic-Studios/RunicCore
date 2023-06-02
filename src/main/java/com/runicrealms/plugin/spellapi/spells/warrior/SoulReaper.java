package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.StackTask;
import com.runicrealms.plugin.spellapi.spellutil.particles.RotatingParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SoulReaper extends Spell implements DurationSpell {
    private final Map<Player, StackTask> reaperTaskMap = new HashMap<>();
    private final Map<Player, Double> tMap = new HashMap<>(); // Keeps track of the angle for the rotating particle task
    private double duration;
    private double maxStacks;
    private double percent;

    public SoulReaper() {
        super("Soul Reaper", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Landing your &aDevour &7or &aUmbral Grasp &7spell " +
                "on enemies builds up &f&osouls&7. " +
                "For each stack of souls you have, " +
                "you take " + (percent * 100) + "% less damage! " +
                "Your souls last " + duration + "s and have their duration reset when stacked. " +
                "Max " + maxStacks + " souls.");
        startParticleTask();
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getVictim().getUniqueId(), this.getName())) return;
        if (reaperTaskMap.isEmpty()) return;
        if (!(event.getVictim() instanceof Player victim)) return;
        event.setAmount(reducedDamage(victim, event.getAmount()));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getVictim().getUniqueId(), this.getName())) return;
        if (reaperTaskMap.isEmpty()) return;
        if (!(event.getVictim() instanceof Player victim)) return;
        event.setAmount(reducedDamage(victim, event.getAmount()));
    }

    private int reducedDamage(Player victim, int amount) {
        if (!reaperTaskMap.containsKey(victim)) return amount;
        int stacks = reaperTaskMap.get(victim).getStacks().get();
        double reducedAmount = amount * (stacks * percent);
        return (int) (amount - reducedAmount);
    }

    private void startParticleTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (reaperTaskMap.isEmpty()) return;
            reaperTaskMap.forEach((player, stackTask) -> {
                int stacks = stackTask.getStacks().get();
                double t = tMap.getOrDefault(player, 0.0);
                new RotatingParticleEffect(player, Particle.REDSTONE, 1.0, 360.0 / stacks, t, Color.fromRGB(185, 251, 185)).show();
                tMap.put(player, t + Math.PI / 16);
            });
        }, 0, 1L);
    }

    @EventHandler
    public void onCast(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        // Damage reduction mechanic
        if (event.getVictim() instanceof Player victim && reaperTaskMap.containsKey(victim)) {
            event.setAmount(reducedDamage(victim, event.getAmount()));
        }
        // Soul stacking mechanic
        if (event.getSpell() instanceof Devour || event.getSpell() instanceof UmbralGrasp) {
            if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
            if (event.getSpell() == null) return;
            Player player = event.getPlayer();
            if (!reaperTaskMap.containsKey(player)) {
                BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                        () -> cleanupTask(player), (long) duration * 20L);
                reaperTaskMap.put(player, new StackTask(player, this, new AtomicInteger(1), bukkitTask));
            } else {
                if (reaperTaskMap.get(player).getStacks().get() <= maxStacks) {
                    reaperTaskMap.get(player).getStacks().getAndIncrement();
                }
                reaperTaskMap.get(player).reset((long) duration, () -> cleanupTask(player));
            }
        }
    }

    private void cleanupTask(Player player) {
        reaperTaskMap.remove(player);
        player.sendMessage(ChatColor.GRAY + "Soul Reaper has expired.");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 0);
        setMaxStacks(maxStacks.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

}
