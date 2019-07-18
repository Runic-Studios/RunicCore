package com.runicrealms.plugin.mysterybox;

import com.runicrealms.plugin.mysterybox.animation.Animation;
import com.runicrealms.plugin.mysterybox.animation.animations.Tornado;
import org.bukkit.entity.Player;

/**
 * Created by KissOfFate
 * Date: 7/17/2019
 * Time: 8:54 PM
 */
public class MysteryTest {

    /**
     * Just to show off how to use the animations
     *
     */
    public void test(Player player) {
        Animation animation = new Tornado(MysteryLoot.getMysteryItems());
        animation.spawn(player, player.getLocation());
    }
}
