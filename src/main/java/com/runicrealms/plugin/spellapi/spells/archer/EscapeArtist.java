package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class EscapeArtist extends Spell {

    private static final int COOLDOWN = 24;
    private final HashSet<UUID> escapeArtists;

    public EscapeArtist() {
        super ("Escape Artist",
                "Upon being rooted, silenced, or stunned, " +
                        "you masterfully escape the effects, " +
                        "removing all roots, silences, and stuns. " +
                        "Cannot occur more than once every " + COOLDOWN + "s.",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
        escapeArtists = new HashSet<>();
        startEscapeArtistTask();
    }

    private void startEscapeArtistTask() {
        String passiveName = this.getName();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player loaded : RunicCore.getCacheManager().getLoadedPlayers()) {
                    if (!hasPassive(loaded, passiveName)) continue;
                    if (escapeArtists.contains(loaded.getUniqueId())) continue; // on cooldown
                    // listen for effects, remove
                    if (RunicCoreAPI.isRooted(loaded)
                            || RunicCoreAPI.isSilenced(loaded)
                            || RunicCoreAPI.isStunned(loaded)) {
                        loaded.getWorld().playSound(loaded.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                        Cone.coneEffect(loaded, Particle.VILLAGER_HAPPY, 1, 0, 20L, Color.WHITE);
                        escapeArtists.add(loaded.getUniqueId());
                        RunicCore.getSpellManager().getRootedEntites().remove(loaded.getUniqueId());
                        RunicCore.getSpellManager().getRootedEntites().remove(loaded.getUniqueId());
                        RunicCore.getSpellManager().getRootedEntites().remove(loaded.getUniqueId());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> escapeArtists.remove(loaded.getUniqueId()), COOLDOWN * 20L);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 5L);
    }
}

