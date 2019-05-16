package com.runicrealms.plugin.item.rune;

import com.runicrealms.plugin.item.LoreGenerator;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;

import java.util.ArrayList;

public class SpellsGUI implements InventoryProvider {

    /**
     * CHANGE THE ID FOR EACH NEW ItemGUI
     */
    public static final SmartInventory RUNIC_SPELLS = SmartInventory.builder()
            .id("runicSpells")
            .provider(new SpellsGUI())
            .size(4, 9)
            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Available Spells")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // grab player's rune
        ItemStack rune = player.getInventory().getItem(1);
        ItemMeta meta = rune.getItemMeta();

        // build the menu description, updates live with their current skills
        String primarySpell = AttributeUtil.getSpell(rune, "primarySpell");
        String secondarySpell = AttributeUtil.getSpell(rune, "secondarySpell");
        ArrayList<String> desc = new ArrayList<>();
        desc.add("");
        desc.add(ChatColor.GRAY + "Spells:");
        desc.add(ChatColor.WHITE + "Primary: " + ChatColor.GREEN + primarySpell);
        desc.add(ChatColor.WHITE + "Secondary: " + ChatColor.GREEN + secondarySpell);
        desc.add("");
        desc.add("Left click a spell to set your primary!");
        desc.add("Right click a spell to set your secondary!");
        desc.add(ChatColor.DARK_GRAY + "Click here to return to the editor");

        // build the menu items
        contents.set(1, 3, ClickableItem.of
                (menuItem(rune.getType(),
                        ChatColor.YELLOW,
                        meta.getDisplayName(),
                        desc, ((Damageable) meta).getDamage(), 1),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            RuneGUI.CUSTOMIZE_RUNE.open(player);
                        }));

        int skillpoints = RunicCore.getInstance().getConfig().getInt(player.getUniqueId() + ".info.skillpoints");
        ArrayList<String> spDesc = new ArrayList<>();
        spDesc.add("");
        spDesc.add(ChatColor.GRAY + "Use skill points to unlock new spells!");
        spDesc.add("");
        spDesc.add(ChatColor.GREEN + "Earn skill points by completing quests");
        spDesc.add(ChatColor.GREEN + "and leveling your character!");
        contents.set(1, 5, ClickableItem.of
                (menuItem(Material.BONE_MEAL, ChatColor.WHITE,
                        ChatColor.BOLD + "Skill Points: " + skillpoints,
                        spDesc, 0, skillpoints), // read config for amount
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            RuneGUI.CUSTOMIZE_RUNE.open(player);
                        }));

        // runic spells are NOT class specific
        displayRunicSpells(player, contents);
    }

    private void displayRunicSpells(Player player, InventoryContents contents) {
        displaySpell(player, contents, 2, 2, "Blink", false);
        displaySpell(player, contents, 2, 3, "Heal", false);
        displaySpell(player, contents, 2, 4, "Fireball", false);
        displaySpell(player, contents, 2, 5, "Frostbolt", false);
        displaySpell(player, contents, 2, 6, "Sprint", false);
    }

    // display for each skin
    private void displaySpell(Player player, InventoryContents contents, int row, int slot, String spellName, boolean isUnlocked) {

        double cooldown = RunicCore.getSkillManager().getSkillByName(spellName).getCooldown();
        int manaCost = RunicCore.getSkillManager().getSkillByName(spellName).getManaCost();
        ArrayList<String> desc = new ArrayList<>();
        desc.add("");
        for (String line : RunicCore.getSkillManager().getSkillByName(spellName).getDescription().split("\n")) {
            desc.add(ChatColor.GRAY + line);
        }
        desc.add("");
        desc.add(ChatColor.RED + "Cooldown: " + ChatColor.YELLOW + cooldown + "s");
        desc.add(ChatColor.DARK_AQUA + "Mana Cost: " + ChatColor.WHITE + manaCost);

        ItemStack item = player.getInventory().getItem(1);
        if (player.hasPermission("ftr.spells." + spellName) || isUnlocked == true) {
            contents.set(row, slot, ClickableItem.of
                    (spellMenuItem(Material.ENCHANTED_BOOK,
                            ChatColor.GREEN,
                            spellName, desc),
                            e -> {
                                if (e.isLeftClick()) {
                                    updateArtifactSpell(player, item, "primarySpell", spellName);
                                } else {
                                    updateArtifactSpell(player, item, "secondarySpell", spellName);
                                }
                            }));
        } else {
            contents.set(row, slot, ClickableItem.of
                    (spellMenuItem(Material.ENCHANTED_BOOK,
                            ChatColor.RED,
                            spellName + " | Cost: 1 SP", desc),
                            e -> {
                                player.sendMessage(ChatColor.RED + "You haven't unlocked this spell yet!");
                            }));
        }
    }

    // updates a spell
    private void updateArtifactSpell(Player pl, ItemStack item, String spellSlot, String spellName) {
        // check so players can't have two of the same spell
        String otherSpell = "";

        if (spellSlot.equals("primarySpell")) {
            otherSpell = AttributeUtil.getSpell(item, "secondarySpell");
        } else {
            otherSpell = AttributeUtil.getSpell(item, "primarySpell");
        }

        if (!otherSpell.equals(spellName)) {
            item = AttributeUtil.addSpell(item, spellSlot, spellName);
            LoreGenerator.generateRuneLore(item);
            pl.getInventory().setItem(1, item);
            SpellsGUI.RUNIC_SPELLS.open(pl);
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            pl.sendMessage(ChatColor.GREEN + "You imbued your rune with " + spellName + "!");
        } else {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You can't imbue the same skill in two slots.");
        }
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor dispColor, String displayName, ArrayList<String> desc, int durability, int amount) {
        if (amount == 0 ) amount = 1;
        if (amount > 64) amount = 64;
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(dispColor + displayName);
        ArrayList<String> lore = new ArrayList<>();
        for (String s : desc) {
            lore.add(ChatColor.GRAY + s);
        }
        meta.setLore(lore);
        if (durability != 0) {
            ((Damageable) meta).setDamage(durability);
        }
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    // creates the visual menu
    private ItemStack spellMenuItem(Material material, ChatColor dispColor, String displayName, ArrayList<String> desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(dispColor + displayName);
        meta.setLore(desc);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}
