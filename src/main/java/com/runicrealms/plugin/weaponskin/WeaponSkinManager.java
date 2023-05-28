package com.runicrealms.plugin.weaponskin;

import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WeaponSkinManager implements WeaponSkinAPI, Listener {

    private Set<WeaponSkin> weaponSkins = new HashSet<>();
    private Map<UUID, WeaponSkinHolder> playerWeaponSkins = new HashMap<>();

    public WeaponSkinManager(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        WeaponSkinConfigLoader.loadFromConfig(new File(plugin.getDataFolder(), "weapon-skins.yml"));
    }

    public boolean canActivateSkin(Player player, WeaponSkin skin) {
        if (skin.hasRank() && !player.hasPermission("runic.rank." + skin.rank())) return false;
        if (skin.hasAchievementID()) return false; // TODO
        return !skin.hasPermission() || player.hasPermission(skin.permission());
    }

    public void activateSkin(Player player, WeaponSkin skin) {
        playerWeaponSkins.get(player.getUniqueId()).setSkinActive(skin, true);
        if (!canActivateSkin(player, skin))
            throw new IllegalStateException("Player " + player.getName() + " cannot equip skin " + skin.customName());

        for (ItemStack item : player.getInventory()) {
            if (item.getType() == skin.material()) {
                Damageable meta = (Damageable) item.getItemMeta();
                meta.setDamage(skin.damage());
            }
        }
    }

    public void deactivateSkin(Player player, WeaponSkin skin) {
        playerWeaponSkins.get(player.getUniqueId()).setSkinActive(skin, false);

        for (ItemStack item : player.getInventory()) {
            if (item.getType() == skin.material()) {
                Damageable meta = (Damageable) item.getItemMeta();
                meta.setDamage(RunicItemsAPI.getItemStackTemplate(item).getDisplayableItem().getDamage());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerWeaponSkins.put(event.getPlayer().getUniqueId(), new WeaponSkinHolder(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerWeaponSkins.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public Set<WeaponSkin> getAllSkins() {
        return weaponSkins;
    }

    @Override
    public boolean hasWeaponSkin(Player player, WeaponSkin skin) {
        return playerWeaponSkins.get(player.getUniqueId()).ownsSkin(skin);
    }

    @Override
    public boolean weaponSkinActive(Player player, WeaponSkin skin) {
        return playerWeaponSkins.get(player.getUniqueId()).skinActive(skin);
    }

    @Override
    public boolean weaponSkinActive(Player player, Material material) {
        return playerWeaponSkins.get(player.getUniqueId()).getSkinsOwned()
                .stream()
                .filter((skin) -> playerWeaponSkins.get(player.getUniqueId()).skinActive(skin))
                .anyMatch((skin) -> skin.material() == material);
    }

    @Nullable
    @Override
    public WeaponSkin getWeaponSkin(Player player, Material material) {
        return playerWeaponSkins.get(player.getUniqueId()).getSkinsOwned()
                .stream()
                .filter((skin) -> playerWeaponSkins.get(player.getUniqueId()).skinActive(skin)
                        && skin.material() == material)
                .findFirst().orElse(null);
    }

}
