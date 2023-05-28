package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.Map;

/**
 * I guess I can't just override the 'loadSpellData' method, so we're using RadiusSpell logic as a dummy.
 * There's no radius lever. It just gets it from Slam. Instead, we have a velocity multiplier
 *
 * @author Skyfallin
 */
public class SeismicForce extends Spell implements RadiusSpell {
    private double pullMultiplier;

    public SeismicForce() {
        super("Seismic Force", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Your slam spell now sends out a seismic wave, " +
                "pulling all enemies hit towards your location " +
                "instead of knocking them up!");
    }

    @EventHandler
    public void onSpellCast(Slam.SlamLandEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        event.setCancelled(true);
        if (RunicCore.getSpellAPI().isOnCooldown(event.getCaster(), "Seismic Slam")) return;
        Bukkit.broadcastMessage("custom effect");
    }

    @Override
    public void loadRadiusData(Map<String, Object> spellData) {
        Number radius = (Number) spellData.getOrDefault("pull-multiplier", 0);
        setRadius(radius.doubleValue());
    }

    @Override
    public double getRadius() {
        return pullMultiplier;
    }

    @Override
    public void setRadius(double radius) {
        this.pullMultiplier = radius;
    }

}

