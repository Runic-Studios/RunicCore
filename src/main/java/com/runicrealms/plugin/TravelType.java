package com.runicrealms.plugin;

import org.bukkit.Material;
import org.bukkit.Sound;

public enum TravelType {
    BOAT("boat", "Captain", Material.OAK_BOAT, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED),
    WAGON("wagon", "Wagonmaster", Material.SADDLE, Sound.ENTITY_HORSE_GALLOP);

    private final String identifier;
    private final String npcName;
    private final Material material;
    private final Sound sound;

    TravelType(String identifier, String npcName, Material material, Sound sound) {
        this.identifier = identifier;
        this.npcName = npcName;
        this.material = material;
        this.sound = sound;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getNpcName() {
        return npcName;
    }

    public Material getMaterial() {
        return material;
    }

    public Sound getSound() {
        return sound;
    }

    public static TravelType getFromIdentifier(String identifier) {
        for (TravelType travelType : TravelType.values()) {
            if (travelType.getIdentifier().equalsIgnoreCase(identifier))
                return travelType;
        }
        return null;
    }
}