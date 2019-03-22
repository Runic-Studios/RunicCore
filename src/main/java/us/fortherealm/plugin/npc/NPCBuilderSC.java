package us.fortherealm.plugin.npc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class NPCBuilderSC extends SuperCommand {

    public NPCBuilderSC() {
        super("ftrcore.npcbuilder");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "(if the name is 2+ words, use underscores to separate");
            sender.sendMessage(ChatColor.RED + "Correct usage: /npcbuilder build {name} {skinName} {tag} {tagName}");
        }
    }
}
