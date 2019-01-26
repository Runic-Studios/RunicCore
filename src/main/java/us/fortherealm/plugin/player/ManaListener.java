package us.fortherealm.plugin.player;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;

import java.util.ArrayList;

/**
 *  Updates player mana on login and upon armor equip
 */
public class ManaListener implements Listener {

    private Plugin plugin = Main.getInstance();
    private ManaManager manaManager = Main.getManaManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player pl = e.getPlayer();

        // set their mana to their maxMana on login
        // create the field if it doesn't exist
        int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        if (maxMana == 0) {
            maxMana = 50+(manaManager.getManaPerLevel()*pl.getLevel());
            plugin.getConfig().set(pl.getUniqueId() + ".info.maxMana", maxMana);
            plugin.saveConfig();
            plugin.reloadConfig();
        }
        // store player's current mana
        manaManager.getCurrentManaList().put(pl.getUniqueId(), maxMana);
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {

        Player pl = e.getPlayer();

        // delay by 1 tick to calculate new armor values, not old
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMana(pl);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        Player pl = e.getPlayer();
        if (pl.getLevel() > 50) return;
        calculateMana(pl);
        int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        manaManager.getCurrentManaList().put(pl.getUniqueId(), maxMana);
    }

    private void calculateMana(Player pl) {
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
        int newMaxMana = 50 + (manaManager.getManaPerLevel() * pl.getLevel()) + totalItemManaBoost;
        plugin.getConfig().set(pl.getUniqueId() + ".info.maxMana", newMaxMana);
        saveConfig(pl);

        int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        int currentMana = manaManager.getCurrentManaList().get(pl.getUniqueId());
        if (currentMana > maxMana) {
            manaManager.getCurrentManaList().put(pl.getUniqueId(), maxMana);
            Main.getScoreboardHandler().updateSideInfo(pl);
        }
    }

    private void saveConfig(Player pl) {
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
        Main.getScoreboardHandler().updateSideInfo(pl);
    }
}
