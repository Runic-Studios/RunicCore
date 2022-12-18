package com.runicrealms.plugin.classes;

public enum CharacterClass {

    ANY("any"),
    ARCHER("Archer"),
    CLERIC("Cleric"),
    MAGE("Mage"),
    ROGUE("Rogue"),
    WARRIOR("Warrior");

    private final String name;

    CharacterClass(String name) {
        this.name = name;
    }

    /**
     * Returns the enum value of a class string
     *
     * @param name of class
     * @return enum of class
     */
    public static CharacterClass getFromName(String name) {
        for (CharacterClass classType : CharacterClass.values()) {
            if (classType.getName().equalsIgnoreCase(name)) {
                return classType;
            }
        }
        return null;
    }

    /**
     * Grab an array of the class names to iterate over (skips the 'any' type)
     *
     * @return an array of Strings containing the LOWERCASE class names
     */
    public static String[] getClassNames() {
        String[] classNames = new String[CharacterClass.values().length - 1]; // exclude 'any'
        int i = 0;
        for (CharacterClass characterClass : CharacterClass.values()) {
            if (characterClass == ANY) continue;
            classNames[i] = characterClass.getName().toLowerCase();
            i++;
        }
        return classNames;
    }

    public String getName() {
        return this.name;
    }

}
