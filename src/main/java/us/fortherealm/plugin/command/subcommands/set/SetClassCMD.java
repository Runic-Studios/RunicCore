package us.fortherealm.plugin.command.subcommands.set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.classes.ClassGUI;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.util.TabCompleteUtil;
import us.fortherealm.plugin.player.commands.SetSC;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;

import java.util.List;

import static us.fortherealm.plugin.classes.ClassGUI.*;

public class SetClassCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = FTRCore.getInstance();
    private ScoreboardHandler sbh = FTRCore.getScoreboardHandler();

    public SetClassCMD(SetSC set) {
        this.set = set;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

        // if the sender does not specify the correct arguments
        setClass(sender, args);
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify the correct arguments
        setClass(sender, args);
    }

    @Override
    public String permissionLabel() {
        return "set.class";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }

    private void setClass(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set class {player} or /set class {player} {class}");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);

        if (args.length == 2) {
            ClassGUI.CLASS_SELECTION.open(pl);
        } else {

            String classStr = args[2];
            if (!(classStr.equals("Archer")
                    || classStr.equals("Cleric")
                    || classStr.equals("Mage")
                    || classStr.equals("Rogue")
                    || classStr.equals("Warrior"))) {

                sender.sendMessage(ChatColor.RED
                        + "Available classes: Archer, Cleric, Mage, Rogue, Warrior (case-sensitive)");
                return;
            }

            setupArtifact(pl, classStr, true);
            setupRune(pl);
            setupHearthstone(pl);
            setConfig(pl, classStr);
            sbh.updatePlayerInfo(pl);
            sbh.updateSideInfo(pl);

            // set the player's slot to 0 (the artifact)
            pl.getInventory().setHeldItemSlot(0);
        }
    }
}
