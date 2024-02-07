package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.ChatColor;

public enum SpellEffectType {
    ARCANUM(ChatColor.LIGHT_PURPLE, "Arcanum", ""),
    ARIA_OF_ARMOR(ChatColor.WHITE, "Aria of Armor", ""),
    BALLAD_OF_BINDING(ChatColor.YELLOW, "Ballad of Binding", ""),
    BETRAYED(ChatColor.RED, "Betrayed", ""),
    BLEED(ChatColor.DARK_RED, "Bleed", "☠"),
    BLESSED_BLADE(ChatColor.GREEN, "Blessed Blade", "⚔"),
    CHARGED(ChatColor.BLUE, "Charged", "➹"),
    CHILLED(ChatColor.AQUA, "Chilled", "❈"),
    HOLY_FERVOR(ChatColor.GOLD, "Holy Fervor", ""),
    ICE_BARRIER(ChatColor.WHITE, "Ice Barrier", "✲"),
    IGNITED(ChatColor.DARK_RED, "Ignited", ""),
    INCENDIARY(ChatColor.DARK_RED, "Incendiary", ""),
    RADIANT_FIRE(ChatColor.YELLOW, "Radiant Fire", "☀"),
    SHROUDED(ChatColor.DARK_AQUA, "Shrouded", ""),
    SONG_OF_WAR(ChatColor.RED, "Song of War", ""),
    STATIC(ChatColor.GRAY, "Static", ""),
    SUNDERED(ChatColor.STRIKETHROUGH, "Sundered", "■");

    private final ChatColor chatColor;
    private final String display;
    private final String symbol;

    /**
     * Enum values for secondary spell effects
     *
     * @param display used for stack effect holograms
     * @param symbol  used for stack effect holograms
     */
    SpellEffectType(ChatColor chatColor, String display, String symbol) {
        this.chatColor = chatColor;
        this.display = display;
        this.symbol = symbol;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getDisplay() {
        return display;
    }

    public String getSymbol() {
        return symbol;
    }
}
