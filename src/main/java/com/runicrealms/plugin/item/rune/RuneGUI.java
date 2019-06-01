package com.runicrealms.plugin.item.rune;

import com.runicrealms.plugin.item.OptionClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.utilities.ColorUtil;

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
                        "&fClick &7to customize your runic abilities", 0)
                .setOption(5+9, new ItemStack(Material.BARRIER), "&cClose",
                        "&7Exit the editor", 0);
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
                        pl.closeInventory();
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(ChatColor.RED + "You haven't unlocked that spell yet.");
                        return;
                    }

                    updateRuneSpell(pl, rune, "primarySpell",
                            event.getInventory().getItem(event.getSlot()).getItemMeta().getDisplayName());

                } else if (event.getClick() == ClickType.RIGHT) {

                    if (!pl.hasPermission("core.spells." + spellName)) {
                        pl.closeInventory();
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(ChatColor.RED + "You haven't unlocked that spell yet.");
                        return;
                    }

                    updateRuneSpell(pl, rune, "secondarySpell",
                            event.getInventory().getItem(event.getSlot()).getItemMeta().getDisplayName());

                } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {

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
                        "\n&fShift + Click &7a spell to set your secondary!" +
                        "\n&fClick here &7to return to the editor", ((Damageable) meta).getDamage());

        int numPoints = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.spellpoints");
        if (numPoints == 0) {
            numPoints = 1;
        }
        spellEditor.setOption(14, new ItemStack(Material.BONE_MEAL, numPoints), "&f&lSpell Points",
                "\n&7Use spell points to unlock new spells!" +
                        "\n&aEarn spell points by completing quests" +
                        "\n&aand leveling-up!", 0);

        List<String> spells = new ArrayList<>();

        spells.add("Arcane Orb");
        spells.add("Blessing");
        spells.add("Blink");
        spells.add("Fire Aura");
        spells.add("Fireball");
        spells.add("Frostbolt");
        spells.add("Harpoon");
        spells.add("Lunge");
        spells.add("Permafrost");
        spells.add("Petrify");
        spells.add("Reflect");
        spells.add("Siphon");
        spells.add("Sprint");
        spells.add("Taunt");
        spells.add("Warsong");

        // first row of spells
        for (int i = 0; i < spells.size() && i < 5; i++) {

            // check for permissions, ex: ftr.spells.blessedrain
            boolean unlocked= false;
            if (pl.hasPermission("core.spells." + spells.get(i).replace(" ", "").toLowerCase())) {
                unlocked = true;
            }
            displaySpell(spellEditor, 20 + i, spells.get(i), unlocked);
        }

        // second row of spells
        for (int i = 5; i < spells.size() && i < 10; i++) {

            boolean unlocked= false;
            if (pl.hasPermission("core.spells." + spells.get(i).replace(" ", "").toLowerCase())) {
                unlocked = true;
            }
            displaySpell(spellEditor, 24 + i, spells.get(i), unlocked);
        }

        // third row of spells
        for (int i = 10; i < spells.size() && i < 15; i++) {

            boolean unlocked= false;
            if (pl.hasPermission("core.spells." + spells.get(i).replace(" ", "").toLowerCase())) {
                unlocked = true;
            }
            displaySpell(spellEditor, 28 + i, spells.get(i), unlocked);
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
            status = "&c[Locked] &8(&fShift + Click &8to unlock)";
        }

        editor.setOption(position, new ItemStack(Material.ENCHANTED_BOOK), spellName,
                status +
                        "\n\n" + spellDesc +
                        "\n&cCooldown: &e" + cooldown + "s" +
                        "\n&3Mana Cost: &f" + manaCost, 0);
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
            int durability = ((Damageable) item.getItemMeta()).getDamage();
            LoreGenerator.generateRuneLore(item);
            pl.getInventory().setItem(1, item);
            pl.closeInventory();
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            pl.sendMessage(ChatColor.GREEN + "You imbued your rune with " + spellName + "!");
        } else {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You can't imbue the same spell in two slots.");
        }
    }

    public static ItemStack menuItem(Material material, String name, String desc,
                                     int durability) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(desc);

        if (meta != null) {
            meta.setLore(lore);
            meta.setDisplayName(ColorUtil.format("&e" + name));
            ((Damageable) meta).setDamage(durability);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }
}
