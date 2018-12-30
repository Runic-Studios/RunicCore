package us.fortherealm.plugin.professions.blacksmith;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.item.LoreGenerator;
import us.fortherealm.plugin.utilities.ArmorEnum;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ArmorGUI implements InventoryProvider {

    public static final SmartInventory CRAFT_ARMOR = SmartInventory.builder()
            .id("armorGUI")
            .provider(new ArmorGUI())
            .size(2, 9)
            .title(ChatColor.WHITE + "" + ChatColor.BOLD + "Select an Item")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // return to anvil
        contents.set(0, 4, ClickableItem.of
                (menuItem(Material.ANVIL,
                        ChatColor.YELLOW,
                        "Anvil",
                        ChatColor.GRAY + "Click an item to craft it!\n"
                                + ChatColor.DARK_GRAY + "Click here to return to the anvil"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            AnvilGUI.ANVIL_GUI.open(player);
                        }));

        // mail
        setCraftItem(player, contents, 1, 0, Material.CHAINMAIL_HELMET,
                "Forged Mithril Helmet", "Iron Link",
                "Mail", Material.IRON_BARS, 5, 5, 1);

        setCraftItem(player, contents, 1, 1, Material.CHAINMAIL_CHESTPLATE,
                "Forged Mithril Body", "Iron Link",
                "Mail", Material.IRON_BARS, 8, 5, 15);


        setCraftItem(player, contents, 1, 2, Material.CHAINMAIL_LEGGINGS,
                "Forged Mithril Legs", "Iron Link",
                "Mail", Material.IRON_BARS, 7, 5, 10);

        setCraftItem(player, contents, 1, 3, Material.CHAINMAIL_BOOTS,
                "Forged Mithril Boots", "Iron Link",
                "Mail", Material.IRON_BARS, 4, 5, 5);

        // plate
        setCraftItem(player, contents, 1, 4, Material.IRON_HELMET,
                "Forged Iron Helmet", "Iron Bar",
                "Plate", Material.IRON_INGOT, 5, 5, 20);

        setCraftItem(player, contents, 1, 5, Material.IRON_CHESTPLATE,
                "Forged Iron Platebody", "Iron Bar",
                "Plate", Material.IRON_INGOT, 8, 5, 35);

        setCraftItem(player, contents, 1, 6, Material.IRON_LEGGINGS,
                "Forged Iron Platelegs", "Iron Bar",
                "Plate", Material.IRON_INGOT, 7, 5, 30);

        setCraftItem(player, contents, 1, 7, Material.IRON_BOOTS,
                "Forged Iron Boots", "Iron Bar",
                "Plate", Material.IRON_INGOT, 4, 5, 25);
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void setCraftItem(Player pl, InventoryContents contents, int row, int slot, Material material,
                              String name, String requirements, String type,
                              Material items, int itemAmt, int exp, int reqLevel) {

        // grab the location of the anvil
        Location anvilLoc = AnvilListener.getAnvilLocation().get(pl.getUniqueId());

        // grab the player's current profession level, progress toward that level
        int currentLvl = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // different color displays, depending on whether the player has the required items
        ChatColor color = ChatColor.RED;
        if (pl.getInventory().contains(items, itemAmt)) {
            color = ChatColor.GREEN;
        }

        String description = ChatColor.RED + "Unlock by reaching lv. " + reqLevel + "!";
        if (pl.isOp() || currentLvl >= reqLevel) {
            description = ChatColor.UNDERLINE
                    + "Materials required:\n"
                    + color + requirements + ChatColor.GRAY + ", " + ChatColor.WHITE + itemAmt + "\n\n"
                    + ChatColor.UNDERLINE + "Rewards:\n"
                    + ChatColor.GOLD + "Experience" + ChatColor.GRAY + ", " + ChatColor.WHITE + exp + "\n\n"
                    + type;
        }

        contents.set(row, slot, ClickableItem.of
                (menuItem(material, ChatColor.WHITE, name, description),
                        e -> {
                            if (!(pl.isOp()) && currentLvl < reqLevel) {
                                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                                pl.sendMessage(ChatColor.RED + "You haven't learned to craft this yet!");
                            } else {
                                if (pl.getInventory().contains(items, itemAmt)) {
                                    // take player's items
                                    pl.closeInventory();
                                    // todo: put player on "currently forging" hashmap
                                    pl.sendMessage(ChatColor.GRAY + "Forging...");
                                    ItemStack regent = new ItemStack(items, itemAmt);
                                    pl.getInventory().removeItem(regent);
                                    // spawn item on anvil
                                    spawnFloatingItem(pl, anvilLoc, material, 4);
                                    // "ding, wait, ding, wait, ding"
                                    new BukkitRunnable() {
                                        int count = 0;
                                        @Override
                                        public void run() {
                                            if (count > 3) {
                                                this.cancel();
                                                pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
                                                // todo: fix this
                                                pl.sendMessage(ChatColor.GREEN + "Done! Progress towards next lv: 5%");
                                                craftArmor(pl, material, name, currentLvl, type);
                                                giveRewards(pl, exp);
                                            }
                                            pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
                                            pl.spawnParticle(Particle.FIREWORKS_SPARK, anvilLoc, 5, 0.25, 0.25, 0.25, 0.01);
                                            count = count + 1;
                                        }
                                    }.runTaskTimer(Main.getInstance(), 0, 20);
                                } else {
                                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                                    pl.sendMessage(ChatColor.RED + "You don't have the items to craft this!");
                                }
                            }
                }));
    }

    // creates the visual menu
    private ItemStack menuItem(Material material, ChatColor color, String displayName, String description) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        String[] desc = description.split("\n");
        ArrayList<String> lore = new ArrayList<>();
        for (String line : desc) {
            lore.add(ChatColor.GRAY + line);
        }
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    private void spawnFloatingItem(Player pl, Location loc, Material material, int duration) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        Vector vec = loc.toVector().multiply(0);
        item.setVelocity(vec);
        item.setPickupDelay(Integer.MAX_VALUE);

        // send packets to make item invisible for all other players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p == pl) continue;
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(item.getEntityId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }

        // tell the item when to despawn, based on duration (in seconds)
        try
        {
            Field itemField = item.getClass().getDeclaredField("item");
            Field ageField;
            Object entityItem;

            itemField.setAccessible(true);
            entityItem = itemField.get(item);

            ageField = entityItem.getClass().getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(entityItem, 6000 - (20 * duration));
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void craftArmor(Player pl, Material material, String dispName, int currentLvl, String type) {

        ItemStack craftedItem = new ItemStack(material);

        String itemSlot = "";
        ArmorEnum armorType = ArmorEnum.matchSlot(craftedItem);
        switch (armorType) {
            case HELMET:
                itemSlot = "head";
                break;
            case CHESTPLATE:
                itemSlot = "chest";
                break;
            case LEGGINGS:
                itemSlot = "legs";
                break;
            case BOOTS:
                itemSlot = "feet";
                break;
        }

        craftedItem = AttributeUtil.addGenericStat
                (craftedItem, "generic.maxHealth", 5+currentLvl, itemSlot);

        LoreGenerator.generateItemLore(craftedItem, ChatColor.WHITE, dispName, "Crafted", type);
        pl.getInventory().addItem(craftedItem);
    }

    private void giveRewards(Player pl, int exp) {
        // give player new item, exp
        int currentExp = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.exp");
        Main.getInstance().getConfig().set(pl.getUniqueId() + ".info.prof.exp", currentExp + exp);
        pl.giveExp(exp);
    }
}
