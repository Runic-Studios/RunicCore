package com.runicrealms.plugin.player.stat;

public enum BaseStatEnum {

    DEXTERITY("Dexterity", "DEX"), // ranged damage / speed
    INTELLIGENCE("Intelligence", "INT"), // spell damage
    STRENGTH("Strength", "STR"), // weapon damage (melee)
    VITALITY("Vitality", "VIT"), // health / defense
    WISDOM("Wisdom", "WIS"); // healing / mana

    private final String name;
    private final String prefix;

    BaseStatEnum(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the enum value of a sub-class string
     * @param name of sub-class
     * @return enum of sub-class
     */
    public static BaseStatEnum getFromName(String name) {
        for (BaseStatEnum baseStatEnum : BaseStatEnum.values()) {
            if (baseStatEnum.getName().equalsIgnoreCase(name)) {
                return baseStatEnum;
            }
        }
        return null;
    }

}
