package com.runicrealms.plugin.spellapi.effect;

import com.runicrealms.plugin.RunicCore;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;

public class StackHologram {
    private final SpellEffectType spellEffectType;
    private final Location initialLocation;
    private final Hologram hologram;
    private final Set<Player> playersToShowTo;

    public StackHologram(
            SpellEffectType spellEffectType,
            Location initialLocation,
            Set<Player> playersToShowTo) {
        this.spellEffectType = spellEffectType;
        this.initialLocation = initialLocation;
        this.playersToShowTo = playersToShowTo;
        this.hologram = createHologram();
    }

    private Hologram createHologram() {
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(initialLocation.add(0, 1.5f, 0));
        hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        playersToShowTo.forEach(player -> hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE));
        return hologram;
    }

    public void showHologram(Location location, int stacks) {
        hologram.getLines().clear();
        hologram.getLines().appendText(spellEffectType.getChatColor() + spellEffectType.getDisplay() + spellEffectType.getSymbol() + ChatColor.WHITE + " x" + stacks);
        hologram.setPosition(location);
    }

    public SpellEffectType getSpellEffectType() {
        return spellEffectType;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Set<Player> getPlayersToShowTo() {
        return playersToShowTo;
    }
}
