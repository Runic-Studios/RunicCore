package com.runicrealms.plugin.player.stat;

public enum BaseStatEnum {

    DEXTERITY("Dexterity", "DEX", "✦", "Deal more ranged damage and gain movement speed!"),
    INTELLIGENCE("Intelligence", "INT", "ʔ", "Deal more spell damage and gain more max mana!"),
    STRENGTH("Strength", "STR", "⚔", "Deal more melee weapon damage!"),
    VITALITY("Vitality", "VIT", "■", "Gain damage reduction and health regen!"), // defense = damage reduction todo: write simple event
    WISDOM("Wisdom", "WIS", "✸", "Gain more spell healing and mana regen!");

    private final String name;
    private final String prefix;
    private final String icon;
    private final String description;

    BaseStatEnum(String name, String prefix, String icon, String description) {
        this.name = name;
        this.prefix = prefix;
        this.icon = icon;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the enum value of a stat from its string
     * @param name of stat (not case sensitive)
     * @return enum of stat
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
