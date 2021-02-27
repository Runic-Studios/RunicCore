package com.runicrealms.plugin.player.stat;

public enum BaseStatEnum {

    DEXTERITY("Dexterity", "DEX", "Ranged damage movement speed"),
    INTELLIGENCE("Intelligence", "INT", "Magic damage and max mana"),
    STRENGTH("Strength", "STR", "Melee damage"),
    VITALITY("Vitality", "VIT", "Defense and health regen"), // defense = damage reduction todo: write simple event
    WISDOM("Wisdom", "WIS", "Spell Healing and mana regen");

    private final String name;
    private final String prefix;
    private final String description;

    BaseStatEnum(String name, String prefix, String description) {
        this.name = name;
        this.prefix = prefix;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDescription() {
        return description;
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
