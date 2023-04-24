package com.runicrealms.plugin.model;

public enum SkillTreePosition {
    FIRST(1),
    SECOND(2),
    THIRD(3);

    private final int value;

    SkillTreePosition(int value) {
        this.value = value;
    }

    public static SkillTreePosition getFromValue(int value) {
        for (SkillTreePosition skillTreePosition : SkillTreePosition.values()) {
            if (skillTreePosition.getValue() == value)
                return skillTreePosition;
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }
}
