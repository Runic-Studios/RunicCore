package com.runicrealms.plugin.weaponskin;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.WeaponSkinAPI;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WeaponSkinManager implements WeaponSkinAPI, Listener {

    private final Set<WeaponSkin> weaponSkins = new HashSet<>();
    private final Map<UUID, WeaponSkinHolder> playerWeaponSkins = new HashMap<>();

    public WeaponSkinManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        WeaponSkinConfigLoader.loadFromConfig(new File(RunicCore.getInstance().getDataFolder(), "weapon-skins.yml"));
        RunicCore.getCommandManager().getCommandCompletions().registerCompletion("weapon-skins", (context) ->
                weaponSkins.stream()
                        .map(WeaponSkin::customName)
                        .collect(Collectors.toSet()));
    }

    @Override
    public boolean canActivateSkin(Player player, WeaponSkin skin) {
        if (skin.hasRank() && !player.hasPermission("runic.rank." + skin.rank())) return false;
        if (skin.hasAchievementID() && !RunicCommon.getAchievementsAPI().hasAchievement(player, skin.achievementID()))
            return false;
        return !skin.hasPermission() || player.hasPermission(skin.permission());
    }

    @Override
    public void activateSkin(Player player, WeaponSkin skin) {
        playerWeaponSkins.get(player.getUniqueId()).setSkinActive(skin, true);
        if (!canActivateSkin(player, skin))
            throw new IllegalStateException("Player " + player.getName() + " cannot equip skin " + skin.customName());

        for (ItemStack item : player.getInventory()) {
            if (item.getType() == skin.material()) {
                Damageable meta = (Damageable) item.getItemMeta();
                meta.setDamage(skin.damage());
                item.setItemMeta((ItemMeta) meta);
            }
        }
    }

    @Override
    public void deactivateSkin(Player player, WeaponSkin skin) {
        playerWeaponSkins.get(player.getUniqueId()).setSkinActive(skin, false);

        for (ItemStack item : player.getInventory()) {
            if (item.getType() == skin.material()) {
                disableSkin(item);
            }
        }
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerWeaponSkins.put(event.getPlayer().getUniqueId(), new WeaponSkinHolder(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerWeaponSkins.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (weaponSkinActive(event.getPlayer(), event.getItemDrop().getItemStack().getType())) {
            event.getItemDrop().setItemStack(disableSkin(event.getItemDrop().getItemStack()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRunicDeath(RunicDeathEvent event) {
        event.getVictim().getInventory().forEach(this::disableSkin);
    }

    private ItemStack disableSkin(ItemStack itemStack) {
        Damageable meta = (Damageable) itemStack.getItemMeta();
        meta.setDamage(RunicItemsAPI.getItemStackTemplate(itemStack).getDisplayableItem().getDamage());
        itemStack.setItemMeta((ItemMeta) meta);
        return itemStack;
    }

}
