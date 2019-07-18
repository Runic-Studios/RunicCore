package com.runicrealms.plugin.mysterybox.animation.animations;

import com.runicrealms.plugin.mysterybox.MysteryItem;
import com.runicrealms.plugin.mysterybox.animation.MysteryAnimation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by KissOfFate
 * Date: 7/17/2019
 * Time: 8:12 PM
 */
public class Tornado extends MysteryAnimation {
    private List<MysteryItem>  _stacks;

    /**
     * Basic Constructor to build the default tornado
     *
     * @param stacks Either MysteryItem List or ItemStack
     */
    public Tornado(List<MysteryItem> stacks) {
        super("Tornado", 5);

        this._stacks = stacks;
    }

    @Override
    public void onTick(Player player, Location location) {
        if(this._stacks == null) return;

        Bukkit.broadcastMessage("Tick Called");
    }

}
