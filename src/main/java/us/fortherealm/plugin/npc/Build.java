package us.fortherealm.plugin.npc;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Party;

import java.util.List;

public class Build implements SubCommand {

    private NPCBuilderSC npcbuilder;
    private Plugin plugin = Main.getInstance();

    public Build(NPCBuilderSC npcbuilder) {
        this.npcbuilder = npcbuilder;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        if (args.length != 4) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Command uage: 4 arguments");
            //return;
        }

    }

    @Override
    public String permissionLabel() {
        return "npcbuilder.build";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
