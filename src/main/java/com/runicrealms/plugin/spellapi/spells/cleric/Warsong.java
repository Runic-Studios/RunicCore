package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class Warsong extends Spell {
    private static final int DURATION = 6;
    private static final int RADIUS = 6;
    private static final double PERCENT = .50;
    private final ConcurrentHashMap<UUID, Set<UUID>> buffedPlayersMap = new ConcurrentHashMap<>();

    public Warsong() {
        super("Warsong",
                "You sing a song of battle, granting a buff " +
                        "to all allies within " + RADIUS + " blocks! " +
                        "For " + DURATION + "s, the buff increases the " +
                        "basic attack damage of you and your allies " +
                        "by " + (int) (PERCENT * 100) + "% of your &eIntelligenceʔ&7!",
                ChatColor.WHITE, CharacterClass.CLERIC, 10, 15);
    }

    public static double getPERCENT() {
        return PERCENT;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 2.0F);
        startParticleTask(player);

        // Buff all players within 10 blocks
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidAlly(player, target))) {
            if (player.getLocation().distanceSquared(entity.getLocation()) > RADIUS * RADIUS) continue;
            if (!buffedPlayersMap.containsKey(player.getUniqueId())) {
                buffedPlayersMap.put(player.getUniqueId(), new HashSet<>());
            }
            buffedPlayersMap.get(player.getUniqueId()).add(entity.getUniqueId());
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> buffedPlayersMap.remove(player.getUniqueId()), DURATION * 20L);

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

        int bonus = (int) (PERCENT * RunicCore.getStatAPI().getPlayerIntelligence(bardUuid));
        if (bonus < 1) bonus = 1;
        event.setAmount(event.getAmount() + bonus);
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
                if (count > DURATION) {
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

