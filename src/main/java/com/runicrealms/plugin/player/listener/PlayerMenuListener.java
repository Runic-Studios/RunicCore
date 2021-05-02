package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.mysterybox.MysteryLoot;
import com.runicrealms.plugin.mysterybox.animation.Animation;
import com.runicrealms.plugin.mysterybox.animation.animations.Tornado;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Controls the player menu in the inventory crafting slots
 */
public class PlayerMenuListener implements Listener {

    private static final int PLAYER_CRAFT_INV_SIZE = 5;

    public PlayerMenuListener() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {

            for (PlayerCache cache : RunicCore.getCacheManager().getPlayerCaches().values()) {

                UUID uuid = cache.getPlayerID();
                Player pl = Bukkit.getPlayer(cache.getPlayerID());
                if (pl == null) continue;

                ItemStack plMenu = item(pl, Material.PLAYER_HEAD, "&eCharacter Info",
                        "\n&7Here are your stats and stuff!\n\n" + combatPercentages(uuid));

                //item 2
                ItemStack questJournal = item(pl, Material.BOOK, "&6Quest Journal",
                        "\n&fClick here &7to view\n&7the quest journal!");

                ItemStack lootChests = item(pl, Material.CHEST, "&dMystery Boxes",
                        "\n&aFeature Coming Soon!"); // todo: remove?

                // item 3 must update dynamically
                int healthBonus = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() -
                        PlayerLevelUtil.calculateHealthAtLevel(cache.getClassLevel(), cache.getClassName());

                ItemStack gemMenu = item(pl, Material.REDSTONE, "&eCharacter Stats",
                        "\n&7Your character stats improve" +
                                "\n&7your potency in battle!" +
                                "\n&7Earn them from your" +
                                "\n&dSkill Tree &7or from items!" +
                                "\n&7Check your bonuses above" +
                                "\n&7in Character Info!" +
                                "\n\n&c❤ (Health) &7bonus: " + statPrefix(healthBonus) + healthBonus +
                                "\n" + formattedStat("Dexterity", RunicCoreAPI.getPlayerDexterity(uuid)) +
                                "\n" + formattedStat("Intelligence", RunicCoreAPI.getPlayerIntelligence(uuid)) +
                                "\n" + formattedStat("Strength", RunicCoreAPI.getPlayerStrength(uuid)) +
                                "\n" + formattedStat("Vitality", RunicCoreAPI.getPlayerVitality(uuid)) +
                                "\n" + formattedStat("Wisdom", RunicCoreAPI.getPlayerWisdom(uuid)));

                InventoryView view = pl.getOpenInventory();

                // If the open inventory is a player inventory
                // Update to the ring item
                // This will update even when it is closed, but
                // it is a small price to pay IMO
                if (isPlayerCraftingInv(view)) {

                    // uses packets to create visual items clientside that can't interact w/ the server
                    // prevents duping
                    PacketPlayOutSetSlot packet1 = new PacketPlayOutSetSlot(0, 1, CraftItemStack.asNMSCopy(plMenu));
                    PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(0, 2, CraftItemStack.asNMSCopy(questJournal));
                    PacketPlayOutSetSlot packet3 = new PacketPlayOutSetSlot(0, 3, CraftItemStack.asNMSCopy(gemMenu));
                    PacketPlayOutSetSlot packet4 = new PacketPlayOutSetSlot(0, 4, CraftItemStack.asNMSCopy(lootChests));

                    ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet1);
                    ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet2);
                    ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet3);
                    ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet4);

                }
            }
        }, 0L, 10L);
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

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();

        // Don't allow players to remove anything from their
        // own crafting matrix
        // The view includes the player's entire inventory
        // as well, so check to make sure that the clicker
        // did not click on their own inventory
        if (isPlayerCraftingInv(view) &&
                event.getClickedInventory() != event.getWhoClicked().getInventory()) {
            if (event.getSlot() < 5 && event.getSlot() > 0) {

                event.setCancelled(true);
                Player pl = (Player) event.getWhoClicked();
                pl.updateInventory();

                if (event.getSlot() == 2 && pl.getGameMode() != GameMode.CREATIVE) {
                    pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                    pl.performCommand("quest");
                } else if (event.getSlot() == 4) {
                    Animation animation = new Tornado(MysteryLoot.getMysteryItems());
                    animation.spawn(pl, pl.getLocation());
                }
            }
        }
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
        double rangedDmgPercent = (BaseStatEnum.getRangedDmgMult() * 100) * dexterity;
        double speedPercent = (BaseStatEnum.getMovementSpeedMult() * 100) * dexterity;
        double magicDmgPercent = (BaseStatEnum.getMagicDmgMult() * 100) * intelligence;
        double maxManaPercent = (BaseStatEnum.getMaxManaMult() * 100) * intelligence;
        double meleeDmgPercent = (BaseStatEnum.getMeleeDmgMult() * 100) * strength;
        double defensePercent = (BaseStatEnum.getDamageReductionMult() * 100) * vitality;
        double healthRegenPercent = (BaseStatEnum.getHealthRegenMult() * 100) * vitality;
        double spellHealingPercent = (BaseStatEnum.getSpellHealingMult() * 100) * wisdom;
        double manaRegenPercent = (BaseStatEnum.getManaRegenMult() * 100) * wisdom;
        String dexterityString = statPrefix(dexterity) + df.format(rangedDmgPercent) + "% Ranged Dmg" +
                "\n" + statPrefix(dexterity) + df.format(speedPercent) + "% Speed\n";
        String intelligenceString = statPrefix(intelligence) + df.format(magicDmgPercent) + "% Magic Dmg" +
                "\n" + statPrefix(intelligence) + df.format(maxManaPercent) + "% Max Mana\n";
        String strengthString = statPrefix(strength) + df.format(meleeDmgPercent) + "% Melee Dmg\n";
        String vitalityString = statPrefix(vitality) + df.format(defensePercent) + "% Defense" +
                "\n" + statPrefix(vitality) + df.format(healthRegenPercent) + "% Health Regen\n";
        String wisdomString = statPrefix(wisdom) + df.format(spellHealingPercent) + "% Spell Healing" +
                "\n" + statPrefix(wisdom) + df.format(manaRegenPercent) + "% Mana Regen\n";
        return dexterityString + intelligenceString + strengthString + vitalityString + wisdomString;
    }

    /**
     * Returns a formatted string of the player's combat stats
     * @param name name of the stat (dexterity)
     * @param value value of the stat (RunicCoreAPI)
     * @return a formatted string for use in the player menu
     */
    private String formattedStat(String name, int value) {
        BaseStatEnum baseStatEnum = BaseStatEnum.getFromName(name);
        if (baseStatEnum == null) {
            Bukkit.getLogger().info("Base stat enum not found!");
            return "";
        }
        return baseStatEnum.getChatColor() +
                baseStatEnum.getIcon() +
                " (" + baseStatEnum.getPrefix() +
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
        item.setItemMeta(meta);
        return item;
    }
}