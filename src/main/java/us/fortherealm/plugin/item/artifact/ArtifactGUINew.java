package us.fortherealm.plugin.item.artifact;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.item.ItemGUI;
import us.fortherealm.plugin.utilities.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArtifactGUINew {

    /**
     * Opens the artifact editor using new instance of inventory
     */
    public static ItemGUI artifactEditor(Player pl, ItemStack artifact, int durability) {

        return new ItemGUI("&f&l" + pl.getName() + "'s &e&lArtifact Editor", 27, event -> {

            // open skin editor
            if (event.getPosition() == 2+9) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI skinEditor = ArtifactGUINew.skinEditor(pl, artifact, durability);
                skinEditor.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else if (event.getPosition() == 4+9) {

                // open spell editor
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(false);

            } else if (event.getPosition() == 6+9) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }

        // setup items
        }, FTRCore.getInstance())
                .setOption(2+9, new ItemStack(artifact.getType()), "&eSkin Editor",
                        "&fClick &7to customize your artifact skin", durability)
                .setOption(4+9, new ItemStack(Material.FIRE_CHARGE), "&aSpell Editor",
                        "&fClick &7to customize your artifact abilities", 0)
                .setOption(6+9, new ItemStack(Material.BARRIER), "&cClose",
                        "&7Exit the editor", 0);
    }

    private static ItemGUI skinEditor(Player pl, ItemStack artifact, int durability) {

        // determine the player's class
        String className = Objects.requireNonNull(FTRCore.getInstance()
                .getConfig().get(pl.getUniqueId() + ".info.class.name")).toString();

        // grab player's artifact
        ItemMeta meta = artifact.getItemMeta();

        List<ItemStack> skins = new ArrayList<>();

        // skin displays are class-specific
        switch (className) {
            case "Archer":
               skins = displaySkinsArcher();
                break;
            case "Cleric":
                skins = displaySkinsCleric();
                break;
            case "Mage":
                skins = displaySkinsMage();
                break;
            case "Rogue":
                skins = displaySkinsRogue();
                break;
            case "Warrior":
                skins = displaySkinsWarrior();
                break;
        }

        int size;
        if (skins.size() <= 5) {
            size = 36;
        } else {
            size = 45;
        }

        ItemGUI skinEditor = new ItemGUI("&f&l" + pl.getName() + "'s &e&lSkin Editor", size, event -> {

            if (event.getPosition() == 13) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI menu = ArtifactGUINew.artifactEditor(pl, artifact, durability);
                menu.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);
            } else {

                // apply the skin if the player has the permission
                ItemStack skin = event.getItem(event.getPosition());
                applySkin(pl, className, event.getPosition(),
                        skin.getType(), skin.getItemMeta().getDisplayName(), ((Damageable) skin.getItemMeta()).getDamage());
                event.setWillClose(true);
                event.setWillDestroy(true);
            }

        }, FTRCore.getInstance());

        skinEditor.setOption(13, new ItemStack(artifact.getType()), artifact.getItemMeta().getDisplayName(),
                "&fClick &7an appearance to change your skin!\n&fClick here &7to return to the editor", ((Damageable) meta).getDamage());

        // first row of skins
        for (int i = 0; i < skins.size() && i < 5; i++) {

            // check for permissions, based on position. So a wood staff needs:
            // ftr.skins.mage.20
            String desc;
            if (!pl.hasPermission("ftr.skins." + className + (20+i)) && i != 0) {
                String desc2 = skins.get(i).getItemMeta().getLore().get(0).split("\n")[1];
                desc = "&cYou haven't unlocked this appearance yet" + "\n" + desc2;
            } else {
                desc = skins.get(i).getItemMeta().getLore().get(0).split("\n")[0];//skins.get(i).getItemMeta().getLore().get(0)
            }
            skinEditor.setOption(20 + i, skins.get(i),
                    skins.get(i).getItemMeta().getDisplayName(),
                    desc, ((Damageable) skins.get(i).getItemMeta()).getDamage());
        }

        // second row of skins
        for (int j = 5; j < skins.size(); j++) {

            String desc;
            if (!pl.hasPermission("ftr.skins." + className + (29+(j-5)))) {
                String desc2 = skins.get(j).getItemMeta().getLore().get(0).split("\n")[1];
                desc = "&cYou haven't unlocked this appearance yet" + "\n" + desc2;
            } else {
                desc = skins.get(j).getItemMeta().getLore().get(0).split("\n")[0];//skins.get(i).getItemMeta().getLore().get(0)
            }
            skinEditor.setOption(29 + (j-5), skins.get(j),
                    skins.get(j).getItemMeta().getDisplayName(),
                    desc, ((Damageable) skins.get(j).getItemMeta()).getDamage());
        }

        return skinEditor;
    }

    private static List<ItemStack> displaySkinsArcher() {
        List<ItemStack> archerSkins = new ArrayList<>();
        archerSkins.add(menuItem(Material.BOW, "Stiff Oaken Shortbow", "&aUnlocked!", 0));
        archerSkins.add(menuItem(Material.BOW, "Worn Stone Shortbow", "&aUnlocked!\n&7Unlock by reaching lv. 10!", 5));
        archerSkins.add(menuItem(Material.BOW, "Polished Silver Shortbow", "&aUnlocked!\n&7Unlock by reaching lv. 20!", 10));
        archerSkins.add(menuItem(Material.BOW, "Victorious Gilded Shortbow", "&aUnlocked!\n&7Unlock by reaching lv. 30!", 15));
        archerSkins.add(menuItem(Material.BOW, "Ancient Crystal Shortbow", "&aUnlocked!\n&7Unlock by reaching lv. 40!", 20));
        return archerSkins;
    }

    private static List<ItemStack> displaySkinsCleric() {
        List<ItemStack> clericSkins = new ArrayList<>();
        clericSkins.add(menuItem(Material.WOODEN_SHOVEL, "Initiate's Oaken Mace", "&aUnlocked!", 0));
        clericSkins.add(menuItem(Material.WOODEN_SHOVEL, "Worn Stone Club", "&aUnlocked!\n&7Unlock by reaching lv. 10!", 1));
        clericSkins.add(menuItem(Material.WOODEN_SHOVEL, "Polished Silver Hammer", "&aUnlocked!\n&7Unlock by reaching lv. 20!", 2));
        clericSkins.add(menuItem(Material.WOODEN_SHOVEL, "Victorious Gilded Mace", "&aUnlocked!\n&7Unlock by reaching lv. 30!", 3));
        clericSkins.add(menuItem(Material.WOODEN_SHOVEL, "Ancient Crystal Maul", "&aUnlocked!\n&7Unlock by reaching lv. 40!", 4));
        return clericSkins;
    }

    private static List<ItemStack> displaySkinsMage() {
        List<ItemStack> mageSkins = new ArrayList<>();
        mageSkins.add(menuItem(Material.WOODEN_HOE, "Sturdy Oaken Branch", "&aUnlocked!", 0));
        mageSkins.add(menuItem(Material.WOODEN_HOE, "Worn Stone Cane", "&aUnlocked!\n&7Unlock by reaching lv. 10!", 1));
        mageSkins.add(menuItem(Material.WOODEN_HOE, "Polished Silver Scepter", "&aUnlocked!\n&7Unlock by reaching lv. 20!", 2));
        mageSkins.add(menuItem(Material.WOODEN_HOE, "Victorious Gilded Staff", "&aUnlocked!\n&7Unlock by reaching lv. 30!", 3));
        mageSkins.add(menuItem(Material.WOODEN_HOE, "Ancient Crystal Greatstaff", "&aUnlocked!\n&7Unlock by reaching lv. 40!", 4));
        mageSkins.add(menuItem(Material.WOODEN_HOE, "Primal Arcane Rod", "&aUnlocked!\n&7Unlocked by purchasing the Alpha+ rank!", 5));
        return mageSkins;
    }

    private static List<ItemStack> displaySkinsRogue() {
        List<ItemStack> rogueSkins = new ArrayList<>();
        rogueSkins.add(menuItem(Material.WOODEN_SWORD, "Oaken Sparring Sword", "&aUnlocked!", 0));
        rogueSkins.add(menuItem(Material.WOODEN_SWORD, "Worn Stone Sword", "&aUnlocked!\n&7Unlock by reaching lv. 10!", 1));
        rogueSkins.add(menuItem(Material.WOODEN_SWORD, "Polished Silver Broadsword", "&aUnlocked!\n&7Unlock by reaching lv. 20!", 2));
        rogueSkins.add(menuItem(Material.WOODEN_SWORD, "Victorious Gilded Longsword", "&aUnlocked!\n&7Unlock by reaching lv. 30!", 3));
        rogueSkins.add(menuItem(Material.WOODEN_SWORD, "Ancient Crystal Greatsword", "&aUnlocked!\n&7Unlock by reaching lv. 40!", 4));
        return rogueSkins;
    }

    private static List<ItemStack> displaySkinsWarrior() {
        List<ItemStack> warriorSkins = new ArrayList<>();
        warriorSkins.add(menuItem(Material.WOODEN_AXE, "Worn Oaken Battleaxe", "&aUnlocked!", 0));
        warriorSkins.add(menuItem(Material.WOODEN_AXE, "Crumbling Stone Axe", "&aUnlocked!\n&7Unlock by reaching lv. 10!", 1));
        warriorSkins.add(menuItem(Material.WOODEN_AXE, "Polished Silver Broadaxe", "&aUnlocked!\n&7Unlock by reaching lv. 20!", 2));
        warriorSkins.add(menuItem(Material.WOODEN_AXE, "Victorious Gilded Reaver", "&aUnlocked!\n&7Unlock by reaching lv. 30!", 3));
        warriorSkins.add(menuItem(Material.WOODEN_AXE, "Ancient Crystal Battleaxe", "&aUnlocked!\n&7Unlock by reaching lv. 40!", 4));
        warriorSkins.add(menuItem(Material.WOODEN_AXE, "Ancient Crystal Battleaxe", "&aUnlocked!\n&7Unlock by achieving True Outlaw Season 1!", 5));
        return warriorSkins;
    }

    // transforms the player's artifact skin
    private static void applySkin(Player pl, String className, int position,
                                  Material material, String name, int durab) {

        if (!pl.hasPermission("ftr.skins." + className + position) && position != 20) {
            pl.sendMessage(ColorUtil.format("&cYou haven't unlocked this appearance yet!"));
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            return;
        }

        // grab the player's artifact
        ItemStack artifact = pl.getInventory().getItem(0);

        // update the display material, name, and durability of the artifact
        assert artifact != null;
        ItemMeta meta = artifact.getItemMeta();
        artifact.setType(material);
        assert meta != null;
        meta.setDisplayName(ChatColor.YELLOW + name);
        ((Damageable) meta).setDamage(durab);
        artifact.setItemMeta(meta);

        // update the artifact!
        pl.getInventory().setItem(0, artifact);
        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        pl.sendMessage(ChatColor.GREEN + "Enjoy your new skin!");
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
