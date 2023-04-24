package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Ambush extends Spell implements DurationSpell, WarmupSpell {
    private static final String AMBUSH_ARROW_KEY = "ambush";
    private final Set<UUID> ambushPlayers = new HashSet<>();
    private final Set<UUID> cooldownPlayers = new HashSet<>();
    private final Set<UUID> successfulPlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> sneakMap = new HashMap<>();
    private double blindDuration;
    private double cooldown;
    private double damage;
    private double damagePerLevel;
    private double speedDuration;
    private double warmup;

    public Ambush() {
        super("Ambush", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("Sneaking without casting spells for at least " + warmup +
                "s causes your next ranged basic attack (if it lands) to ambush its target, " +
                "dealing an additional (" +
                damage + " + &f" + (int) damagePerLevel +
                "x &7lvl) physical⚔ damage, blinding your opponent for " + blindDuration + "s, " +
                "and granting you a a boost of speed for " +
                speedDuration + "s! Cannot occur more than once every " + cooldown + "s.");
    }

    @Override
    public double getDuration() {
        return blindDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.blindDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number blindDuration = (Number) spellData.getOrDefault("blind-duration", 0);
        setDuration(blindDuration.doubleValue());
        Number speedDuration = (Number) spellData.getOrDefault("speed-duration", 0);
        setSpeedDuration(speedDuration.doubleValue());
        Number cooldown = (Number) spellData.getOrDefault("cooldown", 0);
        setCooldown(cooldown.doubleValue());
        Number damage = (Number) spellData.getOrDefault("physical-damage", 0);
        setDamage(damage.doubleValue());
        Number damagePerLevel = (Number) spellData.getOrDefault("physical-damage-per-level", 0);
        setDamagePerLevel(damagePerLevel.doubleValue());
    }

    @Override
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }

    @EventHandler(priority = EventPriority.LOW) // early
    public void onCustomArrowHit(EntityDamageByEntityEvent event) {
        if (!event.getDamager().hasMetadata(AMBUSH_ARROW_KEY)) return;
        UUID uuid = UUID.fromString(event.getDamager().getMetadata(AMBUSH_ARROW_KEY).get(0).asString());
        successfulPlayers.add(uuid);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!successfulPlayers.contains(event.getPlayer().getUniqueId())) return;
        successfulPlayers.remove(event.getPlayer().getUniqueId());
        event.setAmount((int) (event.getAmount() + damage + (damagePerLevel * event.getPlayer().getLevel())));
        event.getVictim().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindDuration * 20), 1));
        addStatusEffect(event.getPlayer(), RunicStatusEffect.SPEED_II, speedDuration, false);
    }

    @EventHandler(priority = EventPriority.LOW) // early
    public void onRunicBow(RunicBowEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!ambushPlayers.contains(event.getPlayer().getUniqueId())) return;
        ambushPlayers.remove(event.getPlayer().getUniqueId());
        event.getArrow().setMetadata(AMBUSH_ARROW_KEY, new FixedMetadataValue(plugin, event.getPlayer().getUniqueId()));
        EntityTrail.entityTrail(event.getArrow(), Particle.SMOKE_NORMAL);
        cooldownPlayers.add(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> cooldownPlayers.remove(event.getPlayer().getUniqueId()), (long) cooldown * 20L);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (!sneakMap.containsKey(event.getCaster().getUniqueId())) return;
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (ambushPlayers.contains(event.getCaster().getUniqueId())) return;
        sneakMap.get(event.getCaster().getUniqueId()).cancel();
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (ambushPlayers.contains(event.getPlayer().getUniqueId())) return;
        if (cooldownPlayers.contains(event.getPlayer().getUniqueId())) return;
        if (event.isSneaking()) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                ambushPlayers.add(event.getPlayer().getUniqueId());
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                event.getPlayer().sendMessage(ChatColor.GREEN + "Your ambush attack is primed!");
                sneakMap.remove(event.getPlayer().getUniqueId());
            }, (long) warmup * 20L);
            sneakMap.put(event.getPlayer().getUniqueId(), bukkitTask);
        } else {
            if (sneakMap.containsKey(event.getPlayer().getUniqueId()))
                sneakMap.get(event.getPlayer().getUniqueId()).cancel();
        }
    }

    public void setBlindDuration(double blindDuration) {
        this.blindDuration = blindDuration;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setDamagePerLevel(double damagePerLevel) {
        this.damagePerLevel = damagePerLevel;
    }

    public void setSpeedDuration(double speedDuration) {
        this.speedDuration = speedDuration;
    }
}

