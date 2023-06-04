package com.runicrealms.plugin.donor;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public enum DonorRank {

    NONE(
            "none", 0, "runic.rank.none", "None", "", ChatColor.GRAY,
            5, false, false, 5, 3, WeaponSkinTier.NONE
    ),
    RED_BRAND(
            "redbrand", 1, "runic.rank.redbrand", "Redbrand", "The Rebel", ChatColor.GOLD,
            5, true, false, 5, 3, WeaponSkinTier.ELVEN
    ),
    KNIGHT(
            "knight", 2, "runic.rank.knight", "Knight", "The Bold", ChatColor.AQUA,
            6, true, false, 5, 3, WeaponSkinTier.ENHANCED
    ),
    HERO(
            "hero", 3, "runic.rank.hero", "Hero", "The Valiant", ChatColor.YELLOW,
            8, true, false, 8, 5, WeaponSkinTier.ENHANCED
    ),
    CHAMPION(
            "champion", 4, "runic.rank.champion", "Champion", "The Magnificent", ChatColor.DARK_PURPLE,
            10, true, true, 10, 6, WeaponSkinTier.ENHANCED
    );

    private final String identifier;
    private final int priority; // higher priority = rank is chosen above others if multiple are owned
    private final String permission;
    private final String name;
    private final String title;
    private final ChatColor chatColor;

    private final int classSlots;
    private final boolean priorityQueue;
    private final boolean partySummon;
    private final int gravestoneDuration;
    private final int gravestonePriorityDuration;
    private final WeaponSkinTier weaponSkinTier;

    DonorRank(
            String identifier, int priority, String permission, String name, String title, ChatColor chatColor,
            int classSlots, boolean priorityQueue, boolean partySummon, int gravestoneDuration, int gravestonePriorityDuration, WeaponSkinTier weaponSkinTier) {
        this.identifier = identifier;
        this.priority = priority;
        this.permission = permission;
        this.name = name;
        this.title = title;
        this.chatColor = chatColor;
        this.classSlots = classSlots;
        this.priorityQueue = priorityQueue;
        this.partySummon = partySummon;
        this.gravestoneDuration = gravestoneDuration;
        this.gravestonePriorityDuration = gravestonePriorityDuration;
        this.weaponSkinTier = weaponSkinTier;
    }

    public static DonorRank getDonorRank(Player player) {
        int currentPriority = -1;
        DonorRank currentRank = DonorRank.NONE;
        for (DonorRank rank : values()) {
            if (rank.hasPermission(player) && rank.getPriority() > currentPriority) {
                currentRank = rank;
                currentPriority = rank.getPriority();
            }
        }
        return currentRank;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getPriority() {
        return this.priority;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission(this.permission);
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;

    }

    public ChatColor getChatColor() {
        return this.chatColor;
    }

    public int getClassSlots() {
        return this.classSlots;
    }

    public boolean hasPriorityQueue() {
        return this.priorityQueue;
    }

    public boolean hasPartySummon() {
        return this.partySummon;
    }

    public int getGravestoneDuration() {
        return this.gravestoneDuration;
    }

    public int getGravestonePriorityDuration() {
        return this.gravestonePriorityDuration;
    }

    public WeaponSkinTier getWeaponSkinTier() {
        return this.weaponSkinTier;
    }


    public enum WeaponSkinTier {

        NONE, ENHANCED, ELVEN

    }


}
