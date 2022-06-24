package com.runicrealms.plugin.model;

/**
 *
 */
public class ProfessionInfo {
    private final String profName;
    private final int profExp;
    private final int profLevel;

    /**
     * @param profName
     * @param profExp
     * @param profLevel
     */
    public ProfessionInfo(String profName, int profExp, int profLevel) {
        this.profName = profName;
        this.profExp = profExp;
        this.profLevel = profLevel;
    }

    public String getProfName() {
        return profName;
    }

    public int getProfExp() {
        return profExp;
    }

    public int getProfLevel() {
        return profLevel;
    }
}
