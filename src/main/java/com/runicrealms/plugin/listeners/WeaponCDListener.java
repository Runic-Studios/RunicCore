package com.runicrealms.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.runicrealms.plugin.attributes.AttributeUtil;

public class WeaponCDListener implements Listener {

    @EventHandler
    public void onMeleeAttack(PlayerInteractEvent e) {

        // check for null
        if (e.getItem() == null) return;

        // only listen for left clicks
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;
        if (e.getAction() == Action.PHYSICAL) return;

        ItemStack artifact = e.getItem();
        Material artifactType = artifact.getType();
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

        // don't fire cooldown if they're sneaking, since they're casting a spell
        if (e.getPlayer().isSneaking()) {
            e.setCancelled(true);
            return;
        }

        // IMPORTANT: we're not gonna set the bow cooldown here since bows use left-click to cast spells.
        double speed = AttributeUtil.getGenericDouble(artifact, "generic.attackSpeed");
        if (speed != 0) {
            e.getPlayer().setCooldown(artifact.getType(), (int) (20/(24+speed)));
        }
    }

    @EventHandler
    public void onMeleeAttack(EntityDamageByEntityEvent e) {

        // check for player
        if (!(e.getDamager() instanceof Player)) return;

        Player pl = (Player) e.getDamager();

        if (pl.getInventory().getItemInMainHand() == null) return;

        ItemStack artifact = pl.getInventory().getItemInMainHand();
        Material artifactType = artifact.getType();
        double cooldown = pl.getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

        // don't fire cooldown if they're sneaking, since they're casting a spell
        if (pl.isSneaking()) {
            e.setCancelled(true);
            return;
        }

        double speed;
        if (artifactType == Material.BOW) {
            speed = AttributeUtil.getCustomDouble(artifact, "custom.bowSpeed");
        } else {
            speed = AttributeUtil.getGenericDouble(artifact, "generic.attackSpeed");
        }
        if (speed != 0) {
            pl.setCooldown(artifact.getType(), (int) (20/(24+speed)));
        }
    }
}
