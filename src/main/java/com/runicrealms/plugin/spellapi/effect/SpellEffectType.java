package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.ChatColor;

public enum SpellEffectType {
    BLEED(ChatColor.DARK_RED, "Bleed", "☠"),
    BLESSED_BLADE(ChatColor.GREEN, "Blessed Blade", "⚔"),
    CHARGED(ChatColor.BLUE, "Charged", "➹"),
    CHILLED(ChatColor.AQUA, "Chilled", "❈"),
    HOLY_FERVOR(ChatColor.GOLD, "Holy Fervor", ""),
    ICE_BARRIER(ChatColor.WHITE, "Ice Barrier", "✲"),
    IGNITED(ChatColor.DARK_RED, "Ignited", ""),
    INCENDIARY(ChatColor.DARK_RED, "Incendiary", ""),
    RADIANT_FIRE(ChatColor.YELLOW, "Radiant Fire", "☀"),
    STATIC(ChatColor.GRAY, "Static", "");

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
