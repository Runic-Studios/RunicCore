package com.runicrealms.plugin.classes;

public enum SubClassEnum {

    // archer
    MARKSMAN("Marksman", ClassEnum.ARCHER),
    SCOUT("Scout", ClassEnum.ARCHER),
    WARDEN("Warden", ClassEnum.ARCHER),
    // cleric
    BARD("Bard", ClassEnum.CLERIC),
    EXEMPLAR("Exemplar", ClassEnum.CLERIC),
    PRIEST("Priest", ClassEnum.CLERIC),
    // mage
    CRYOMANCER("Cryomancer", ClassEnum.MAGE),
    PYROMANCER("Pyromancer", ClassEnum.MAGE),
    WARLOCK("Warlock", ClassEnum.MAGE),
    // rogue
    ASSASSIN("Assassin", ClassEnum.ROGUE),
    DUELIST("Duelist", ClassEnum.ROGUE),
    SWINDLER("Swindler", ClassEnum.ROGUE),
    // warior
    BERSERKER("Berserker", ClassEnum.WARRIOR),
    GUARDIAN("Guardian", ClassEnum.WARRIOR),
    INQUISITOR("Inquisitor", ClassEnum.WARRIOR);

    private final String name;
    private final ClassEnum baseClass;

    SubClassEnum(String name, ClassEnum baseClass) {
        this.name = name;
        this.baseClass = baseClass;
    }

    public String getName() {
        return this.name;
    }
    public ClassEnum getBaseClass() {
        return this.baseClass;
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
