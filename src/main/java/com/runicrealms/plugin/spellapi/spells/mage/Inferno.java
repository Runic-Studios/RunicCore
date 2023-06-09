package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.StackTask;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Inferno extends Spell implements DurationSpell {
    private final Map<UUID, StackTask> infernoMap = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> buffedPlayers = new ConcurrentHashMap<>();
    private double buffDuration;
    private int period;
    private double percent;
    private int damageCapPerTick;
    private double duration;
    private double durationFalloff;
    private double stacksRequired;

    public Inferno() {
        super("Inferno", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Casting a fire spell creates a stack of &7&oinferno&7. " +
                "After gaining " + stacksRequired + " stacks, you become infernal for " + buffDuration + "s!" +
                "While infernal, your fire spells burn targets for " + (percent * 100) +
                "% max health every second for " + duration + "s! " +
                "Targets receive " + (percent * 100) + "% less healing while under the effect of inferno. " +
                "Each tick is capped at " + damageCapPerTick + " total damage against monsters. " +
                "Stacks expire after " + durationFalloff + "s. Targets cannot be affected more than once.");
    }

    public void setStacksRequired(double stacksRequired) {
        this.stacksRequired = stacksRequired;
    }

    public void setDurationFalloff(double durationFalloff) {
        this.durationFalloff = durationFalloff;
    }

    public void setBuffDuration(double buffDuration) {
        this.buffDuration = buffDuration;
    }

    private void triggerInferno(Player player) {
        buffedPlayers.put(player.getUniqueId(), new HashSet<>());
        Bukkit.getScheduler().runTaskLater(plugin, () -> buffedPlayers.remove(player.getUniqueId()), (long) buffDuration * 20L);
        Cone.coneEffect(player, Particle.FLAME, buffDuration, 0, 20, Color.RED);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2.0f);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (buffedPlayers.isEmpty()) return;
        if (!buffedPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        if (buffedPlayers.get(event.getPlayer().getUniqueId()).contains(event.getVictim().getUniqueId())) return;
        // Only fire spells trigger deal damage
        if (!(event.getSpell() instanceof Fireball
                || event.getSpell() instanceof DragonsBreath
                || event.getSpell() instanceof FireBlast
                || event.getSpell() instanceof MeteorShower)) return;
        buffedPlayers.get(event.getPlayer().getUniqueId()).add(event.getVictim().getUniqueId());
        Player player = event.getPlayer();
        LivingEntity victim = event.getVictim();
        boolean isCapped = !(victim instanceof Player);
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += 1;
                    player.getWorld().spawnParticle(Particle.LAVA, victim.getEyeLocation(), 10, 0.25f, 0, 0.25f);
                    int damage = (int) ((percent * victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) / period);
                    if (isCapped && damage > damageCapPerTick)
                        damage = damageCapPerTick;
                    DamageUtil.damageEntitySpell(damage, victim, player, false);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);

    }

    public void setDamageCapPerTick(int damageCapPerTick) {
        this.damageCapPerTick = damageCapPerTick;
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
    public void loadDurationData(Map<String, Object> spellData) {
        Number buffDuration = (Number) spellData.getOrDefault("buff-duration", 0);
        setBuffDuration(buffDuration.doubleValue());
        Number damageCapPerTick = (Number) spellData.getOrDefault("damage-cap-per-tick", 0);
        setDamageCapPerTick((int) damageCapPerTick.doubleValue());
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number durationFalloff = (Number) spellData.getOrDefault("duration-falloff", 0);
        setDurationFalloff(durationFalloff.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue() / 100);
        Number period = (Number) spellData.getOrDefault("period", 0);
        setPeriod(period.intValue());
        Number stacksRequired = (Number) spellData.getOrDefault("stacks-required", 0);
        setStacksRequired(stacksRequired.intValue());
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    @EventHandler
    public void onMagicDamage(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        // Stack inferno
        if (!(event.getSpell() instanceof Fireball
                || event.getSpell() instanceof DragonsBreath
                || event.getSpell() instanceof FireBlast
                || event.getSpell() instanceof MeteorShower)) return;
        // Trigger inferno
        if (infernoMap.containsKey(event.getCaster().getUniqueId())
                && infernoMap.get(event.getCaster().getUniqueId()).getStacks().get() == stacksRequired) {
            infernoMap.get(event.getCaster().getUniqueId()).getBukkitTask().cancel();
            infernoMap.remove(event.getCaster().getUniqueId());
            triggerInferno(event.getCaster());
            return;
        }
        Player caster = event.getCaster();
        if (!infernoMap.containsKey(caster.getUniqueId())) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> cleanupTask(caster), (long) durationFalloff * 20L);
            infernoMap.put(caster.getUniqueId(), new StackTask(caster, this, new AtomicInteger(1), bukkitTask));
        } else if (infernoMap.get(caster.getUniqueId()).getStacks().get() < stacksRequired) {
            infernoMap.get(caster.getUniqueId()).getStacks().getAndIncrement();
            infernoMap.get(caster.getUniqueId()).reset((long) durationFalloff, () -> cleanupTask(caster));
        }
        // Send message feedback
        caster.sendMessage(ChatColor.GRAY + "Inferno stacks: " + ChatColor.YELLOW + infernoMap.get(caster.getUniqueId()).getStacks().get());
    }

    /**
     * @param player whose charges have expired
     */
    private void cleanupTask(Player player) {
        infernoMap.remove(player.getUniqueId());
        player.setGlowing(false);
        player.sendMessage(ChatColor.GRAY + "Inferno has expired.");
    }

}

