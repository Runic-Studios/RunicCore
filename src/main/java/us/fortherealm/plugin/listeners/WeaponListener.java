package us.fortherealm.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.utilities.WeaponEnum;

public class WeaponListener implements Listener {

    @EventHandler
    public void onMeleeAttack(PlayerInteractEvent e) {

        // check for null
        if (e.getItem() == null) {
            return;
        }

        // only listen for left clicks
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;

        ItemStack artifact = e.getItem();
        WeaponEnum artifactType = WeaponEnum.matchType(artifact);
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

        if (artifactType.equals(WeaponEnum.BOW) || artifactType.equals(WeaponEnum.STAFF)) return;

        double speed = AttributeUtil.getGenericDouble(artifact, "generic.attackSpeed");
        if (speed != 0) {
            e.getPlayer().setCooldown(artifact.getType(), (int) (20/(24+speed)));
        }
    }
}
