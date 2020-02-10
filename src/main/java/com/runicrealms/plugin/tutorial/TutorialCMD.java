package com.runicrealms.plugin.tutorial;

import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.player.commands.SetSC;

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
        String classStr = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (classStr == null) return;

        Location anvilLoc = new Location(Bukkit.getWorld("Alterra"), -2329.5, 38, 1770.5);

        pl.playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);

        // fog particles for player
        pl.spawnParticle(Particle.REDSTONE, anvilLoc.add(0,1,0),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 4));

        Material artifactType = Material.STICK;
        switch (classStr.toLowerCase()) {
            case "archer":
                artifactType = Material.BOW;
                break;
            case "cleric":
                artifactType = Material.WOODEN_SHOVEL;
                break;
            case "mage":
                artifactType = Material.WOODEN_HOE;
                break;
            case "rogue":
                artifactType = Material.WOODEN_SWORD;
                break;
            case "warrior":
                artifactType = Material.WOODEN_AXE;
                break;
        }

        // floating item for player
        FloatingItemUtil.spawnFloatingItem(pl, anvilLoc.add(0, 3, 0), artifactType, 6, 0);
    }
}
