package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Fade extends Spell {
    private static final int DURATION = 6;
    private static final int PERIOD = 1; // seconds

    public Fade() {
        super("Fade",
                "You begin to flicker in and out of invisibility " +
                        "for the next " + DURATION + "s! While invisible, " +
                        "you are immune to damage, but cannot cast spells.",
                ChatColor.WHITE, CharacterClass.ARCHER, 18, 30);
    }

    private void disappear(Player player) {
        addStatusEffect(player, RunicStatusEffect.INVULNERABILITY, 1, false);
        addStatusEffect(player, RunicStatusEffect.SILENCE, 1, false);
        // poof!
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));

        PacketPlayOutPlayerInfo packet =
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                        ((CraftPlayer) player).getHandle());

        // hide the player, prevent them from disappearing in tab
        for (UUID uuid : RunicCore.getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.hidePlayer(plugin, player);
            ((CraftPlayer) loaded).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count >= DURATION)
                    this.cancel();
                if (count % 2 == 0) // count is even
                    reappear(player);
                else
                    disappear(player);
                count += PERIOD;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, PERIOD * 20L);

    }

    private void reappear(Player player) {
        for (UUID uuid : RunicCore.getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.showPlayer(plugin, player);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));
    }
}

