//package us.fortherealm.plugin.artifact;
//
//import fr.minuskube.inv.ClickableItem;
//import fr.minuskube.inv.SmartInventory;
//import fr.minuskube.inv.content.InventoryContents;
//import fr.minuskube.inv.content.InventoryProvider;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemFlag;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.Damageable;
//import org.bukkit.inventory.meta.ItemMeta;
//import us.fortherealm.plugin.Main;
//
//import java.util.ArrayList;
//
//public class SpellsGUI implements InventoryProvider {
//
//    /**
//     * CHANGE THE ID FOR EACH NEW GUI
//     */
//    public static final SmartInventory ARTIFACT_SPELLS = SmartInventory.builder()
//            .id("artifactSpells")
//            .provider(new SkinsGUI())
//            .size(2, 9)
//            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Available Spells")
//            .build();
//
//    @Override
//    public void init(Player player, InventoryContents contents) {
//
//        // determine the player's class
//        String className = Main.getInstance().getConfig().get(player.getUniqueId() + ".info.class").toString();
//
//        // grab player's artifact
//        ItemStack artifact = player.getInventory().getItem(0);
//        ItemMeta meta = artifact.getItemMeta();
//
//        // skin artifact
//        contents.set(0, 4, ClickableItem.of
//                (menuItem(artifact.getType(),
//                        ChatColor.YELLOW,
//                        meta.getDisplayName(),
//                        "Click an appearance to change your skin!",
//                        "Click here to return to the artifact",
//                        ((Damageable) meta).getDamage()),
//                        e -> {
//                            ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player);
//                        }));
//
//        // skin displays are class-specific
////        switch (className) {
////            case "Archer":
////                displaySkinsArcher(player, contents);
////                break;
////            case "Cleric":
////                displaySkinsCleric(player, contents);
////                break;
////            case "Mage":
////                displaySkinsMage(player, contents);
////                break;
////            case "Rogue":
////                displaySkinsRogue(player, contents);
////                break;
////            case "Warrior":
////                displaySkinsWarrior(player, contents);
////                break;
////        }
//    }
//
//    // used for animated inventories
//    @Override
//    public void update(Player player, InventoryContents contents) {
//    }
//
//    // creates the visual menu
//    private ItemStack menuItem(Material material, ChatColor dispColor, String displayName, String desc, String desc2, int durability) {
//        ItemStack item = new ItemStack(material);
//        ItemMeta meta = item.getItemMeta();
//        meta.setDisplayName(dispColor + displayName);
//        ArrayList<String> lore = new ArrayList<>();
//        lore.add(ChatColor.GRAY + desc);
//        if (!desc2.equals("")) lore.add(ChatColor.DARK_GRAY + desc2);
//        meta.setLore(lore);
//        ((Damageable) meta).setDamage(durability);
//        meta.setUnbreakable(true);
//        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
//        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
//        item.setItemMeta(meta);
//        return item;
//    }
