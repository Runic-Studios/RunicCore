package us.fortherealm.plugin.command.subcommands.skills;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.skills.caster.itemstack.CasterItemStack;
import us.fortherealm.plugin.skills.skilltypes.cleric.defensive.Windstride;
import us.fortherealm.plugin.skills.skilltypes.mage.offensive.Comet;
import us.fortherealm.plugin.skills.skilltypes.mage.offensive.Discharge;
import us.fortherealm.plugin.skills.skilltypes.mage.offensive.IceNova;
import us.fortherealm.plugin.skills.skilltypes.rogue.offensive.Backstab;
import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Blink;
//import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Heal;
import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Heal;
import us.fortherealm.plugin.skills.skilltypes.runic.defensive.Speed;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Fireball;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Frostbolt;
import us.fortherealm.plugin.skills.skilltypes.warrior.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.warrior.offensive.Enrage;

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
				Arrays.asList(new Discharge()),
				Arrays.asList(new Comet())
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
