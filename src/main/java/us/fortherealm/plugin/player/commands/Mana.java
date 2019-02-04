package us.fortherealm.plugin.player.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class Mana extends SuperCommand {

    public Mana() {
        super("player.set.mana");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            int maxMana = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
            Main.getManaManager().getCurrentManaList().put(pl.getUniqueId(), maxMana);
            Main.getScoreboardHandler().updateSideInfo(pl);
            pl.sendMessage(ChatColor.AQUA + "You've restored your mana!");
        }
    }
}
