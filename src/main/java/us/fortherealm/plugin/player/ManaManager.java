package us.fortherealm.plugin.player;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Method to manage player mana. Stores max mana in the player data file,
 * and creates a HashMap to store all current player manas. Updates on login
 * and upon armor equip
 * @author Skyfallin_
 */
public class ManaManager implements Listener {

    private Plugin plugin = Main.getInstance();
    private static HashMap<UUID, Integer> currentPlayerManas;
    private static final int manaPerLevel = 2;
    private static final int manaRegenAmt = 5;
    private static final long manaRegenTime = (long) 7.5; // seconds

    // constructor
    public ManaManager() {
        currentPlayerManas = new HashMap<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            int maxMana = Main.getInstance().getConfig().getInt(online.getUniqueId() + ".info.maxMana");
            currentPlayerManas.put(online.getUniqueId(), maxMana);
        }
        this.startRegenTask();
    }

    private void startRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                regenMana();
            }
        }.runTaskTimer(this.plugin, 20, manaRegenTime*20);
    }

    private void regenMana() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            int mana = currentPlayerManas.get(online.getUniqueId());
            int maxMana = Main.getInstance().getConfig().getInt(online.getUniqueId() + ".info.maxMana");
            if (mana >= maxMana) continue;

            if (mana+manaRegenAmt >= maxMana) {
                currentPlayerManas.put(online.getUniqueId(), maxMana);
            } else {
                currentPlayerManas.put(online.getUniqueId(), mana + manaRegenAmt);
            }
            Main.getScoreboardHandler().updateSideInfo(online);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player pl = e.getPlayer();

        // set their mana to their maxMana on login
        // create the field if it doesn't exist
        int maxMana = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        if (maxMana == 0) {
            maxMana = 50+(manaPerLevel*pl.getLevel());
            Main.getInstance().getConfig().set(pl.getUniqueId() + ".info.maxMana", maxMana);
            Main.getInstance().saveConfig();
            Main.getInstance().reloadConfig();
        }
        // store player's current mana
        this.getCurrentManaList().put(pl.getUniqueId(), maxMana);
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {

        Player pl = e.getPlayer();

        // delay by 0.5s to calculate new armor values, not old
        new BukkitRunnable() {
            @Override
            public void run() {
                // grab player's armor, offhand
                ArrayList<ItemStack> armorAndOffhand = new ArrayList<>();
                PlayerInventory inv = pl.getInventory();
                ItemStack helmet = inv.getHelmet();
                ItemStack chestplate = inv.getChestplate();
                ItemStack leggings = inv.getLeggings();
                ItemStack boots = inv.getBoots();
                ItemStack offhand = inv.getItemInOffHand();

                // add all the items to arraylist
                if (helmet != null) armorAndOffhand.add(pl.getInventory().getHelmet());
                if (chestplate != null) armorAndOffhand.add(pl.getInventory().getChestplate());
                if (leggings != null) armorAndOffhand.add(pl.getInventory().getLeggings());
                if (boots != null) armorAndOffhand.add(pl.getInventory().getBoots());
                if (offhand != null) armorAndOffhand.add(pl.getInventory().getItemInOffHand());

                int totalItemManaBoost = 0;

                // calculate the player's total mana boost
                for (ItemStack item : armorAndOffhand) {
                    int itemManaBoost = (int) AttributeUtil.getCustomDouble(item, "custom.manaBoost");
                    totalItemManaBoost = totalItemManaBoost + itemManaBoost;
                }

                // update stored mana in config, update scoreboard
                int newMaxMana = 50 + (manaPerLevel * pl.getLevel()) + totalItemManaBoost;
                plugin.getConfig().set(pl.getUniqueId() + ".info.maxMana", newMaxMana);
                Main.getInstance().saveConfig();
                Main.getInstance().reloadConfig();
                Main.getScoreboardHandler().updateSideInfo(pl);

                int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
                int currentMana = getCurrentManaList().get(pl.getUniqueId());
                if (currentMana > maxMana) {
                    getCurrentManaList().put(pl.getUniqueId(), maxMana);
                    Main.getScoreboardHandler().updateSideInfo(pl);
                }
            }
        }.runTaskLater(Main.getInstance(), 10L);
    }

    public HashMap<UUID, Integer> getCurrentManaList() {
        return currentPlayerManas;
    }
 }
