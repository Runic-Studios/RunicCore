package com.runicrealms.plugin.player.stat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatContainer {

    private final Player player;
    private int dexterity;
    private int intelligence;
    private int strength;
    private int vitality;
    private int wisdom;

    public StatContainer(Player player) {
        this.player = player;
    }

    public StatContainer(Player player, int dexterity, int intelligence, int strength, int vitality, int wisdom) {
        this.player = player;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.strength = strength;
        this.vitality = vitality;
        this.wisdom = wisdom;
    }

    public Player getPlayer() {
        return player;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getVitality() {
        return vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public void increaseStat(BaseStatEnum baseStatEnum, int value) {
        switch (baseStatEnum) {
            case DEXTERITY:
                dexterity += value;
                break;
            case INTELLIGENCE:
                intelligence += value;
                break;
            case STRENGTH:
                strength += value;
                break;
            case VITALITY:
                vitality += value;
                break;
            case WISDOM:
                wisdom += value;
                break;
        }
        // call custom event for listeners
        StatChangeEvent statChangeEvent = new StatChangeEvent(this.player, this);
        Bukkit.getPluginManager().callEvent(statChangeEvent);
    }

    public void resetValues() {
        this.dexterity = 0;
        this.intelligence = 0;
        this.strength = 0;
        this.vitality = 0;
        this.wisdom = 0;
        // call custom event for listeners
        StatChangeEvent statChangeEvent = new StatChangeEvent(this.player, this);
        Bukkit.getPluginManager().callEvent(statChangeEvent);
    }
}
