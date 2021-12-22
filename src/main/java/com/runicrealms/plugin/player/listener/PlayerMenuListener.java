package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.professions.api.RunicProfessionsAPI;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicitems.Stat;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Controls the player menu in the inventory crafting slots
 */
public class PlayerMenuListener implements Listener {

    private static final int PLAYER_CRAFT_INV_SIZE = 5;
    private static final Set<Integer> PLAYER_CRAFTING_SLOTS = new HashSet<>(Arrays.asList(1, 2, 3, 4));

    public PlayerMenuListener() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {

            for (PlayerCache playerCache : RunicCore.getCacheManager().getPlayerCaches().values()) {

                Player player = Bukkit.getPlayer(playerCache.getPlayerID());
                if (player == null) continue;
                InventoryView view = player.getOpenInventory();

                // If the open inventory is a player inventory
                // Update to the ring item
                // This will update even when it is closed, but
                // it is a small price to pay IMO
                if (isPlayerCraftingInv(view)) {

                    // uses packets to create visual items clientside that can't interact w/ the server
                    // prevents duping
                    // todo: rework items 1 and 2 to be a menu to reduce calculations
                    PacketPlayOutSetSlot packet1 = new PacketPlayOutSetSlot(0, 1, CraftItemStack.asNMSCopy(combatStatsIcon(player)));
                    PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(0, 2, CraftItemStack.asNMSCopy(gemMenuIcon(player, playerCache)));
                    PacketPlayOutSetSlot packet3 = new PacketPlayOutSetSlot(0, 3, CraftItemStack.asNMSCopy(gatheringLevelItemStack(player)));
                    PacketPlayOutSetSlot packet4 = new PacketPlayOutSetSlot(0, 4, CraftItemStack.asNMSCopy(groupFinderIcon(player)));
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet1);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet2);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet3);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet4);
                }
            }
        }, 100L, 10L);
    }

    /**
     * The info item for the player's gathering levels
     *
     * @param player to display menu for
     * @return an Itemstack to display
     */
    private ItemStack gatheringLevelItemStack(Player player) {
        return item(player, Material.IRON_PICKAXE, "&eGathering Skills",
                "\n&6&lCLICK" +
                        "\n&7To view your gathering skills!" +
                        "\n&7They are account-wide!\n\n");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        // Remove the ring item in the matrix to prevent
        // players from duping them
        if (isPlayerCraftingInv(view)) {
            view.setItem(1, null);
            view.setItem(2, null);
            view.setItem(3, null);
            view.setItem(4, null);
            view.getTopInventory().clear();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.CRAFTING) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getClickedInventory().equals(e.getView().getBottomInventory())) return;
        if (PLAYER_CRAFTING_SLOTS.contains(e.getSlot())) {
            e.setCancelled(true);
            player.updateInventory();
            if (e.getCursor() != null
                    && e.getCursor().getType() == Material.AIR // prevents clicking with items on cursur
                    && e.getSlot() == 3)
                RunicProfessionsAPI.openGatheringGUI(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!e.getInventory().getType().equals(InventoryType.CRAFTING)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getInventory().equals(e.getView().getBottomInventory())) return;
        if (e.getInventorySlots().contains(1) || e.getInventorySlots().contains(2)
                || e.getInventorySlots().contains(3) || e.getInventorySlots().contains(4)) {
            e.setCancelled(true);
        }
    }

    /**
     * @param player
     * @return
     */
    private ItemStack combatStatsIcon(Player player) {
        return item(player, Material.PLAYER_HEAD, "&eCharacter Info",
                "\n&7Here are the combat bonuses" +
                        "\n&7of your character! They" +
                        "\n&7come from your stats," +
                        "\n&7which you can check" +
                        "\n&7in &eCharacter Stats&7!\n\n" +
                        combatPercentages(player.getUniqueId()));
    }

    /**
     * Creates the menu icon for the
     *
     * @param player      who the menu belongs to
     * @param playerCache
     * @return
     */
    private ItemStack gemMenuIcon(Player player, PlayerCache playerCache) {
        UUID uuid = player.getUniqueId();
        // item 2 must update dynamically
        int healthBonus = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() -
                PlayerLevelUtil.calculateHealthAtLevel(playerCache.getClassLevel(), playerCache.getClassName());
        return item(player, Material.REDSTONE, "&eCharacter Stats",
                "\n&7Your character stats improve" +
                        "\n&7your potency in battle!" +
                        "\n&7Earn them from your" +
                        "\n&dSkill Tree &7or from items!" +
                        "\n&7Check your bonuses above" +
                        "\n&7in &eCharacter Info&7!" +
                        "\n\n&c❤ (Health): " + statPrefix(healthBonus) + healthBonus +
                        "\n" + formattedStat("Dexterity", RunicCoreAPI.getPlayerDexterity(uuid)) +
                        "\n" + formattedStat("Intelligence", RunicCoreAPI.getPlayerIntelligence(uuid)) +
                        "\n" + formattedStat("Strength", RunicCoreAPI.getPlayerStrength(uuid)) +
                        "\n" + formattedStat("Vitality", RunicCoreAPI.getPlayerVitality(uuid)) +
                        "\n" + formattedStat("Wisdom", RunicCoreAPI.getPlayerWisdom(uuid)));
    }

    private ItemStack groupFinderIcon(Player player) {
        return item
                (
                        player,
                        Material.ENDER_EYE,
                        ChatColor.RED + "Group Finder",
                        ChatColor.GRAY + "Click to open the group finder!"
                );
    }

    private static boolean isPlayerCraftingInv(InventoryView view) {
        return view.getTopInventory().getSize() == PLAYER_CRAFT_INV_SIZE;
    }

    private String statPrefix(int stat) {
        return stat > 0 ? "&a+" : "&7+";
    }

    private String combatPercentages(UUID uuid) {
        int dexterity = RunicCoreAPI.getPlayerDexterity(uuid);
        int intelligence = RunicCoreAPI.getPlayerIntelligence(uuid);
        int strength = RunicCoreAPI.getPlayerStrength(uuid);
        int vitality = RunicCoreAPI.getPlayerVitality(uuid);
        int wisdom = RunicCoreAPI.getPlayerWisdom(uuid);
        DecimalFormat df = new DecimalFormat("0.##");
        double rangedDmgPercent = (Stat.getRangedDmgMult() * 100) * dexterity;
        double speedPercent = (Stat.getMovementSpeedMult() * 100) * dexterity;
        double magicDmgPercent = (Stat.getMagicDmgMult() * 100) * intelligence;
        double maxManaPercent = (Stat.getMaxManaMult() * 100) * intelligence;
        double meleeDmgPercent = (Stat.getMeleeDmgMult() * 100) * strength;
        double critPercent = 0;
        double defensePercent = (Stat.getDamageReductionMult() * 100) * vitality;
        if (defensePercent > Stat.getDamageReductionCap())
            defensePercent = Stat.getDamageReductionCap();
        double healthRegenPercent = (Stat.getHealthRegenMult() * 100) * vitality;
        double spellHealingPercent = (Stat.getSpellHealingMult() * 100) * wisdom;
        double manaRegenPercent = (Stat.getManaRegenMult() * 100) * wisdom;
        String dexterityString = statPrefix(dexterity) + df.format(rangedDmgPercent) + "% Ranged Dmg" +
                "\n" + statPrefix(dexterity) + df.format(speedPercent) + "% Movespeed\n";
        String intelligenceString = statPrefix(intelligence) + df.format(magicDmgPercent) + "% Magic Dmg" +
                "\n" + statPrefix(intelligence) + df.format(maxManaPercent) + "% Max Mana\n";
        String strengthString = statPrefix(strength) + df.format(meleeDmgPercent) + "% Melee Dmg" +
                "\n" + df.format(critPercent) + "% Crit\n";
        String vitalityString = statPrefix(vitality) + df.format(defensePercent) + "% Defense" + (defensePercent >= Stat.getDamageReductionCap() ? " (Cap Reached)" : "") +
                "\n" + statPrefix(vitality) + df.format(healthRegenPercent) + "% Health Regen\n";
        String wisdomString = statPrefix(wisdom) + df.format(spellHealingPercent) + "% Spell Healing" +
                "\n" + statPrefix(wisdom) + df.format(manaRegenPercent) + "% Mana Regen\n";
        return dexterityString + intelligenceString + strengthString + vitalityString + wisdomString;
        // todo crit, dodge, attack speed
    }

    /**
     * Returns a formatted string of the player's combat stats
     *
     * @param name  name of the stat (dexterity)
     * @param value value of the stat (RunicCoreAPI)
     * @return a formatted string for use in the player menu
     */
    private String formattedStat(String name, int value) {
        Stat stat = Stat.getFromName(name);
        if (stat == null) {
            Bukkit.getLogger().info("Base stat enum not found!");
            return "";
        }
        return stat.getChatColor() +
                stat.getIcon() +
                " (" + stat.getPrefix() +
                "): " + statPrefix(value) + value;
    }

    private ItemStack item(Player pl, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (material == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(pl);
        }

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ColorUtil.format(name));
        String[] desc = description.split("\n");
        for (String line : desc) {
            lore.add(ColorUtil.format(line));
        }
        meta.setLore(lore);
        ((Damageable) meta).setDamage(3);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}