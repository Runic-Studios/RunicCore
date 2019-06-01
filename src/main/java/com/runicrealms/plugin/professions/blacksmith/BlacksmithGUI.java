package com.runicrealms.plugin.professions.blacksmith;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.item.OptionClickEvent;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlacksmithGUI extends ItemGUI implements Workstation {

    public BlacksmithGUI(Player pl) {
        super(pl.getName() + "'s Blacksmith Workstation", 27, (OptionClickEvent event) -> {

            Bukkit.broadcastMessage(event.getSlot() + "");

            // open skin editor
            if (event.getSlot() == 5 || event.getSlot() == 4) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                //ItemGUI skinEditor = ArtifactGUI.skinEditor(pl, artifact, durability);
                //skinEditor.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);
            }
        }, RunicCore.getInstance());

        super.setHandler((OptionClickEvent eve) -> {

            if (eve.getSlot() == 0) {
                Bukkit.broadcastMessage("you win!");
            }
        });

        createCraftableItem();
    }

    @Override
    public void createCraftableItem() {
        super.setOption(5, new ItemStack(Material.DIAMOND), "&aDiamond", "&7Test", 0);
    }
}
