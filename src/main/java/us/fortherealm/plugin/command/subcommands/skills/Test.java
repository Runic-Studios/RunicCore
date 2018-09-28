package us.fortherealm.plugin.command.subcommands.skills;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.skills.caster.Caster;
import us.fortherealm.plugin.skills.caster.CasterStorage;

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
		
		for(CasterStorage<ItemStack> casterStorage : Main.getItemCasterManager().getCasterStorages()) {
			Caster mother = casterStorage.getMotherCaster();
			
			if(!(mother.getName().toLowerCase().equals(params[1].toLowerCase())))
				return;
			
			ItemStack testItem = new ItemStack(Material.CLAY_BALL);
			testItem.setItemMeta(Main.getItemCasterManager().generateItemMeta(testItem, mother));
			
			casterStorage.addLinkedCaster(testItem);
		}
	}
	
	
	@Override
	public String permissionLabel() {
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> casterNames = new ArrayList<>();
		for(CasterStorage<ItemStack> casterStorage : Main.getItemCasterManager().getCasterStorages()) {
			casterNames.add(casterStorage.getMotherCaster().getName());
		}
		
		if(args.length == 1)
			return casterNames;
		
		List<String> specificCaster = new ArrayList<>();
		for(String casterName : casterNames)
			if(casterName.toLowerCase().startsWith(args[1].toLowerCase()))
				specificCaster.add(casterName);
		
		return specificCaster;
	}
	
}
