package com.runicrealms.plugin.classes;

public enum ClassEnum {

    ANY("any"),
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
     *
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

    /**
     * Grab an array of the classnames to iterate over (skips the 'any' type)
     *
     * @return an array of Strings containing the LOWERCASE class names
     */
    public static String[] getClassNames() {
        String[] classNames = new String[ClassEnum.values().length - 1]; // exclude 'any'
        int i = 0;
        for (ClassEnum classEnum : ClassEnum.values()) {
            if (classEnum == ANY) continue;
            classNames[i] = classEnum.getName().toLowerCase();
            i++;
        }
        return classNames;
    }

}
