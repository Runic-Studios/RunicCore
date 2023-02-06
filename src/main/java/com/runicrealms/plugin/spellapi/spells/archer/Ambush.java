package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
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

public class Ambush extends Spell {

    private static final int BLIND_DURATION = 2;
    private static final int COOLDOWN = 6;
    private static final int DAMAGE = 10;
    private static final int SPEED_DURATION = 3;
    private static final int WARMUP = 2;
    private static final double DAMAGE_PER_LEVEL = 1.0;
    private static final String AMBUSH_ARROW_KEY = "ambush";
    private final Set<UUID> ambushPlayers = new HashSet<>();
    private final Set<UUID> cooldownPlayers = new HashSet<>();
    private final Set<UUID> successfulPlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> sneakMap = new HashMap<>();

    public Ambush() {
        super("Ambush",
                "Sneaking without casting spells for at least " + WARMUP +
                        "s causes your next ranged basic attack (if it lands) to ambush its target, " +
                        "dealing an additional (" +
                        DAMAGE + " + &f" + (int) DAMAGE_PER_LEVEL +
                        "x &7lvl) physical⚔ damage, blinding your opponent for " + BLIND_DURATION + "s, " +
                        "and granting you a a boost of speed for " +
                        SPEED_DURATION + "s! Cannot occur more than once every " + COOLDOWN + "s.",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
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
        event.setAmount((int) (event.getAmount() + DAMAGE + (DAMAGE_PER_LEVEL * event.getPlayer().getLevel())));
        event.getVictim().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BLIND_DURATION * 20, 1));
        addStatusEffect(event.getPlayer(), RunicStatusEffect.SPEED_II, SPEED_DURATION, false);
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> cooldownPlayers.remove(event.getPlayer().getUniqueId()), COOLDOWN * 20L);
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
            }, WARMUP * 20L);
            sneakMap.put(event.getPlayer().getUniqueId(), bukkitTask);
        } else {
            if (sneakMap.containsKey(event.getPlayer().getUniqueId()))
                sneakMap.get(event.getPlayer().getUniqueId()).cancel();
        }
    }
}

