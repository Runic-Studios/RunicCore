package com.runicrealms.plugin.professions.jeweler;

import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.professions.WorkstationListener;
import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.GUIItem;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;

import java.util.concurrent.ThreadLocalRandom;

public class GemGUI implements InventoryProvider {

    public static final SmartInventory CUT_GEMS = SmartInventory.builder()
            .id("gemGUI")
            .provider(new GemGUI())
            .size(2, 9)
            .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Select an Item")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // return to bench
        contents.set(0, 4, ClickableItem.of
                (GUIItem.dispItem(Material.COBBLESTONE_STAIRS,
                        ChatColor.YELLOW,
                        "Gemcutting Bench",
                        ChatColor.GRAY + "Click an item to craft it!\n"
                                + ChatColor.DARK_GRAY + "Click here to return to the bench"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            BenchGUI.BENCH_GUI.open(player);
                        }));

        // ruby (+hp)
        setCraftItem(player, contents, 1, 0, Material.REDSTONE,
                "Cut Ruby", "Uncut Ruby", ChatColor.RED + "+❤ (Health)",
                Material.REDSTONE_ORE, 1, 5, 1, "ruby");

        // sapphire (+mana)
        setCraftItem(player, contents, 1, 1, Material.LAPIS_LAZULI,
                "Cut Sapphire", "Uncut Sapphire", ChatColor.DARK_AQUA + "+✸ (Mana)",
                Material.LAPIS_ORE, 1, 10, 10, "sapphire");

        // opal (+weapon damage)
        setCraftItem(player, contents, 1, 2, Material.QUARTZ,
                "Cut Opal", "Uncut Opal", ChatColor.RED + "+⚔ (Weapon Damage)",
                Material.NETHER_QUARTZ_ORE, 1, 20, 20, "opal");

        // emerald (+healing)
        setCraftItem(player, contents, 1, 3, Material.EMERALD,
                "Cut Emerald", "Uncut Emerald", ChatColor.GREEN + "+✦ (Healing)",
                Material.EMERALD_ORE, 1, 15, 30, "emerald");

        // diamond (+spell dmg)
        setCraftItem(player, contents, 1, 4, Material.DIAMOND,
                "Cut Diamond", "Uncut Diamond", ChatColor.DARK_AQUA + "+ʔ (Spell Damage)",
                Material.DIAMOND_ORE, 1, 15, 40, "diamond");
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void setCraftItem(Player pl, InventoryContents contents, int row, int slot, Material craftedItem,
                              String name, String requirements, String gemDesc,
                              Material reagent, int itemAmt, int exp, int reqLevel, String gemType) {

        // grab the location of the anvil
        Location stationLoc = WorkstationListener.getStationLocation().get(pl.getUniqueId());

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // determine the success rate, based on level
        int rate = (25+currentLvl);
        String rateToStr;
        if (rate < 45) {
            rateToStr = ChatColor.RED + "" + rate;
        } else if (rate < 65) {
            rateToStr = ChatColor.YELLOW + "" + rate;
        } else {
            rateToStr = ChatColor.GREEN + "" + rate;
        }

        // different color displays, depending on whether the player has the required items
        ChatColor color = ChatColor.RED;
        if (pl.getInventory().contains(reagent, itemAmt)) {
            color = ChatColor.GREEN;
        }

        String description = ChatColor.RED + "Unlock by reaching lv. " + reqLevel + "!";
        if (pl.isOp() || currentLvl >= reqLevel) {
            description = "\n"
                    + ChatColor.GOLD + gemDesc + "\n\n"
                    + "Materials required:\n"
                    + color + requirements + ChatColor.GRAY + ", " + ChatColor.WHITE + itemAmt + "\n\n"
                    + "Success Rate:\n" + rateToStr + "%\n\n"
                    + ChatColor.WHITE + "Left Click " + ChatColor.DARK_GRAY + "to craft\n"
                    + ChatColor.WHITE + "Right Click " + ChatColor.DARK_GRAY + "to craft 5\n\n"
                    + ChatColor.ITALIC + "Rewards "
                    + ChatColor.WHITE + ChatColor.ITALIC + exp
                    + ChatColor.GRAY + ChatColor.ITALIC + " Experience";
        }

        contents.set(row, slot, ClickableItem.of
                (GUIItem.dispItem(craftedItem, ChatColor.GREEN, name, description),
                        e -> {

                            // check that the player has reached the req. lv
                            if (!(pl.isOp()) && currentLvl < reqLevel) {
                                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                                pl.sendMessage(ChatColor.RED + "You haven't learned to craft this yet!");
                                return;
                            }

                            // make 5 if right click
                            if (e.isRightClick()) {
                                startCrafting(pl, reagent, itemAmt*5, stationLoc,
                                        craftedItem, name, currentLvl,exp*5, 5, rate, gemType);
                            } else {
                                startCrafting(pl, reagent, itemAmt, stationLoc,
                                        craftedItem, name, currentLvl, exp, 1, rate, gemType);
                            }
                        }));
    }

    private void startCrafting(Player pl, Material reagent, int reagentAmt, Location stationLoc, Material craftedItem,
                               String name, int currentLvl, int exp, int craftedAmt, int rate, String gemType) {

        // check that the player has the items.
        if (!pl.getInventory().contains(reagent, reagentAmt)) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You don't have the items to craft this!");
            return;
        }

        // check that the player has an open inventory space
        if (pl.getInventory().firstEmpty() == -1 && craftedItem.getMaxStackSize() == 1) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You don't have any inventory space!");
            return;
        }

        // take player's items, add player to currently crafting ArrayList
        pl.closeInventory();
        RunicCore.getProfManager().getCurrentCrafters().add(pl);
        pl.sendMessage(ChatColor.GRAY + "Cutting...");
        ItemStack[] inv = pl.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            if (pl.getInventory().getItem(i) == null) continue;
            if (pl.getInventory().getItem(i).getType() == reagent) {
                pl.getInventory().getItem(i).setAmount(pl.getInventory().getItem(i).getAmount() - reagentAmt);
                break;
            }
        }

        // spawn item on anvil for visual
        FloatingItemUtil.spawnFloatingItem(pl, stationLoc, craftedItem, 4);

        // start the crafting process
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count > 3) {
                    this.cancel();
                    RunicCore.getProfManager().getCurrentCrafters().remove(pl);
                    pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.GREEN + "Done!");
                    ProfExpUtil.giveExperience(pl, exp);
                    cutGem(pl, craftedItem, name, currentLvl, craftedAmt, rate, gemType);
                } else {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
                    pl.spawnParticle(Particle.FIREWORKS_SPARK, stationLoc, 5, 0.25, 0.25, 0.25, 0.01);
                    count = count + 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    private void cutGem(Player pl, Material material, String dispName, int currentLvl, int amt, int rate, String gemType) {

        // check that the player has an open inventory space
        int failCount = 0;
        for (int i = 0; i < amt; i++) {

            // build our item
            ItemStack craftedItem = new ItemStack(material);

            // roll chance for each item
            double chance = ThreadLocalRandom.current().nextDouble(0, 100);

            // tell the game that this is a gemstone
            craftedItem = AttributeUtil.addCustomStat
                    (craftedItem, "custom.isGemstone", "true");

            craftedItem = addGemStat(gemType, currentLvl, craftedItem);

            LoreGenerator.generateItemLore(craftedItem, ChatColor.WHITE, dispName,
                    "\n" + ChatColor.DARK_GRAY + "Use this on an item");

            if (chance <= rate) {
                if (pl.getInventory().firstEmpty() != -1) {
                    int firstEmpty = pl.getInventory().firstEmpty();
                    pl.getInventory().setItem(firstEmpty, craftedItem);
                } else {
                    pl.getWorld().dropItem(pl.getLocation(), craftedItem);
                }
            } else {
                failCount = failCount + 1;
            }
        }

        // display fail message
        if (failCount == 0) return;
        pl.sendMessage(ChatColor.RED + "You fail to craft this item. [x" + failCount + "]");
    }

    private ItemStack addGemStat(String gemType, int currentLvl, ItemStack craftedItem) {
        switch (gemType) {
            case "ruby":
                // item will have a random health value that increases w/ prof lv (max of +26)
                int minHP = 1;
                int maxHP;
                if (currentLvl != 1) {
                    maxHP = (int) (0.5 * currentLvl) + 1;
                } else {
                    maxHP = 2;
                }
                int rangeR = ThreadLocalRandom.current().nextInt(minHP, maxHP + 1);
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.maxHealth", rangeR);
                break;
            case "sapphire":
                // item will have a random mana value that increases w/ prof lv (max of +26)
                int minMana = 1;
                int maxMana;
                if (currentLvl != 1) {
                    maxMana = (int) (0.5 * currentLvl) + 1;
                } else {
                    maxMana = 2;
                }
                int rangeS = ThreadLocalRandom.current().nextInt(minMana, maxMana + 1);
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.manaBoost", rangeS);
                break;
            case "opal":
                // item will have a random physical damage value that increases w/ lvl (max of +5)
                int minDmg = 1;
                int maxDmg;
                if (currentLvl != 1) {
                    maxDmg = (int) (0.1 * currentLvl) + 1;
                } else {
                    maxDmg = (int) 1.1;
                }
                int rangeO = ThreadLocalRandom.current().nextInt(minDmg, maxDmg + 1);
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.attackDamage", rangeO);
                break;
            case "emerald":
                // item will have a random healing value that increases w/ lvl (max of +5)
                int minHealing = 1;
                int maxHealing;
                if (currentLvl != 1) {
                    maxHealing = (int) (0.1 * currentLvl) + 1;
                } else {
                    maxHealing = (int) 1.1;
                }
                int rangeE = ThreadLocalRandom.current().nextInt(minHealing, maxHealing + 1);
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.healingBoost", rangeE);
                break;
            case "diamond":
                // item will have a random magic damage value that increases w/ lvl (max of +5)
                int minMagDmg = 1;
                int maxMagDmg;
                if (currentLvl != 1) {
                    maxMagDmg = (int) (0.1 * currentLvl) + 1;
                } else {
                    maxMagDmg = (int) 1.1;
                }
                int rangeD = ThreadLocalRandom.current().nextInt(minMagDmg, maxMagDmg + 1);
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.magicDamage", rangeD);
                break;
        }
        return craftedItem;
    }
}
