package com.runicrealms.plugin.spellapi.spells.archer;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import java.util.Collections;
import java.util.UUID;

public class Fade extends Spell implements DurationSpell {
    private static final int PERIOD = 1; // seconds
    private double duration;

    public Fade() {
        super("Fade", CharacterClass.ARCHER);
        this.setDescription("You begin to flicker in and out of invisibility " +
                "for the next " + duration + "s! While invisible, " +
                "you cannot cast spells.");
    }

    private void disappear(Player player) {
        addStatusEffect(player, RunicStatusEffect.SILENCE, 1, false);
        // poof!
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        packet.getPlayerInfoDataLists().write(1, Collections.singletonList(
                new PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        0,
                        EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode()),
                        WrappedChatComponent.fromText(player.getDisplayName())
                )
        ));

        // hide the player, prevent them from disappearing in tab
        for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.hidePlayer(plugin, player);
            ProtocolLibrary.getProtocolManager().sendServerPacket(loaded, packet);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count >= duration)
                    this.cancel();
                if (count % 2 == 0) // count is even
                    reappear(player);
                else
                    disappear(player);
                count += PERIOD;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, PERIOD * 20L);

    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    private void reappear(Player player) {
        for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
            Player loaded = Bukkit.getPlayer(uuid);
            if (loaded == null) continue;
            loaded.showPlayer(plugin, player);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));
    }
}

