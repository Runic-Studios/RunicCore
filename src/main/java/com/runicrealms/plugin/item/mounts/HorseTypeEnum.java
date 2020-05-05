package com.runicrealms.plugin.item.mounts;

import org.bukkit.entity.Horse;

@SuppressWarnings("deprecation")
public enum HorseTypeEnum {

    // normal mounts
    BROWN(Horse.Color.BROWN, Horse.Variant.HORSE, HorseTierEnum.NORMAL, "Brown Steed"),
    CHESTNUT(Horse.Color.CHESTNUT, Horse.Variant.HORSE, HorseTierEnum.NORMAL, "Chestnut Mare"),
    GRAY(Horse.Color.GRAY, Horse.Variant.HORSE, HorseTierEnum.NORMAL, "Gray Stallion"),
    // epic
    BLACK(Horse.Color.BLACK, Horse.Variant.HORSE, HorseTierEnum.EPIC, "Swift Black Steed"),
    WHITE(Horse.Color.WHITE, Horse.Variant.HORSE, HorseTierEnum.EPIC, "Swift White Stallion"),
    // legendary
    FIRE(Horse.Color.CREAMY, Horse.Variant.HORSE, HorseTierEnum.LEGENDARY, "Blazing War Steed"),
    ICE(Horse.Color.DARK_BROWN, Horse.Variant.HORSE, HorseTierEnum.LEGENDARY, "Mirage");
//    SKELETON(Horse.Color.BROWN, Horse.Variant.SKELETON_HORSE, HorseTierEnum.LEGENDARY, "Dead Man's Charger"),
//    UNDEAD(Horse.Color.BROWN, Horse.Variant.UNDEAD_HORSE, HorseTierEnum.LEGENDARY, "Shademane");

    Horse.Color color;
    Horse.Variant variant;
    HorseTierEnum tier;
    String name;

    HorseTypeEnum(Horse.Color color, Horse.Variant variant, HorseTierEnum tier, String name) {
        this.color = color;
        this.variant = variant;
        this.tier = tier;
        this.name = name;
    }

    public Horse.Color getColor() {
        return color;
    }

    public Horse.Variant getVariant() {
        return variant;
    }

    public HorseTierEnum getTier() {
        return tier;
    }

    public String getName() {
        return name;
    }
}
