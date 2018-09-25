package us.fortherealm.plugin.command.subcommands.skills;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.skills.caster.Caster;
import us.fortherealm.plugin.skills.caster.itemcaster.ItemCaster;

import java.util.ArrayList;
import java.util.List;

public class Test implements SubCommand {
	
	SkillSC skillSC;
	
	public Test(SkillSC skillSC) {
		this.skillSC = skillSC;
	}
	
	@Override
	public void onConsoleCommand(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.RED + "This command may only be run by a player.");
	}
	
	@Override
	public void onOPCommand(Player sender, String[] args) {
		this.onUserCommand(sender, args);
	}
	
	@Override
	public void onUserCommand(Player sender, String[] params) {
		
		if (params.length == 1) {
			sender.sendMessage(ChatColor.RED + "You must specify a oldskills name!");
			return;
		}
		
		StringBuilder casterName = new StringBuilder();
		for(int c = 1; c < params.length; c++)
			casterName.append(params[c] + " ");
		casterName.delete(casterName.length() - 1, casterName.length()); // Removes final space
		
		Caster caster = Main.getCasterManager().getCasterByName(casterName.toString());
		
		if(!(caster instanceof ItemCaster)) {
			System.out.println(ChatColor.RED + "was not an item caster... strange");
		}
		
		ItemCaster itemCaster = (ItemCaster) Main.getCasterManager().getCasterByName(casterName.toString());
		
		if (itemCaster == null) {
			sender.sendMessage(ChatColor.RED + "Error: Caster does not exist.");
			return;
		}
		
		sender.getInventory().setItem(1,itemCaster.getItem());
	}
	
	
	@Override
	public String permissionLabel() {
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> casterNames = new ArrayList<>();
		for(Caster caster : Main.getCasterManager().getCasters())
			casterNames.add(caster.getName());
		
		if(args.length == 1)
			return casterNames;
		
		List<String> specificCaster = new ArrayList<>();
		for(String casterName : casterNames)
			if(casterName.toLowerCase().startsWith(args[1].toLowerCase()))
				specificCaster.add(casterName);
		
		return specificCaster;
	}
	
}
