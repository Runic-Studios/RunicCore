package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.classes.ClassGUI;

import java.util.List;

public class Help implements SubCommand {
	
	private PartySC party;
	
	public Help(PartySC party) {
		this.party = party;
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
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GRAY + "Available commands: "
                        + ChatColor.YELLOW + "create, disband, help, invite, join, kick, leave");

        ClassGUI.CLASS_SELECTION.open(sender);

//        ItemStack silverKey = new ItemStack(Material.SHEARS);
//        ItemMeta meta = silverKey.getItemMeta();
//        ((Damageable) meta).setDamage(100);
//        silverKey.setItemMeta(meta);
//        sender.getInventory().setItem(7, silverKey);
//
//		ItemStack goldenKey = new ItemStack(Material.SHEARS);
//		ItemMeta meta2 = goldenKey.getItemMeta();
//		((Damageable) meta2).setDamage(200);
//		goldenKey.setItemMeta(meta2);
//		sender.getInventory().setItem(8, goldenKey);
//
		item(sender, 5, 3);
		item(sender, 10, 4);
		item(sender, 15, 5);
		item(sender, 20, 6);
		item(sender, 25, 7);
	}

	private void item(Player sender, int durab, int slot) {
		ItemStack cloth = new ItemStack(Material.SHEARS);
		ItemMeta metaCloth = cloth.getItemMeta();
		((Damageable) metaCloth).setDamage(durab);
		cloth.setItemMeta(metaCloth);
		sender.getInventory().setItem(slot, cloth);
	}
	
	@Override
	public String permissionLabel() {
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
