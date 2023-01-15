package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class Improvisation extends Spell {

    private static final int DURATION = 2;
    private static final int PERCENT = 10;
    private static final int RADIUS = 10;
    private static final double PERCENT_DAMAGE = .45;
    private final HashSet<UUID> buffedPlayers;

    public Improvisation() {
        super("Improvisation",
                "Your basic attacks have a " + PERCENT + "% chance " +
                        "to grant yourself and nearby allies within " + RADIUS + " " +
                        "blocks a " + (int) (PERCENT_DAMAGE * 100) + "% physical⚔ damage buff " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
        buffedPlayers = new HashSet<>();
    }

    private void attemptToBuffAllies(Player player) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        buffPlayer(player);
        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(isValidAlly(player, entity))) continue;
            buffPlayer((Player) entity);
        }
    }

    private void buffPlayer(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5F, 0.5F);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.5F);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.3F);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.1F);
        player.getWorld().spawnParticle
                (Particle.NOTE, player.getEyeLocation(), 15, 0.75F, 0.75F, 0.75F, 0);
        buffedPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> buffedPlayers.remove(player.getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onWeaponHit(PhysicalDamageEvent event) {
        Player player = event.getPlayer();
        if (hasPassive(player.getUniqueId(), this.getName()) && event.isBasicAttack())
            attemptToBuffAllies(player);
        if (buffedPlayers.contains(player.getUniqueId())) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 1.0F);
            event.getVictim().getWorld().spawnParticle
                    (Particle.NOTE, event.getVictim().getLocation().add(0, 1.5, 0),
                            5, 1.0F, 0, 0, 0);
            event.setAmount((int) (event.getAmount() + (event.getAmount() * PERCENT_DAMAGE)));
        }
    }
}

