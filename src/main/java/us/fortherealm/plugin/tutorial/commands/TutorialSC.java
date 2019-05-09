package us.fortherealm.plugin.tutorial.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class TutorialSC extends SuperCommand {

    public TutorialSC() {
        super("ftr.tutorial.sc");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

    }
}
