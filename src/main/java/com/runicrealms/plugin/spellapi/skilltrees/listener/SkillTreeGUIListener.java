package com.runicrealms.plugin.spellapi.skilltrees.listener;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SkillTreeGUIListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {

        /*
        Preliminary checks
         */
        if (event.getClickedInventory() == null) return;
        if (event.isCancelled()) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof SkillTreeGUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        SkillTreeGUI skillTreeGUI = (SkillTreeGUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(skillTreeGUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (skillTreeGUI.getInventory().getItem(event.getRawSlot()) == null) return;

        ItemStack item = event.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (material == Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
            player.openInventory(new SubClassGUI(player).getInventory());
        } else if (Arrays.stream(SkillTreeGUI.getPerkSlots()).anyMatch(n -> n == event.getRawSlot())) {
            int perkPosition = ArrayUtils.indexOf(SkillTreeGUI.getPerkSlots(), event.getRawSlot());
            Perk previous;
            if (perkPosition == 0) {
                previous = null;
            } else {
                previous = skillTreeGUI.getSkillTree().getPerks().get(perkPosition - 1); // grab previous perk to ensure they follow path
            }
            Perk perk = skillTreeGUI.getSkillTree().getPerks().get(perkPosition);
            // Purchase perk and write to Redis async
            TaskChain<?> chain = RunicCore.newChain();
            chain
                    .asyncFirst(() -> skillTreeGUI.getSkillTree().attemptToPurchasePerk(player, RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId()), previous, perk))
                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to purchase a skill tree perk!")
                    .syncLast(result -> {
                        skillTreeGUI.getInventory().setItem(event.getRawSlot(),
                                SkillTreeGUI.buildPerkItem(perk, true, ChatColor.AQUA + "» Click to purchase"));
                        skillTreeGUI.getInventory().setItem(SkillTreeGUI.getInfoItemPosition(), skillTreeGUI.infoItem());
                    })
                    .execute();
        }
    }
}
