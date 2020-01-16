package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.events.LootEvent;
import com.runicrealms.plugin.item.ItemNameGenerator;
import com.runicrealms.plugin.item.util.ItemDropper;
import com.runicrealms.plugin.item.util.ItemScrapsUtil;
import com.runicrealms.plugin.item.util.ItemUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ItemCMD implements SubCommand {

    private RunicGiveSC giveItemSC;

    public ItemCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;

        // mounts
        if (args[2].equals("mount")) {
            ItemScrapsUtil.giveScrap(pl, Integer.parseInt(args[3]));
            return;
        }

        // item scraps
        if (args[2].equals("scrap")) {
            ItemScrapsUtil.giveScrap(pl, Integer.parseInt(args[3]));
            return;
        }

        // runicgive item [player] [itemType] [tier] | length = 4
        // runicgive item [player] [itemType] [tier] ([x] [y] [z]) [uuid] | length = 8
        // runicgive item [player] [potion] [type] [someVar]
        ItemNameGenerator nameGen = new ItemNameGenerator();

        String name;
        if (!args[2].equals("gemstone") && !args[2].equals("potion")) {
            name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf(args[3].toUpperCase()));
            if (name == null) return;
        }


        String itemType = args[2];

        Material material = Material.STICK;
        int durability = 0;
        Random random = new Random();
        int randomNum = random.nextInt(5) + 1;
        if (itemType.toLowerCase().equals("armor")) {
            int type = random.nextInt(4) + 1;
            switch (type) {
                case 1:
                    itemType = "helmet";
                    break;
                case 2:
                    itemType = "chestplate";
                    break;
                case 3:
                    itemType = "leggings";
                    break;
                case 4:
                    itemType = "boots";
                    break;
            }
        }
        switch (itemType.toLowerCase()) {
            case "gemstone":
                switch (randomNum) {
                    case 1:
                        material = Material.REDSTONE;
                        break;
                    case 2:
                        material = Material.LAPIS_LAZULI;
                        break;
                    case 3:
                        material = Material.EMERALD;
                        break;
                    case 4:
                        material = Material.QUARTZ;
                        break;
                    case 5:
                        material = Material.DIAMOND;
                        break;
                }
                break;
            case "potion":
                ItemStack potion = ItemUtils.generatePotion(args[3], Integer.parseInt(args[4]));
                // check that the player has an open inventory space
                // this method prevents items from stacking if the player crafts 5
                if (pl.getInventory().firstEmpty() != -1) {
                    int firstEmpty = pl.getInventory().firstEmpty();
                    pl.getInventory().setItem(firstEmpty, potion);
                } else {
                    pl.getWorld().dropItem(pl.getLocation(), potion);
                }
                return;
        }

        ItemStack craftedItem = new ItemStack(material);
        ItemMeta meta = craftedItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        craftedItem.setItemMeta(meta);

        switch (args[3].toLowerCase()) {
            case "common":
                craftedItem = ItemUtils.generateCommonItem();
                break;
            case "uncommon":
                craftedItem = ItemUtils.generateUncommonItem();
                break;
            case "rare":
                craftedItem = ItemUtils.generateRareItem();
                break;
            case "epic":
                craftedItem = ItemUtils.generateEpicItem();
                break;
        }

        // ----------------------------------------------------------------
        // run our custom event for purposes of loot bonuses, potions, etc.
        LootEvent event = new LootEvent(pl, craftedItem);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        // ----------------------------------------------------------------

        // check that the player has an open inventory space
        // this method prevents items from stacking if the player crafts 5
        // quests, or directly in inventory
        if (args.length == 4) {
            if (pl.getInventory().firstEmpty() != -1) {
                int firstEmpty = pl.getInventory().firstEmpty();
                pl.getInventory().setItem(firstEmpty, craftedItem);
            } else {
                pl.getWorld().dropItem(pl.getLocation(), craftedItem);
            }

        // mob drops
        } else if (args.length == 8) {
//            Location loc = new Location(pl.getWorld(), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]));
//            UUID mobID = UUID.fromString(args[7]);
//            if (RunicCore.getMobTagger().getIsTagged(mobID)) { // if the mob is tagged, drop a prio item
//                RunicCore.getMobTagger().dropTaggedLoot(RunicCore.getMobTagger().getTagger(mobID), loc, craftedItem);
//            } else if (RunicCore.getBossTagger().isBoss(mobID)) {
//                RunicCore.getBossTagger().dropTaggedBossLoot(mobID, loc, craftedItem); // boss loot
//            } else {
//                pl.getWorld().dropItem(loc, craftedItem); // regular drop
//            }
            ItemDropper.dropLoot(pl, args[4], args[5], args[6], args[7], craftedItem);
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if(args.length == 3 || args.length == 4 || args.length == 5 || args.length == 7) {
            this.onConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /giveitem generator [itemType] [tier] ([x] [y] [z])");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "runic.generateitem";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
