package us.fortherealm.plugin.command.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.SkillpointSC;
import us.fortherealm.plugin.command.util.TabCompleteUtil;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;

import java.util.List;

public class Skillpoint implements SubCommand {

    private SkillpointSC skillpointSC;
    private Plugin plugin = FTRCore.getInstance();
    private ScoreboardHandler sbh = FTRCore.getScoreboardHandler();

    public Skillpoint(SkillpointSC skillpointSC) {
        this.skillpointSC = skillpointSC;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {
        this.onUserCommand((Player) sender, args);
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify the correct arguments
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /skillpoint give [player]");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);

        // give player 1 skillpoint
        if (pl != null) {
            int skillpoints = FTRCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.skillpoints");
            FTRCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.skillpoints", skillpoints+1);
            FTRCore.getInstance().saveConfig();
            FTRCore.getInstance().reloadConfig();
        }
    }

    @Override
    public String permissionLabel() {
        return "ftrcore.skillpoint";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
