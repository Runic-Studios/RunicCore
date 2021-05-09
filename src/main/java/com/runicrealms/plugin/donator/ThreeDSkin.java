package com.runicrealms.plugin.donator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum ThreeDSkin {

    // starter
    OAKEN_SHORTBOW("Oaken Shortbow", 0, 100, "runic.skins.oaken_shortbow"),
    OAKEN_MACE("Oaken Mace", 0, 40, "runic.skins.oaken_mace"),
    OAKEN_BRANCH("Oaken Branch", 0, 40, "runic.skins.oaken_branch"),
    OAKEN_SPARRING_SWORD("Oaken Sparring Sword", 0, 40, "runic.skins.oaken_sparring_sword"),
    OAKEN_AXE("Oaken Axe", 0, 40, "runic.skins.oaken_axe"),
    // archer
    TWISTED_LONGBOW("Twisted Longbow", 3, 101, "runic.skins.twisted_longbow"),
    // cleric
    DAWNBRINGER("Dawnbringer", 5, 41, "runic.skins.twisted_longbow"),
    // mage
    TWISTED_SCYTHE("Twisted Scythe", 3, 41, "runic.skins.twisted_scythe"),
    ANCIENT_ARCANE_ROD("Ancient Arcane Rod", 2, 50, "runic.skins.ancient_arcane_rod"),
    // rogue
    TWISTED_DAGGER("Twisted Dagger", 3, 41, "runic.skins.twisted_dagger"),
    BLADE_OF_THE_BETRAYER("Blade of the Betrayer", 6, 42, "runic.skins.blade_of_the_betrayer"),
    // warrior
    DEATHBRINGER("Deathbringer", 2, 101, "runic.skins.deathbringer");

    private final String displayName;
    private final int twoDDurability;//Integer
    private final int threeDDurability;//Integer
    private final String permission;

    private ThreeDSkin(String displayName, int twoDDurability, int threeDDurability, String permission) {
        this.displayName = displayName;
        this.twoDDurability = twoDDurability;
        this.threeDDurability = threeDDurability;
        this.permission = permission;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getTwoDDurability() {
        return this.twoDDurability;
    }

    public Integer getThreeDDurability() {
        return this.threeDDurability;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean hasPermission(Player player) {
        return player.hasPermission(this.permission);
    }

    public static ThreeDSkin getSkinFromName(String displayName) {
        for (ThreeDSkin skin : ThreeDSkin.values()) {
            if (skin.getDisplayName().equals(ChatColor.stripColor(displayName))) {
                return skin;
            }
        }
        return null;
    }

    public static boolean findArtifactMaterial(ItemStack itemStack) {
        return (itemStack.getType() == Material.BOW
                || itemStack.getType() == Material.WOODEN_SHOVEL
                || itemStack.getType() == Material.WOODEN_HOE
                || itemStack.getType() == Material.WOODEN_SWORD
                || itemStack.getType() == Material.WOODEN_AXE);
    }

}
