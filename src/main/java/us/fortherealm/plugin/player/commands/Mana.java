package us.fortherealm.plugin.player.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class Mana extends SuperCommand {

    public Mana() {
        super("player.set.mana");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            int maxMana = FTRCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
            FTRCore.getManaManager().getCurrentManaList().put(pl.getUniqueId(), maxMana);
            FTRCore.getScoreboardHandler().updateSideInfo(pl);
            pl.sendMessage(ChatColor.AQUA + "You've restored your mana!");

//            for (Entity e : pl.getNearbyEntities(2, 2, 2)) {
//                if (e instanceof ArmorStand) e.remove();
//            }
//
//            FTRCore.getMobHealthManager().fullClean();
        }
    }
}
