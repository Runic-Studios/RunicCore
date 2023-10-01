package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

public enum RunicStatusEffect {
    DISARM("Disarm", "Enemy cannot use basic attacks!", false, "disarmed!", Sound.ENTITY_ITEM_BREAK),
    INVULNERABILITY("Invulnerability", "You cannot take damage!", true, "invulnerability!", Sound.ITEM_TOTEM_USE),
    ROOT("Root", "Enemy cannot move! Effect broken by damage.", false, "rooted!", Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR),
    SILENCE("Silence", "Enemy cannot cast spells!", false, "silenced!", Sound.ENTITY_CHICKEN_DEATH),
    SLOW_I("Slow", "Enemy is slowed!", false, "slowed!", Sound.BLOCK_SOUL_SAND_BREAK),
    SLOW_II("Slow", "Enemy is slowed!", false, "slowed!", Sound.BLOCK_SOUL_SAND_BREAK),
    SLOW_III("Slow", "Enemy is slowed!", false, "slowed!", Sound.BLOCK_SOUL_SAND_BREAK),
    SPEED_I("Speed", "You gain speed!", true, "speed!", Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    SPEED_II("Speed", "You gain speed!", true, "speed!", Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    SPEED_III("Speed", "You gain speed!", true, "speed!", Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    STUN("Stun", "Enemy cannot cast spells, deal damage, or move!", false, "stunned!", Sound.BLOCK_GLASS_BREAK);

    private final String name;
    private final String description;
    private final boolean buff;
    private final String message;
    private final Sound sound;

    /**
     * @param name        of the effect
     * @param description of the effect
     * @param isBuff      true if the effect is beneficial to the player
     * @param message     that displays in-chat to the player
     * @param sound       of the effect
     */
    RunicStatusEffect(String name, String description, boolean isBuff, String message, Sound sound) {
        this.name = name;
        this.description = description;
        this.buff = isBuff;
        this.message = message;
        this.sound = sound;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        ChatColor chatColor = buff ? ChatColor.DARK_GREEN : ChatColor.DARK_RED;
        String prepend = buff ? "You have gained " : "You have been ";
        return chatColor + String.valueOf(ChatColor.BOLD) + prepend + message;
    }

    public String getName() {
        return name;
    }

    public Sound getSound() {
        return sound;
    }

    public boolean isBuff() {
        return buff;
    }
}
