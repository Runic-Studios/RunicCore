package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Unseen extends Spell implements DurationSpell {
    private final Set<UUID> cloakers;
    private double duration;

    public Unseen() {
        super("Unseen", CharacterClass.ROGUE);
        cloakers = new HashSet<>();
        this.setDescription("For " + duration + "s, you vanish completely, " +
                "causing you to appear invisible to " +
                "players. During this time, you are " +
                "immune to damage from monsters!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Poof!
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));

        // hide the player, prevent them from disappearing in tab
        for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.hidePlayer(plugin, player);
        }

        cloakers.add(player.getUniqueId());
        player.sendMessage(ChatColor.GRAY + "You vanished!");

        // Reappear after duration or upon dealing damage. Can't be tracked async :(
        AtomicInteger count = new AtomicInteger(0);
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() >= duration) {
                task.cancel();
                cloakers.remove(player.getUniqueId());
                for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                    Player loaded = Bukkit.getPlayer(uuid);
                    if (loaded == null) continue;
                    loaded.showPlayer(plugin, player);
                }
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
                player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                        new Particle.DustOptions(Color.BLACK, 1));
                player.sendMessage(ChatColor.GRAY + "You reappeared!");
            } else {
                count.set(count.get() + 1);
            }
        }, 0, 20);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Player is immune to mob attacks
     */
    @EventHandler
    public void onDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player player))
            return;
        if (cloakers.contains(player.getUniqueId()))
            event.setCancelled(true);
    }
}
