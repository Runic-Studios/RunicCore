package us.fortherealm.plugin.npc;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.subcommands.SubCommand;

import java.util.List;

public class Build implements SubCommand {

    private NPCBuilderSC builderSC;
    private static final double hologramHeight = 1.3; // actually 2.5, just weird stuff cuz we gotta subtract the head.

    public Build(NPCBuilderSC builderSC) {
        this.builderSC = builderSC;
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

        if (args.length != 5) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /npcbuilder build {name} {skinName} {tag} {tagName}");
            sender.sendMessage(ChatColor.RED + "(if the name is 2+ words, use underscores to separate");
            return;
        }

        // validate input
        if (!args[3].toLowerCase().equals("npc")
                && !args[3].toLowerCase().equals("quest")
                && !args[3].toLowerCase().equals("banker")
                && !args[3].toLowerCase().equals("merchant")) {

            sender.sendMessage(ChatColor.RED + "Valid npc tags: npc, quest, merchant, banker");
            return;
        }

        // create the NPC tag
        String tagString = "";
        switch (args[3].toLowerCase()) {
            case "npc":
                tagString = ChatColor.GRAY + "NPC";
                break;
            case "quest":
                tagString = ChatColor.GOLD + "Quest";
                break;
            case "merchant":
                tagString = ChatColor.GRAY + "Merchant";
                break;
            case "banker":
                tagString = ChatColor.GRAY + "Banker";
                break;
        }

        // center player to build NPC
        Location loc = sender.getLocation();
        double x = (int) loc.getX();
        double y = (int) loc.getY();
        double z = (int) loc.getZ();

        if (x > 0) {
            x+=0.5;
        } else {
            x-=0.5;
        }

        if (z > 0) {
            z = z + 0.5;
        } else {
            z -= 0.5;
        }

        Location centeredLoc = new Location(sender.getWorld(), x, y, z);
        int centerY = (int) centeredLoc.getY();
        sender.teleport(centeredLoc);

        // create the npc, make it look at nearby players
        sender.performCommand("npc create &7");
        sender.performCommand("npc lookclose");
        sender.performCommand("npc skin " + args[2]);

        // create hologram height
        double holoY = centerY + hologramHeight;
        Location hologramLoc = new Location(sender.getWorld(), centeredLoc.getX(), holoY, centeredLoc.getZ());

        // build holographic display
        String[] npcNameArr = args[1].split("_");
        StringBuilder npcName = new StringBuilder();
        for (String s : npcNameArr) {

            npcName.append(s);
            npcName.append(" ");
        }
        sender.teleport(hologramLoc);
        sender.performCommand("hd create " + args[4] + " &e" + npcName);
        sender.performCommand("hd addline " + args[4] + " " + tagString);
    }

    @Override
    public String permissionLabel() {
        return "ftrcore.npcbuilder.build";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
