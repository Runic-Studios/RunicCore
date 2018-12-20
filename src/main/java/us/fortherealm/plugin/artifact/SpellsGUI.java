package us.fortherealm.plugin.artifact;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;

import java.util.ArrayList;

public class SpellsGUI implements InventoryProvider {

    /**
     * CHANGE THE ID FOR EACH NEW GUI
     */
    public static final SmartInventory ARTIFACT_SPELLS = SmartInventory.builder()
            .id("artifactSpells")
            .provider(new SpellsGUI())
            .size(2, 9)
            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Available Spells")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // determine the player's class
        String className = Main.getInstance().getConfig().get(player.getUniqueId() + ".info.class").toString();

        // grab player's artifact
        ItemStack artifact = player.getInventory().getItem(0);
        ItemMeta meta = artifact.getItemMeta();

        // build the menu description, updates live with their current skills
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
        contents.set(0, 4, ClickableItem.of
                (menuItem(artifact.getType(),
                        ChatColor.YELLOW,
                        meta.getDisplayName(),
                        desc,
                        ((Damageable) meta).getDamage()),
                        e -> ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player)));

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
        displaySpell(player, contents, 1, 0, "Barrage", "Archer");
        displaySpell(player, contents, 1, 1, "Grapple", "Archer");
        displaySpell(player, contents, 1, 2, "Parry", "Archer");
    }

    private void displaySpellsCleric(Player player, InventoryContents contents) {
        //displaySpell(player, contents, 1, 0, "Lightwell", "Cleric");
        displaySpell(player, contents, 1, 1, "Rejuvenate", "Cleric");
        displaySpell(player, contents, 1, 2, "Windstride", "Cleric");
    }

    private void displaySpellsMage(Player player, InventoryContents contents) {
        displaySpell(player, contents, 1, 0, "Arcane Spike", "Mage");
        displaySpell(player, contents, 1, 1, "Comet", "Mage");
        displaySpell(player, contents, 1, 2, "Discharge", "Mage");
    }

    private void displaySpellsRogue(Player player, InventoryContents contents) {
        displaySpell(player, contents, 1, 0, "Backstab", "Rogue");
        //displaySpell(player, contents, 1, 1, "Cloak", "Rogue");
        displaySpell(player, contents, 1, 2, "Smoke Bomb", "Rogue");
    }

    private void displaySpellsWarrior(Player player, InventoryContents contents) {
        //displaySpell(player, contents, 1, 0, "Charge", "Warrior");
        displaySpell(player, contents, 1, 1, "Enrage", "Warrior");
        displaySpell(player, contents, 1, 2, "Deliverance", "Warrior");
    }

    // display for each skin
    private void displaySpell(Player player, InventoryContents contents, int row, int slot, String spellName, String className) {

        String[] desc = Main.getSkillManager().getSkillByName(spellName).getDescription().split("\n");
        ItemStack item = player.getInventory().getItem(0);
        String itemName = item.getItemMeta().getDisplayName();
        if (player.hasPermission("ftr.spells." + spellName)) {
            contents.set(row, slot, ClickableItem.of
                    (spellMenuItem(Material.ENCHANTED_BOOK,
                            ChatColor.GREEN,
                            spellName, desc),
                            e -> {
                                // todo: prevent having the same spell in the same slot
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
    private void updateArtifactSpell(Player player, ItemStack item, String spellSlot, String spellName, String itemName, String className) {
        item = AttributeUtil.addSpell(item, spellSlot, spellName);
        LoreGenerator.generateArtifactLore(item, ChatColor.YELLOW, itemName, className, 0);
        player.getInventory().setItem(0, item);
        SpellsGUI.ARTIFACT_SPELLS.open(player);
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor dispColor, String displayName, ArrayList<String> desc, int durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(dispColor + displayName);
        ArrayList<String> lore = new ArrayList<>();
        for (String s : desc) {
            lore.add(ChatColor.GRAY + s);
        }
        meta.setLore(lore);
        ((Damageable) meta).setDamage(durability);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    // creates the visual menu
    private ItemStack spellMenuItem(Material material, ChatColor dispColor, String displayName, String[] desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(dispColor + displayName);
        ArrayList<String> lore = new ArrayList<>();
        for (String s : desc) {
            lore.add(ChatColor.GRAY + s);
        }
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}
