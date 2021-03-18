package com.runicrealms.plugin.classes;

public enum ClassEnum {

    ARCHER("Archer"),
    CLERIC("Cleric"),
    MAGE("Mage"),
    ROGUE("Rogue"),
    WARRIOR("Warrior");

    private final String name;

    ClassEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Returns the enum value of a class string
     * @param name of class
     * @return enum of class
     */
    public static ClassEnum getFromName(String name) {
        for (ClassEnum classType : ClassEnum.values()) {
            if (classType.getName().equalsIgnoreCase(name)) {
                return classType;
            }
        }
        return null;
    }

}
