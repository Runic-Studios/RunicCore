package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class SkillpointSC extends SuperCommand {

    public SkillpointSC() {
        super("player.skillpoint");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillpoint give [player]");
        }
    }
}
