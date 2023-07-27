package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.runicitems.Stat;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Warsong extends Spell implements AttributeSpell, DurationSpell, RadiusSpell {
    private final ConcurrentHashMap<UUID, Set<UUID>> buffedPlayersMap = new ConcurrentHashMap<>();
    private double duration;
    private double radius;
    private double multiplier;
    private double baseValue;
    private String statName;

    public Warsong() {
        super("Warsong", CharacterClass.CLERIC);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("You sing a song of battle, granting a buff " +
                "to all allies within " + radius + " blocks! " +
                "For " + duration + "s, the buff increases the " +
                "basic attack damage of you and your allies " +
                "by (" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7)!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2.0F);
        startParticleTask(player);

        // Buff all players within 10 blocks
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            if (player.getLocation().distanceSquared(entity.getLocation()) > radius * radius)
                continue;
            if (!buffedPlayersMap.containsKey(player.getUniqueId())) {
                buffedPlayersMap.put(player.getUniqueId(), new HashSet<>());
            }
            buffedPlayersMap.get(player.getUniqueId()).add(entity.getUniqueId());
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> buffedPlayersMap.remove(player.getUniqueId()), (int) duration * 20L);

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
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (buffedPlayersMap == null) return;

        // Get the attacker
        Player player = event.getPlayer();
        UUID bardUuid = null;
        boolean playerIsBuffed = false;
        for (UUID uuid : buffedPlayersMap.keySet()) {
            if (buffedPlayersMap.get(uuid).contains(player.getUniqueId())) {
                bardUuid = uuid;
                playerIsBuffed = true;
            }
        }
        if (!playerIsBuffed) return;

        int bonus = (int) (multiplier * RunicCore.getStatAPI().getPlayerIntelligence(bardUuid));
        event.setAmount((int) (event.getAmount() + baseValue + Math.max(0, bonus))); // Bonus cannot be negative
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 1.0F);
        event.getVictim().getWorld().spawnParticle
                (Particle.NOTE, event.getVictim().getLocation().add(0, 1.5, 0),
                        5, 1.0F, 0, 0, 0); // 0.3F
    }

    private void startParticleTask(Player player) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration) {
                    this.cancel();
                    return;
                }
                count++;
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
                player.getWorld().spawnParticle
                        (Particle.NOTE, player.getEyeLocation(), 15, 0.75F, 0.75F, 0.75F, 0);
            }
        }.runTaskTimer(plugin, 0, 20L);
    }
}

