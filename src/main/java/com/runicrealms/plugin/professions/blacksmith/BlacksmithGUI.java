package com.runicrealms.plugin.professions.blacksmith;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.item.OptionClickEvent;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class BlacksmithGUI extends Workstation {

    public BlacksmithGUI(Player pl) {

        super(pl.getName() + "'s Blacksmith Workstation", 27, (OptionClickEvent event) -> {

            Bukkit.broadcastMessage(event.getPosition() + "");

            // open skin editor
            if (event.getPosition() == 5 || event.getPosition() == 4) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                //ItemGUI skinEditor = ArtifactGUI.skinEditor(pl, artifact, durability);
                //skinEditor.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);
            }
        }, RunicCore.getInstance());

        super.setHandler((OptionClickEvent eve) -> {

            if (eve.getSuper().getSlot() == 0) {
                Bukkit.broadcastMessage("you win!");
            }
        });

        super.createCraftableItem();
    }
}
