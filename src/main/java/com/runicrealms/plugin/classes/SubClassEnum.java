package com.runicrealms.plugin.classes;

import org.bukkit.Material;

public enum SubClassEnum {

    // archer
    MARKSMAN("Marksman", ClassEnum.ARCHER, Material.GREEN_GLAZED_TERRACOTTA, "Shoot bows"),
    SCOUT("Scout", ClassEnum.ARCHER, Material.LIME_GLAZED_TERRACOTTA, ""),
    WARDEN("Warden", ClassEnum.ARCHER, Material.CYAN_GLAZED_TERRACOTTA, ""),
    // cleric
    BARD("Bard", ClassEnum.CLERIC, Material.ORANGE_GLAZED_TERRACOTTA, ""),
    EXEMPLAR("Exemplar", ClassEnum.CLERIC, Material.YELLOW_GLAZED_TERRACOTTA, ""),
    PRIEST("Priest", ClassEnum.CLERIC, Material.PINK_GLAZED_TERRACOTTA, ""),
    // mage
    CRYOMANCER("Cryomancer", ClassEnum.MAGE, Material.LIGHT_BLUE_GLAZED_TERRACOTTA, ""),
    PYROMANCER("Pyromancer", ClassEnum.MAGE, Material.RED_GLAZED_TERRACOTTA, ""),
    WARLOCK("Warlock", ClassEnum.MAGE, Material.PURPLE_GLAZED_TERRACOTTA, ""),
    // rogue
    ASSASSIN("Assassin", ClassEnum.ROGUE, Material.BLACK_GLAZED_TERRACOTTA,
            "Assassin kills stuff fast!"),
    DUELIST("Duelist", ClassEnum.ROGUE, Material.BROWN_GLAZED_TERRACOTTA,
            "Duelist focuses on a single enemy!"),
    SWINDLER("Swindler", ClassEnum.ROGUE, Material.BLUE_GLAZED_TERRACOTTA,
            "Swindler uses parlor tricks!"),
    // warior
    BERSERKER("Berserker", ClassEnum.WARRIOR, Material.GRAY_GLAZED_TERRACOTTA, ""),
    GUARDIAN("Guardian", ClassEnum.WARRIOR, Material.WHITE_GLAZED_TERRACOTTA, ""),
    INQUISITOR("Inquisitor", ClassEnum.WARRIOR, Material.LIGHT_GRAY_GLAZED_TERRACOTTA, "");

    private final String name;
    private final ClassEnum baseClass;
    private final Material itemStack;
    private final String description;

    SubClassEnum(String name, ClassEnum baseClass, Material itemStack, String description) {
        this.name = name;
        this.baseClass = baseClass;
        this.itemStack = itemStack;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public ClassEnum getBaseClass() {
        return this.baseClass;
    }

    public Material getMaterial() {
        return this.itemStack;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the enum value of a sub-class string
     * @param name of sub-class
     * @return enum of sub-class
     */
    public static SubClassEnum getFromName(String name) {
        for (SubClassEnum subClassType : SubClassEnum.values()) {
            if (subClassType.getName().equalsIgnoreCase(name)) {
                return subClassType;
            }
        }
        return null;
    }

}
