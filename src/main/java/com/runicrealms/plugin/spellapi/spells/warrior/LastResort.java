package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class LastResort extends Spell {
    private static final int COOLDOWN = 360;
    private static final double PERCENT = .25;
    private static final int RADIUS = 5;
    private final HashSet<UUID> resorters;

    public LastResort() {
        super("Last Resort", CharacterClass.MAGE);
        this.resorters = new HashSet<>();
        this.setIsPassive(true);
        this.setDescription("Upon death, you are resurrected✦ with " +
                (int) (PERCENT * 100) + "% health, blasting back enemies within " +
                RADIUS + " blocks! Last Resort cannot occur " +
                "more than once every " + COOLDOWN + "s.");
    }

    private void beginLastResort(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        // knockback
        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!isValidEnemy(player, entity)) continue;
            Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.25).setY(0.3);
            entity.setVelocity(force);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRunicDeath(RunicDeathEvent event) {
        if (!hasPassive(event.getVictim().getUniqueId(), this.getName())) return;
        if (resorters.contains(event.getVictim().getUniqueId())) return;
        Player player = event.getVictim();
        event.setCancelled(true);
        resorters.add(player.getUniqueId());
        healPlayer(player, player, (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT), this);
        beginLastResort(player);
        player.sendMessage(ChatColor.GREEN + "Your Last Resort has spared you from death!");
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            player.sendMessage(ChatColor.GREEN + "Your Last Resort is now available!");
            resorters.remove(event.getVictim().getUniqueId());
        }, COOLDOWN * 20L);
    }
}

