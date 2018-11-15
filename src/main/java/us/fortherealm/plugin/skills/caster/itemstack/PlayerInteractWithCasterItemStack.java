package us.fortherealm.plugin.skills.caster.itemstack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getName;

public class PlayerInteractWithCasterItemStack implements Listener {

    private Plugin plugin = Main.getInstance();
    private List<Skill> skillList;
    private HashMap<UUID, HashMap<Skill, Long>> cooldown;
    private boolean doCooldown;
    private CasterItemStack casterItemStack;

    // ensure player still casts skill
    // TODO: add a slot check 0, 1
    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        performCast(event.getItem(), event.getAction(), event.getPlayer());

    }
// TODO: keep this, but add a check for player holding slot 0, 1
//	@EventHandler
//	public void onDamageEvent(EntityDamageByEntityEvent event) {
//
//		if(!(event.getDamager() instanceof Player))
//			return;
//
//		Player player = (Player) event.getDamager();
//        int slot = player.getInventory().getHeldItemSlot();
//
//        // cast the
//		if (slot == 0) {
//            performCast(player.getInventory().getItemInMainHand(), Action.LEFT_CLICK_AIR, player);
//        }
//
//	}

    private void performCast(ItemStack item, Action action, Player player) {
        if (player == null)
            return;

        if (item == null)
            return;

        if (!(CasterItemStack.containsCasterSignature(item)))
            return;

        CasterItemStack casterItem = CasterItemStack.getCasterItem(item);

        if (casterItem == null)
            return;

        CasterItemStack.ItemType type = casterItem.getItemType();


        switch (action) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:

              //if(secondaryCD.isOnCooldown()) {
//            return;
//        }


                if (item.getType() == Material.BOW) {
                    if (player.isSneaking()) {


                        // TODO: execute artifactPrimary, artSec, runePrim, runeSec
                        // TODO: problem -- one is artifact, one is rune
                        // -------------------------------------------
                        casterItem.executeSecondarySkills(player);
                        //casterItemStack.cooldownTask(player);
                        //--------------------------------------------

                    }
                } else {
                    casterItem.executeSecondarySkills(player);
                }
                break;
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                if (casterItem.getItemType() == CasterItemStack.ItemType.ARTIFACT && item.getType() != Material.BOW) {
                    if (player.isSneaking()) {
                        casterItem.executePrimarySkills(player);
                    }
                } else {
                    casterItem.executePrimarySkills(player);
                }
                break;
        }
    }
}
