package us.fortherealm.plugin.command.subcommands.runes;

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
import us.fortherealm.plugin.skill.skilltypes.Skill;

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
		
	}
	
	@Override
	public void onUserCommand(Player sender, String[] args) {
		String skillname = args[0];
		Skill skill = Main.getSkillManager().getSkillByName(skillname);
		
		if(skill == null) {
			sender.sendMessage(ChatColor.RED + "Error: Skill does not exist.");
			return;
		}
		
		sender.getInventory().setItem(2, baseRune(skillname));
	}
	
	private ItemStack baseRune(String skillname) {
		ItemStack baseRune = new ItemStack(Material.INK_SACK, 1, (byte) 1);
		ItemMeta runeMeta = baseRune.getItemMeta();
		runeMeta.setDisplayName(ChatColor.YELLOW + "Rune of " + skillname);
		ArrayList<String> runeLore = new ArrayList<String>();
		runeLore.add(ChatColor.GRAY + "Skill: " + ChatColor.RED + skillname);
		runeLore.add(ChatColor.YELLOW + "Rune");
		runeMeta.setLore(runeLore);
		runeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		baseRune.setItemMeta(runeMeta);
		return baseRune;
	}
	
	@Override
	public String permissionLabel() {
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> skillNames = new ArrayList<>();
		for(Skill skill : Main.getSkillManager().getSkills())
			skillNames.add(skill.getName());
		
		if(args.length == 0)
			return skillNames;
		
		List<String> specificSkills = new ArrayList<>();
		for(String skillName : skillNames)
			if(skillName.toLowerCase().startsWith(skillName.toLowerCase()))
				specificSkills.add(skillName);
		
		return specificSkills;
	}
}
