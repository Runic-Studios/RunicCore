package com.runicrealms.plugin.dungeons;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.utilities.ChatUtils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class BossKillListener implements Listener {

    @EventHandler
    public void onBossKill(MythicMobDeathEvent e) {

        if (!e.getMobType().hasFaction()) return;
        if (e.getMobType().getFaction().toLowerCase().equals("boss")) {

            if (!(e.getKiller() instanceof Player)) return;
            Player pl = (Player) e.getKiller();
            String inDungeonPl = DamageListener.checkForDungeon(pl);
            if (inDungeonPl.equals("library")) {
                completeDungeon(pl, "SUNKEN LIBRARY", 750, ChatColor.GREEN + "Uncommon");
            } else if (inDungeonPl.equals("crypts")) {

            } else if (inDungeonPl.equals("fortress")) {
                // todo: if in room 1, if in boss room 1, etc. update respawn location
                // if boss room
                completeDungeon(pl, "FROZEN FORTRESS", 0, ChatColor.LIGHT_PURPLE + "Epic");
                giveToken(pl);
            }
        }
    }

    // todo: write party rewards method. always send rewards to party leader
    // todo: move exp command in here for bosses
    // todo: announce eldrid kill, fireworks
    private void completeDungeon(Player pl, String dungeonName, int expAmt, String lootTier) {

        // player does not have party
        if (RunicCore.getPartyManager().getPlayerParty(pl) == null) {
        pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        pl.sendMessage("");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "DUNGEON COMPLETE: " + dungeonName + "!");
        if (expAmt != 0) {
            ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "+ " + ChatColor.WHITE + expAmt + ChatColor.GREEN + " Experience");
        }
        ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "+ 3 " + lootTier);
        pl.sendMessage("");

        // player has party
        } else if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
            for (Player member : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {
                member.sendMessage("");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "DUNGEON COMPLETE: " + dungeonName + "!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "+ " + ChatColor.WHITE + expAmt + ChatColor.GREEN + " Experience Split Among Party");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "+ 3 " + lootTier + " to boss killer");
                member.sendMessage("");
            }
        }
    }

    private void giveToken(Player pl) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem("TokenOfValor").get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(1);
        ItemStack token = BukkitAdapter.adapt(abstractItemStack);

        if (RunicCore.getPartyManager().getPlayerParty(pl) == null) {
            if (pl.getInventory().firstEmpty() != -1) {
                int firstEmpty = pl.getInventory().firstEmpty();
                pl.getInventory().setItem(firstEmpty, token);
            } else {
                pl.getWorld().dropItem(pl.getLocation(), token);
            }
        } else if (RunicCore.getPartyManager().getPlayerParty(pl) != null) {
            for (Player member : RunicCore.getPartyManager().getPlayerParty(pl).getPlayerMembers()) {
                if (member.getInventory().firstEmpty() != -1) {
                    int firstEmpty = member.getInventory().firstEmpty();
                    member.getInventory().setItem(firstEmpty, token);
                } else {
                    member.getWorld().dropItem(member.getLocation(), token);
                }
            }

        }
    }
}
