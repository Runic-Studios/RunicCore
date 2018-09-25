package us.fortherealm.plugin.listeners;

import us.fortherealm.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class SheepDyeEvent implements Listener {

    private Main plugin = Main.getInstance();

    // *** MESSING WITH DYE EVENT IS MESSY, SO THIS SIMPLY GIVES THE ITEM BACK *** //
    @EventHandler
    public void onSheepInteract(PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        if (e.getRightClicked() instanceof Sheep && p.getItemInHand() != null && p.getItemInHand().getData() instanceof Dye) {
            final ItemStack dye = p.getItemInHand().clone();

            Sheep sheep = (Sheep) e.getRightClicked();
            if (sheep.getColor() == ((Dye) dye.getData()).getColor()) return;
            dye.setAmount(1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    p.getInventory().addItem(dye);
                }
            }, 1);//1 tick(s)
        }
    }

    // *** AND THIS RESETS THE SHEEP'S COLOR FOR POLISH *** //
    @EventHandler
    public void onSheepColor(SheepDyeWoolEvent e) {
        final Sheep sheep = e.getEntity();
        final DyeColor color = sheep.getColor();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                sheep.setColor(color);
            }
        }, 1);//1 tick(s)
    }
}
