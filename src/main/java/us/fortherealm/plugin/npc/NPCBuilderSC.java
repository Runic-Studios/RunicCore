package us.fortherealm.plugin.npc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class NPCBuilderSC extends SuperCommand {

    public NPCBuilderSC() {
        super("ftr.scripter.npcbuild");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        sender.sendMessage(ChatColor.GOLD + "Command usage: /npc build {type} {skin} {skinname}");
        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "NPC Types: " + ChatColor.YELLOW + "banker, default, merchant, quest");
    }
}
