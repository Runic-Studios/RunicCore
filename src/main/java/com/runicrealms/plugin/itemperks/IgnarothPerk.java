package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.dynamicitem.DynamicItemManager;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.dynamic.DynamicItemTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.player.FlatStatsModifier;
import com.runicrealms.plugin.runicitems.player.PlayerEquipmentCache;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.tr7zw.nbtapi.NBTItem;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An item perk that gives a stat bonus while inside Ignaroth's Lair
 *
 * @author BoBoBalloon
 */
public class IgnarothPerk extends ItemPerkHandler {
    private final Set<UUID> alreadyActive; //this is necessary because the built-in set is updated before onChange is called
    private final FlatStatsModifier modifier;

    private static final String REGION = "ignaroth_lair";

    public IgnarothPerk(@NotNull String identifier, @NotNull Stat stat, int amount) {
        super(identifier);
        this.alreadyActive = new HashSet<>();
        this.modifier = new FlatStatsModifier(Map.of(stat, amount), null, 0);
    }

    @Override
    public void onChange(Player player, int stacks) {
        boolean inside = isInRegion(player); //can this be done async? xD

        PlayerEquipmentCache cache = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId());

        if (cache == null) {
            return;
        }

        if (stacks > 0 && inside && !this.alreadyActive.contains(player.getUniqueId())) {
            cache.addModifier(this.modifier);
            this.alreadyActive.add(player.getUniqueId());
        }

        if (stacks <= 0 && inside && this.alreadyActive.contains(player.getUniqueId())) {
            cache.removeModifier(this.modifier);
            this.alreadyActive.remove(player.getUniqueId());
        }

        if (!inside && this.alreadyActive.contains(player.getUniqueId())) {
            cache.removeModifier(this.modifier);
            this.alreadyActive.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onRegionEntered(RegionEnteredEvent event) {
        Player player = event.getPlayer();

        if (player == null || !event.getRegionName().equals(REGION) || !this.isActive(player)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> this.onChange(player, 1));
    }

    @EventHandler(ignoreCancelled = true)
    private void onRegionLeft(RegionLeftEvent event) {
        Player player = event.getPlayer();

        if (player == null || !event.getRegionName().equals(REGION) || !this.isActive(player)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> this.onChange(player, 0));
    }

    private static boolean isInRegion(@NotNull Player player) {
        World world = BukkitAdapter.adapt(player.getWorld());
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);

        if (regionManager == null) {
            return false;
        }

        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));

        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase(REGION)) {
                return true;
            }
        }

        return false;
    }

    public static class InsideLairTextPlaceholder extends DynamicItemTextPlaceholder {
        public InsideLairTextPlaceholder() {
            super("inside-ignaroths-lair");
        }

        @Nullable
        @Override
        public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
            return isInRegion(viewer) ? DynamicItemManager.CHECKMARK : DynamicItemManager.X;
        }
    }
}
