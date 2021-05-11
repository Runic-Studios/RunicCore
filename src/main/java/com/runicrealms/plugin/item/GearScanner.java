package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GearScanner {

    /**
     * Returns the bonus health of all gear pieces the player is wearing.
     * @param uuid uuid of player
     * @return added item health
     */
    public static int getItemHealth(UUID uuid) {
        return RunicItemsAPI.getAddedPlayerStats(uuid).getAddedHealth();
    }

    /**
     * Returns the bonus dexterity of all gear pieces the player is wearing.
     * @param uuid uuid of player
     * @return added item dex
     */
    public static int getItemDexterity(UUID uuid) {
        if (RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.DEXTERITY) != null)
            return RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.DEXTERITY);
        else
            return 0;
    }

    /**
     * Returns the bonus intelligence of all gear pieces the player is wearing.
     * @param uuid uuid of player
     * @return added item int
     */
    public static int getItemIntelligence(UUID uuid) {
        if (RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.INTELLIGENCE) != null)
            return RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.INTELLIGENCE);
        else
            return 0;
    }

    /**
     * Returns the bonus strength of all gear pieces the player is wearing.
     * @param uuid uuid of player
     * @return added item str
     */
    public static int getItemStrength(UUID uuid) {
        if (RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.STRENGTH) != null)
            return RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.STRENGTH);
        else
            return 0;
    }

    /**
     * Returns the bonus vitality of all gear pieces the player is wearing.
     * @param uuid uuid of player
     * @return added item vit
     */
    public static int getItemVitality(UUID uuid) {
        if (RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.VITALITY) != null)
            return RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.VITALITY);
        else
            return 0;
    }

    /**
     * Returns the bonus wisdom of all gear pieces the player is wearing.
     * @param uuid uuid of player
     * @return added item wis
     */
    public static int getItemWisdom(UUID uuid) {
        if (RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.WISDOM) != null)
            return RunicItemsAPI.getAddedPlayerStats(uuid).getAddedStats().get(Stat.WISDOM);
        else
            return 0;
    }

    public static int getMinDamage(Player pl) {
        ItemStack item = pl.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
            return 0;
        return (int) AttributeUtil.getCustomDouble(item, "custom.minDamage");
    }

    public static int getMaxDamage(Player pl) {
        ItemStack item = pl.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
            return 0;
        return (int) AttributeUtil.getCustomDouble(item, "custom.maxDamage");
    }
}
