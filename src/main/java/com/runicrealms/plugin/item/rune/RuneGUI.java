package com.runicrealms.plugin.item.rune;

import com.runicrealms.plugin.item.GUIMenu.OptionClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.LoreGenerator;

import java.util.ArrayList;
import java.util.List;

public class RuneGUI {

    /**
     * Opens the artifact editor using new instance of inventory
     */
    public static ItemGUI runeEditor(Player pl, ItemStack artifact, int durability) {

        return new ItemGUI("&f&l" + pl.getName() + "'s &d&lRune Editor", 27, event -> {

            // open spell editor
            if (event.getSlot() == 3+9) {

                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI spellEditor = RuneGUI.spellEditor(pl, artifact, durability);
                spellEditor.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else if (event.getSlot() == 5+9) {

                // close editor
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }

            // setup items
        }, RunicCore.getInstance())
                .setOption(3+9, new ItemStack(Material.FIRE_CHARGE), "&aSpell Editor",
                        "&fClick &7to customize your runic abilities", 0, false)
                .setOption(5+9, new ItemStack(Material.BARRIER), "&cClose",
                        "&7Exit the editor", 0, false);
    }

    private static ItemGUI spellEditor(Player pl, ItemStack rune, int durability) {

        // grab player's rune
        ItemMeta meta = rune.getItemMeta();

        int size = 54;

        ItemGUI spellEditor = new ItemGUI("&f&l" + pl.getName() + "'s &e&lSpell Editor", size, (OptionClickEvent event) -> {

            if (event.getSlot() == 12) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI menu = RuneGUI.runeEditor(pl, rune, durability);
                menu.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else if (event.getSlot() == 14) {

                event.setWillClose(false);
                event.setWillDestroy(false);

            } else {

                String spellName = event.getInventory().getItem(event.getSlot()).getItemMeta().getDisplayName().replace(" ", "").toLowerCase();

                // apply the skin if the player has the permission
                if (event.getClick() == ClickType.LEFT) {

                    if (!pl.hasPermission("core.spells." + spellName)) {
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(ChatColor.RED + "You haven't unlocked that spell yet." + ChatColor.WHITE + " Shift + Left-click" + ChatColor.GRAY + " a spell to unlock it.");
                        return;
                    }

                    updateRuneSpell(pl, rune, "primarySpell",
                            event.getInventory().getItem(event.getSlot()).getItemMeta().getDisplayName());

                } else if (event.getClick() == ClickType.RIGHT) {

                    if (!pl.hasPermission("core.spells." + spellName)) {
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(ChatColor.RED + "You haven't unlocked that spell yet." + ChatColor.WHITE + " Shift + Left-click" + ChatColor.GRAY + " a spell to unlock it.");
                        return;
                    }

                    updateRuneSpell(pl, rune, "secondarySpell",
                            event.getInventory().getItem(event.getSlot()).getItemMeta().getDisplayName());

                } else if (event.getClick() == ClickType.SHIFT_LEFT) {

                    event.setCancelled(true);
                    event.setWillClose(false);
                    event.setWillDestroy(true);
                    // unlock spell
                    if (!pl.hasPermission("core.spells." + spellName)) {

                        int numPoints = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.spellpoints");
                        if (numPoints > 0) {
                            RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.spellpoints", numPoints-1);
                            RunicCore.getInstance().saveConfig();
                            RunicCore.getInstance().reloadConfig();
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                    "lp user " + pl.getName() + " permission set core.spells." + spellName + " true");
                            // ex: lp user Skyfallin_ permission set core.spells.rejuvenate true
                            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                            pl.sendMessage(ChatColor.GREEN + "You have unlocked "
                                    + ChatColor.YELLOW + ChatColor.ITALIC
                                    + event.getInventory().getItem(event.getSlot()).getItemMeta().getDisplayName()
                                    + ChatColor.GREEN + "!");
                        }
                    }
                } else {
                    event.setCancelled(true);
                }

                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        }, RunicCore.getInstance());

        // build the menu description, updates live with their current spells
        String primarySpell = AttributeUtil.getSpell(rune, "primarySpell");
        String secondarySpell = AttributeUtil.getSpell(rune, "secondarySpell");
        spellEditor.setOption(12, new ItemStack(rune.getType()), "&a" + rune.getItemMeta().getDisplayName(),
                "\n" +
                        "&7Spells:" +
                        "\n&7Primary: &a" + primarySpell +
                        "\n&7Secondary: &a" + secondarySpell +
                        "\n" +
                        "\n&fLeft Click &7a spell to set your primary!" +
                        "\n&fRight Click &7a spell to set your secondary!" +
                        "\n&fClick here &7to return to the editor", ((Damageable) meta).getDamage(), false);

        int numPoints = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.spellpoints");
        spellEditor.setOption(14, new ItemStack(Material.BONE_MEAL), "&f&lSpell Points: &a&l" + numPoints,
                "\n&7Use spell points to unlock new spells!" +
                        "\n&aEarn spell points by completing quests" +
                        "\n&aand leveling-up!", 0, false);

        List<String> spells = new ArrayList<>();

        spells.add("Arcane Orb");
        spells.add("Blessing");
        spells.add("Blink");
        spells.add("Cleave");
        spells.add("Fire Aura");
        spells.add("Fireball");
        spells.add("Frostbolt");
        spells.add("Harpoon");
        spells.add("Lunge");
        spells.add("Parry");
        spells.add("Permafrost");
        spells.add("Petrify");
        spells.add("Reflect");
        spells.add("Shadow Strike");
        spells.add("Siphon");
        spells.add("Sprint");
        spells.add("Starfall");
        spells.add("Taunt");
        spells.add("Warsong");

        // first row of spells
        for (int i = 0; i < spells.size() && i < 7; i++) {

            // check for permissions, ex: ftr.spells.blessedrain
            boolean unlocked= false;
            if (pl.hasPermission("core.spells." + spells.get(i).replace(" ", "").toLowerCase())) {
                unlocked = true;
            }
            displaySpell(spellEditor, 19 + i, spells.get(i), unlocked);
        }

        // second row of spells
        for (int i = 7; i < spells.size() && i < 14; i++) {

            boolean unlocked= false;
            if (pl.hasPermission("core.spells." + spells.get(i).replace(" ", "").toLowerCase())) {
                unlocked = true;
            }
            displaySpell(spellEditor, 21 + i, spells.get(i), unlocked);
        }

        // third row of spells
        for (int i = 14; i < spells.size() && i < 21; i++) {

            boolean unlocked= false;
            if (pl.hasPermission("core.spells." + spells.get(i).replace(" ", "").toLowerCase())) {
                unlocked = true;
            }
            displaySpell(spellEditor, 23 + i, spells.get(i), unlocked);
        }

        return spellEditor;
    }

    // display for each skin
    private static void displaySpell(ItemGUI editor, int position, String spellName, boolean isUnlocked) {

        double cooldown = RunicCore.getSpellManager().getSpellByName(spellName).getCooldown();
        int manaCost = RunicCore.getSpellManager().getSpellByName(spellName).getManaCost();

        StringBuilder spellDesc = new StringBuilder();
        for (String line : RunicCore.getSpellManager().getSpellByName(spellName).getDescription().split("\n")) {
            spellDesc.append(ChatColor.GRAY).append(line).append("\n");
        }

        String status;
        if (isUnlocked) {

            status = "&a[ Unlocked ]";
        } else {
            status = "&c[Locked] &8(&fShift + Left Click &8to unlock)";
        }

        editor.setOption(position, new ItemStack(Material.ENCHANTED_BOOK), spellName,
                status +
                        "\n\n" + spellDesc +
                        "\n&cCooldown: &e" + cooldown + "s" +
                        "\n&3Mana Cost: &f" + manaCost, 0, false);
    }

    private static void updateRuneSpell(Player pl, ItemStack item, String spellSlot, String spellName) {
        // check so players can't have two of the same spell
        String otherSpell;

        if (spellSlot.equals("primarySpell")) {
            otherSpell = AttributeUtil.getSpell(item, "secondarySpell");
        } else {
            otherSpell = AttributeUtil.getSpell(item, "primarySpell");
        }

        if (!otherSpell.equals(spellName)) {
            item = AttributeUtil.addSpell(item, spellSlot, spellName);
            LoreGenerator.generateRuneLore(item);
            pl.getInventory().setItem(1, item);
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            pl.sendMessage(ChatColor.GREEN + "You imbued your rune with " + spellName + "!");
        } else {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You can't imbue the same spell in two slots.");
        }
    }
}
