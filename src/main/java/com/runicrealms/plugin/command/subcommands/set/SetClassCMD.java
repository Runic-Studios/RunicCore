package com.runicrealms.plugin.command.subcommands.set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassGUI;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.player.commands.SetSC;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;

import java.util.List;
import java.util.Objects;

import static com.runicrealms.plugin.classes.ClassGUI.*;

public class SetClassCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();
    private ScoreboardHandler sbh = RunicCore.getScoreboardHandler();

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
        if (pl == null) return;

        if (args.length == 2) {
            //ClassGUI.CLASS_SELECTION.open(pl);
        } else {

            String classStr = args[2].toLowerCase();
            if (!(classStr.equals("archer")
                    || classStr.equals("cleric")
                    || classStr.equals("mage")
                    || classStr.equals("rogue")
                    || classStr.equals("warrior"))) {

                sender.sendMessage(ChatColor.RED
                        + "Available classes: archer, cleric, mage, rogue, warrior");
                return;
            }

            String formattedStr = classStr.substring(0, 1).toUpperCase() + classStr.substring(1);

            setupArtifact(pl, formattedStr, true);
            setupRune(pl);

            // only setup their hearthstone if they don't have one already
            if (pl.getInventory().getItem(2) == null
                    || (pl.getInventory().getItem(2) != null
                    && pl.getInventory().getItem(2).getType() != Material.CLAY_BALL)) {
                setupHearthstone(pl, "General Tso's Camp");
            }

            setConfig(pl, formattedStr);
            sbh.updatePlayerInfo(pl);
            sbh.updateSideInfo(pl);

            // set the player's slot to 0 (the artifact)
            pl.getInventory().setHeldItemSlot(0);
        }
    }
}
