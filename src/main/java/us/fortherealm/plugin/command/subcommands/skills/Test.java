package us.fortherealm.plugin.command.subcommands.skills;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.skills.caster.itemstack.CasterItemStack;
import us.fortherealm.plugin.skills.skilltypes.warrior.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Fireball;

import java.util.Arrays;
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
		
		CasterItemStack casterItem = new CasterItemStack(
				Material.CLAY_BALL,
				"test",
				CasterItemStack.Type.RUNE,
				5,
				Arrays.asList(new Fireball()),
				Arrays.asList(new Deliverance())
		);
		
		sender.getInventory().addItem(casterItem);
	}
	
	
	@Override
	public String permissionLabel() {
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
