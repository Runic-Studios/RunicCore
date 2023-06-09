package com.runicrealms.plugin.donor.ui;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.DonorRank;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.model.TitleData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import redis.clients.jedis.Jedis;

public class DonorPerksUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof DonorPerksUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        DonorPerksUI donorPerksUI = (DonorPerksUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(donorPerksUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (donorPerksUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 0) {
            player.openInventory(new DonorUI(player).getInventory());
        } else if (event.getSlot() == 20) {
            DonorRank rank = DonorRank.getDonorRank(player);
            if (rank != DonorRank.NONE) {
                String title = ColorUtil.format(rank.getTitle());
                String completeMessage = ChatColor.DARK_AQUA + "You have enabled the title: " + ChatColor.AQUA + title + ChatColor.DARK_AQUA + "!";
                TaskChain<?> chain = RunicCore.newChain();
                chain
                        .asyncFirst(() -> {
                            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                                TitleData titleData = RunicCore.getTitleAPI().loadTitleData(player.getUniqueId(), jedis);
                                titleData.setPrefix(title);
                                titleData.writeToJedis(player.getUniqueId(), jedis);
                                return titleData;
                            }
                        })
                        .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicAchievements failed to write title!")
                        .syncLast(data -> player.sendMessage(completeMessage))
                        .execute();
                player.closeInventory();
            }
        }
    }

}
