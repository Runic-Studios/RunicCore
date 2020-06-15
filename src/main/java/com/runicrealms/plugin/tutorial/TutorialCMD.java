package com.runicrealms.plugin.tutorial;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.player.commands.SetSC;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TutorialCMD implements SubCommand {

    private SetSC set;

    public TutorialCMD(SetSC set) {
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
        return "set.artifact";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, RunicCore.getInstance());
    }

    private void setClass(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set artifact {player}");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;

        // get player class
        String classStr = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();
        if (classStr == null) return;

        pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);

        // fog particles for player
        pl.spawnParticle(Particle.REDSTONE, pl.getEyeLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 4));

        String artifactName = "";
        switch (classStr.toLowerCase()) {
            case "archer":
                artifactName = "oaken_shortbow";
                break;
            case "cleric":
                artifactName = "oaken_mace";
                break;
            case "mage":
                artifactName = "oaken_branch";
                break;
            case "rogue":
                artifactName = "oaken_sparring_sword";
                break;
            case "warrior":
                artifactName = "oaken_axe";
                break;
        }

        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "giveartifact " + pl.getName() + " " + artifactName);
    }
}
