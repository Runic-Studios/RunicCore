package com.runicrealms.plugin.spellapi.spelltypes;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

public enum RunicStatusEffect {
    DISARM(false, "disarmed!", Sound.ENTITY_ITEM_BREAK),
    INVULNERABILITY(true, "invulnerability!", Sound.ITEM_TOTEM_USE),
    ROOT(false, "rooted!", Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR),
    SILENCE(false, "silenced!", Sound.ENTITY_CHICKEN_DEATH),
    SLOW_I(false, "slowed!", Sound.BLOCK_SOUL_SAND_BREAK),
    SLOW_II(false, "slowed!", Sound.BLOCK_SOUL_SAND_BREAK),
    SLOW_III(false, "slowed!", Sound.BLOCK_SOUL_SAND_BREAK),
    SPEED_I(true, "speed!", Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    SPEED_II(true, "speed!", Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    SPEED_III(true, "speed!", Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    STUN(false, "stunned!", Sound.BLOCK_GLASS_BREAK);

    private final boolean buff;
    private final String message;
    private final Sound sound;

    /**
     * @param isBuff  true if the effect is beneficial to the player
     * @param message that displays in-chat to the player
     * @param sound   of the effect
     */
    RunicStatusEffect(boolean isBuff, String message, Sound sound) {
        this.buff = isBuff;
        this.message = message;
        this.sound = sound;
    }

    public String getMessage() {
        ChatColor chatColor = buff ? ChatColor.DARK_GREEN : ChatColor.DARK_RED;
        String prepend = buff ? "You have gained " : "You have been ";
        return chatColor + "" + ChatColor.BOLD + prepend + message;
    }

    public Sound getSound() {
        return sound;
    }

    public boolean isBuff() {
        return buff;
    }
}
