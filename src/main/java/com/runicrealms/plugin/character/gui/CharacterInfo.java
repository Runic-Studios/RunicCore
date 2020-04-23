package com.runicrealms.plugin.character.gui;

import com.runicrealms.plugin.classes.ClassEnum;

public class CharacterInfo {

    private ClassEnum classType;
    private int exp;
    private int level;

    public CharacterInfo(ClassEnum classType, int exp, int level) {
        this.classType = classType;
        this.exp = exp;
        this.level = level;
    }

    public ClassEnum getClassType() {
        return this.classType;
    }

    public int getExp() {
        return this.exp;
    }

    public int getLevel() {
        return this.level;
    }

}
