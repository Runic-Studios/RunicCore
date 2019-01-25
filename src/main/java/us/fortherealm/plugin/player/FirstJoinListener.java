package us.fortherealm.plugin.player;

import de.tr7zw.itemnbtapi.NBTEntity;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.UUID;

public class FirstJoinListener implements Listener {

    private Main plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // set join message
        // TODO: inform players if their guild mate or friend logs in.
        event.setJoinMessage("");
        player.sendMessage(ChatColor.GRAY + "Loading resource pack, this may take a moment...");

        // set the player's class to "None" if they don't have one setup (do this every login in case of corruption)
        if (!plugin.getConfig().isSet(uuid + ".info.class.name")) {
            setConfig(uuid, "class.name");
        }

        if (!plugin.getConfig().isSet(uuid + ".info.guild")) {
            setConfig(uuid, "guild");
        }

        if (!plugin.getConfig().isSet(uuid + ".info.prof.name")) {
            setConfig(uuid, "prof.name");
        }

        // setup for new players
        if (!player.hasPlayedBefore()) {

            // broadcast new player welcome message
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + player.getName() + ChatColor.LIGHT_PURPLE + " joined the realm for the first time!");

            // create the hearthstone
            ItemStack hearthstone = new ItemStack(Material.NETHER_STAR);
            ItemMeta hsmeta = hearthstone.getItemMeta();
            hsmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Hearthstone");
            ArrayList<String> hslore = new ArrayList<String>();
            // TODO: add variable for HS location
            hslore.add(ChatColor.GRAY + "Left click: Return to your " + ChatColor.GOLD + "Guild Hall");
            hslore.add(ChatColor.GRAY + "Right click: Return to " + ChatColor.GREEN + "The Tutorial");
            hslore.add(ChatColor.DARK_GRAY
                    + "Speak to an " + ChatColor.YELLOW + "innkeeper "
                    + ChatColor.DARK_GRAY + "to change your home.");
            hsmeta.setLore(hslore);
            hearthstone.setItemMeta(hsmeta);

            // set the item!
            player.getInventory().setItem(2, hearthstone);
        }

        // set player's default health to 50
        NBTEntity nbtPlayer = new NBTEntity(player);
        NBTList list = nbtPlayer.getList("Attributes", NBTType.NBTTagCompound);
        for (int i = 0; i < list.size(); i++) {
            NBTListCompound lc = list.getCompound(i);
            if (lc.getString("Name").equals("generic.maxHealth")) {
                lc.setDouble("Base", 50f);
            }
        }

        // todo: put all health-related stuff in its own class file
        // update the heart display
        // (ex: 50/12.5 = 4 hearts)
        // to prevent awkward half-heart displays, it rounds down to the nearest full heart.
        int scale = (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 12.5);
        if (scale % 2 != 0) {
            scale = scale-1;
        }
        player.setHealthScale(scale);
    }

    private void setConfig(UUID uuid, String setting) {
        Main.getInstance().getConfig().set(uuid + ".info." + setting, "None");
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
    }
}
