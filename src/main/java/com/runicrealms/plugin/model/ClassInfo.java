package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.ClassEnum;

public class ClassInfo {

    private final ClassEnum classType;
    private final int exp;
    private final int level;

    public ClassInfo(ClassEnum classType, int exp, int level) {
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
