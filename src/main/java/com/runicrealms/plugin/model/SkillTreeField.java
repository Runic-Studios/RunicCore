package com.runicrealms.plugin.model;

public enum SkillTreeField {

    SPENT_POINTS("spentPoints");

    private final String field;

    SkillTreeField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    /**
     * Returns the corresponding SpellField from the given string version
     *
     * @param field a string matching a constant
     * @return the constant
     */
    public static SkillTreeField getFromFieldString(String field) {
        for (SkillTreeField skillTreeField : SkillTreeField.values()) {
            if (skillTreeField.getField().equalsIgnoreCase(field))
                return skillTreeField;
        }
        return null;
    }
}
