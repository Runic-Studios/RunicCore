package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
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

@SuppressWarnings("FieldCanBeLocal")
public class Improvisation extends Spell {

    private static final int DURATION = 2;
    private static final int PERCENT = 10;
    private static final int RADIUS = 10;
    private static final double PERCENT_DAMAGE = .45;
    private final HashSet<UUID> buffedPlayers;

    public Improvisation() {
        super("Improvisation",
                "Your melee weapon⚔ attacks have a " + PERCENT + "% chance " +
                        "to grant yourself and nearby allies within " + RADIUS + " " +
                        "blocks a " + (int) (PERCENT_DAMAGE * 100) + "% weapon⚔ damage buff " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
        buffedPlayers = new HashSet<>();
    }

    @EventHandler
    public void onWeaponHit(WeaponDamageEvent e) {
        Player player = e.getPlayer();
        if (hasPassive(player.getUniqueId(), this.getName()) && e.isBasicAttack())
            attemptToBuffAllies(player);
        if (buffedPlayers.contains(player.getUniqueId())) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 1.0F);
            e.getVictim().getWorld().spawnParticle
                    (Particle.NOTE, e.getVictim().getLocation().add(0, 1.5, 0),
                            5, 1.0F, 0, 0, 0);
            e.setAmount((int) (e.getAmount() + (e.getAmount() * PERCENT_DAMAGE)));
        }
    }

    private void attemptToBuffAllies(Player pl) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        buffPlayer(pl);
        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(verifyAlly(pl, en))) continue;
            buffPlayer((Player) en);
        }
    }

    private void buffPlayer(Player pl) {
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5F, 0.5F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.5F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.3F);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.1F);
        pl.getWorld().spawnParticle
                (Particle.NOTE, pl.getEyeLocation(), 15, 0.75F, 0.75F, 0.75F, 0);
        buffedPlayers.add(pl.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> buffedPlayers.remove(pl.getUniqueId()), DURATION * 20L);
    }
}

