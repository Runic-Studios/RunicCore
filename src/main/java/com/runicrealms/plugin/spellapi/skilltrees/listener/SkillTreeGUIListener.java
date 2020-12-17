package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SkillTreeGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof SkillTreeGUI)) return;
        SkillTreeGUI skillTreeGUI = (SkillTreeGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(skillTreeGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }

        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (skillTreeGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        ItemMeta itemMeta = item.getItemMeta();
        Material material = item.getType();

        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            pl.openInventory(new SubClassGUI(pl).getInventory());
        else if (Arrays.stream(SkillTreeGUI.getPerkSlots()).anyMatch(n-> n == e.getRawSlot())) {
            int perkPosition = ArrayUtils.indexOf(SkillTreeGUI.getPerkSlots(), e.getRawSlot());
            Perk previous;
            if (perkPosition == 0)
                previous = null;
            else
                previous = skillTreeGUI.getSkillTree().getPerks().get(perkPosition - 1); // grab previous perk to ensure they follow path
            Perk perk = skillTreeGUI.getSkillTree().getPerks().get(perkPosition);
            skillTreeGUI.getSkillTree().attemptToPurchasePerk(previous, perk);
            skillTreeGUI.getInventory().setItem(e.getRawSlot(), skillTreeGUI.buildPerkItem(perk));
            skillTreeGUI.getInventory().setItem(SkillTreeGUI.getInfoItemPosition(), skillTreeGUI.infoItem());
        }
    }
}
