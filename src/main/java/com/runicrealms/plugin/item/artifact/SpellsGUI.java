package com.runicrealms.plugin.item.artifact;

import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.item.rune.RuneGUI;
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

    // todo: add variable in each skill which corresponds to a class to make adding this easier
    // for (Spell skill : spells) {
    // if (skill.isMageSkill)... etc
    // private List<Spell> spells = RunicCore.getSkillManager().getSkills();

    /**
     * CHANGE THE ID FOR EACH NEW ItemGUI
     */
    public static final SmartInventory ARTIFACT_SPELLS = SmartInventory.builder()
            .id("artifactSpells")
            .provider(new SpellsGUI())
            .size(4, 9)
            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Available Spells")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // determine the player's class
        String className = RunicCore.getInstance().getConfig().get(player.getUniqueId() + ".info.class.name").toString();

        // grab player's artifact
        ItemStack artifact = player.getInventory().getItem(0);
        ItemMeta meta = artifact.getItemMeta();

        // build the menu description, updates live with their current spells
        String primarySpell = AttributeUtil.getSpell(artifact, "primarySpell");
        String secondarySpell = AttributeUtil.getSpell(artifact, "secondarySpell");
        ArrayList<String> desc = new ArrayList<>();
        desc.add("");
        desc.add(ChatColor.GRAY + "Spells:");
        desc.add(ChatColor.WHITE + "Primary: " + ChatColor.GREEN + primarySpell);
        desc.add(ChatColor.WHITE + "Secondary: " + ChatColor.GREEN + secondarySpell);
        desc.add("");
        desc.add("Left click a spell to set your primary!");
        desc.add("Right click a spell to set your secondary!");
        desc.add(ChatColor.DARK_GRAY + "Click here to return to the editor");

        // build the menu item
        contents.set(1, 3, ClickableItem.of
                (menuItem(artifact.getType(),
                        ChatColor.YELLOW,
                        meta.getDisplayName(),
                        desc,
                        ((Damageable) meta).getDamage(), 1),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player);
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
                        ChatColor.BOLD + "Spell Points: " + skillpoints,
                        spDesc, 0, skillpoints), // read config for amount
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            RuneGUI.CUSTOMIZE_RUNE.open(player);
                        }));

        // artifact spell displays are class-specific
        switch (className) {
            case "Archer":
                displaySpellsArcher(player, contents);
                break;
            case "Cleric":
                displaySpellsCleric(player, contents);
                break;
            case "Mage":
                displaySpellsMage(player, contents);
                break;
            case "Rogue":
                displaySpellsRogue(player, contents);
                break;
            case "Warrior":
                displaySpellsWarrior(player, contents);
                break;
        }
    }

    private void displaySpellsArcher(Player player, InventoryContents contents) {
        displaySpell(player, contents, 2, 3, "Barrage", "Archer", true);
        displaySpell(player, contents, 2, 4, "Grapple", "Archer", false);
        displaySpell(player, contents, 2, 5, "Parry", "Archer", false);
    }

    private void displaySpellsCleric(Player player, InventoryContents contents) {
        displaySpell(player, contents, 2, 3, "Holy Nova", "Cleric", true);
        displaySpell(player, contents, 2, 4, "Rejuvenate", "Cleric", true);
        displaySpell(player, contents, 2, 5, "Windstride", "Cleric", false);
    }

    private void displaySpellsMage(Player player, InventoryContents contents) {
        displaySpell(player, contents, 2, 2, "Arcane Spike", "Mage", false);
        displaySpell(player, contents, 2, 3, "Blizzard", "Mage", true);
        displaySpell(player, contents, 2, 4, "Comet", "Mage", false);
        displaySpell(player, contents, 2, 5, "Discharge", "Mage", false);
    }

    private void displaySpellsRogue(Player player, InventoryContents contents) {
        displaySpell(player, contents, 2, 3, "Backstab", "Rogue", false);
        displaySpell(player, contents, 2, 4, "Cloak", "Rogue", false);
        displaySpell(player, contents, 2, 5, "Smoke Bomb", "Rogue", true);
    }

    private void displaySpellsWarrior(Player player, InventoryContents contents) {
        displaySpell(player, contents, 2, 3, "Charge", "Warrior", true);
        displaySpell(player, contents, 2, 4, "Enrage", "Warrior", false);
        displaySpell(player, contents, 2, 5, "Judgment", "Warrior", false);
    }

    // display for each skin
    private void displaySpell(Player player, InventoryContents contents,
                              int row, int slot, String spellName, String className, boolean isUnlocked) {

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

        ItemStack item = player.getInventory().getItem(0);
        String itemName = item.getItemMeta().getDisplayName();
        if (player.hasPermission("ftr.spells." + spellName) || isUnlocked) {
            contents.set(row, slot, ClickableItem.of
                    (spellMenuItem(Material.ENCHANTED_BOOK,
                            ChatColor.GREEN,
                            spellName, desc),
                            e -> {
                                if (e.isLeftClick()) {
                                    updateArtifactSpell(player, item, "primarySpell", spellName, itemName, className);
                                } else {
                                    updateArtifactSpell(player, item, "secondarySpell", spellName, itemName, className);
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
    private void updateArtifactSpell(Player pl, ItemStack item, String spellSlot, String spellName, String itemName, String className) {
        // check so players can't have two of the same spell
        String otherSpell = "";

        if (spellSlot.equals("primarySpell")) {
            otherSpell = AttributeUtil.getSpell(item, "secondarySpell");
        } else {
            otherSpell = AttributeUtil.getSpell(item, "primarySpell");
        }

        if (!otherSpell.equals(spellName)) {
            item = AttributeUtil.addSpell(item, spellSlot, spellName);
            int durability = ((Damageable) item.getItemMeta()).getDamage();
            LoreGenerator.generateArtifactLore(item, itemName, className, durability);
            pl.getInventory().setItem(0, item);
            SpellsGUI.ARTIFACT_SPELLS.open(pl);
            pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            pl.sendMessage(ChatColor.GREEN + "You imbued your artifact with " + spellName + "!");
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
