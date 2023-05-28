package com.runicrealms.plugin.weaponskin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface WeaponSkinAPI {

    /**
     * Gets a list of all skins loaded from config
     */
    Set<WeaponSkin> getAllSkins();

    /**
     * Checks if a player owns a weapon skin (has permission to use it)
     */
    boolean hasWeaponSkin(Player player, WeaponSkin skin);

    /**
     * Checks if a player has a given weapon skin activated currently
     */
    boolean weaponSkinActive(Player player, WeaponSkin skin);

    /**
     * Checks if a player has a weapon skin currently activated for a given material
     */
    boolean weaponSkinActive(Player player, Material material);

    /**
     * Gets the player's current weapon skin activated for a material, or null
     */
    @Nullable
    WeaponSkin getWeaponSkin(Player player, Material material);


}
