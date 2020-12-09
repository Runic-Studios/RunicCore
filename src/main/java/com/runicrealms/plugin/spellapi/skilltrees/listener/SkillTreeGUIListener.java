package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        e.setCancelled(true);

        if (material == Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            pl.openInventory(RunicCoreAPI.runeGUI(pl).getInventory());


//        if (material == Material.GREEN_STAINED_GLASS_PANE) {
//            if (ui.getPage() == 1 && ui.getSelectedColor() != null && ui.getChosenColor() == null && ui.getChosenPattern() == null) {
//                ui.setSelectedColor(null);
//                ui.openPatternMenu();
//            } else if (ui.getPage() == 1 && ui.getSelectedColor() != null && ui.getChosenColor() == null && ui.getChosenPattern() != null) {
//                ui.setChosenColor(ui.getSelectedColor());
//                meta.removePattern(meta.getPatterns().size() - 1);
//                meta.addPattern(new Pattern(ui.getChosenColor(), ui.getChosenPattern()));
//                ui.setSelectedColor(null);
//                ui.setChosenColor(null);
//                ui.setChosenPattern(null);
//                ui.openPatternMenu();
//            } else if (ui.getPage() == 2 && ui.getSelectedColor() == null && ui.getSelectedPattern() != null && ui.getChosenPattern() == null) {
//                ui.setChosenPattern(ui.getSelectedPattern());
//                ui.setSelectedPattern(null);
//                ui.openColorMenu();
//            }
//            return;
//        }
//
//        if (itemMeta.getPersistentDataContainer().has(ui.getKey(), PersistentDataType.STRING) && this.isConcrete(ui, item)) {
//            DyeColor color = DyeColor.valueOf(itemMeta.getPersistentDataContainer().get(ui.getKey(), PersistentDataType.STRING));
//            if (ui.getChosenPattern() == null) {
//                ui.getDummyBanner().setType(Material.valueOf(color.name() + "_BANNER"));
//            } else {
//                meta.removePattern(meta.getPatterns().size() - 1);
//                meta.addPattern(new Pattern(color, ui.getChosenPattern()));
//                ui.getDummyBanner().setItemMeta(meta);
//            }
//            ui.setSelectedColor(color);
//            ui.openColorMenu();
//            return;
//        }
    }
}
