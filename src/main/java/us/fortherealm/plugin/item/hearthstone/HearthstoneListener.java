package us.fortherealm.plugin.item.hearthstone;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

public class HearthstoneListener implements Listener {

    private static final int cooldownTime = 5;
    private static final int teleportTime = 5;
    private HashMap<UUID, Long> hsCooldowns = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player pl = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // if its the 3rd slot in a player's inventory, run the stuff
        if (itemslot != 2) return;
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onHearthstoneUse(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();

        if (pl.getInventory().getItemInMainHand() == null) return;

        int slot = pl.getInventory().getHeldItemSlot();
        if (slot != 2) return;
        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (Main.getCombatManager().getPlayersInCombat().containsKey(uuid)) {
            pl.sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }

        // todo: change to check the server time
        if (hsCooldowns.containsKey(uuid)) {

            if ((System.currentTimeMillis()-hsCooldowns.get(uuid))/1000 >= cooldownTime) {
                hsCooldowns.remove(uuid);
                activateHearthstone(pl);


            } else {

                double rawTime = (double) ((cooldownTime*1000) - (System.currentTimeMillis()-hsCooldowns.get(uuid))) / 1000;
                NumberFormat toDecimal = new DecimalFormat("#0.00");
                String timeLeft = toDecimal.format(rawTime);
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                pl.sendMessage(ChatColor.RED + "You must wait "
                        + ChatColor.YELLOW + timeLeft + "s"
                        + ChatColor.RED + " before using your hearthstone.");
            }
        } else {
            activateHearthstone(pl);
        }
    }

    private void activateHearthstone(Player pl) {
        hsCooldowns.put(pl.getUniqueId(), System.currentTimeMillis());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation().add(0,1,0),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.AQUA, 5));

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {

                if (count >= teleportTime) {
                    this.cancel();
                }

                pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Teleporting... "
                        + ChatColor.WHITE + ChatColor.BOLD + + (teleportTime-count) + "s");
                count = count+1;

            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        // cancel the event
        if (slot == 2) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
            p.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }
}

