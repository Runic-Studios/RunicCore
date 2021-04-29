package com.runicrealms.plugin.player.stat;

public enum BaseStatEnum {

    DEXTERITY("Dexterity", "DEX", "✦", "Deal more ranged damage and gain movement speed!"),
    INTELLIGENCE("Intelligence", "INT", "ʔ", "Deal more spell damage and gain more max mana!"),
    STRENGTH("Strength", "STR", "⚔", "Deal more melee weapon damage!"),
    VITALITY("Vitality", "VIT", "■", "Gain damage reduction and health regen!"),
    WISDOM("Wisdom", "WIS", "✸", "Gain more spell healing and mana regen!");

    private static final double RANGED_DMG_MULT = 1.0;
    private static final double MOVEMENT_SPEED_MULT = 1.0;
    private static final double MAGIC_DMG_MULT = 1.0;
    private static final double MANA_MANA_MULT = 1.0;
    private static final double MELEE_DMG_MULT = 1.0;
    private static final double DAMAGE_REDUCTION_MULT = 1.0;
    private static final double HEALTH_REGEN_MULT = 1.0;
    private static final double SPELL_HEALING_MULT = 1.0;
    private static final double MANA_REGEN_MULT = 1.0;
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

    /*
    Multipliers returned as value / 100, so 1.0 returns .01. Better for damage calcualtion.
     */
    public static double getRangedDmgMult() {
        return RANGED_DMG_MULT / 100;
    }

    public static double getMovementSpeedMult() {
        return MOVEMENT_SPEED_MULT / 100;
    }

    public static double getMagicDmgMult() {
        return MAGIC_DMG_MULT / 100;
    }

    public static double getManaManaMult() {
        return MANA_MANA_MULT / 100;
    }

    public static double getMeleeDmgMult() {
        return MELEE_DMG_MULT / 100;
    }

    public static double getDamageReductionMult() {
        return DAMAGE_REDUCTION_MULT / 100;
    }

    public static double getHealthRegenMult() {
        return HEALTH_REGEN_MULT / 100;
    }

    public static double getSpellHealingMult() {
        return SPELL_HEALING_MULT / 100;
    }

    public static double getManaRegenMult() {
        return MANA_REGEN_MULT / 100;
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
