package us.fortherealm.plugin.command.commands;

import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.Command;
import us.fortherealm.plugin.skill.skilltypes.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GetRune extends Command {
    public GetRune() {
        super("getrune", "&eObtain a rune with given skill name.", "ftr.command.getrune");
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] params) {
        sender.sendMessage(color("&cThis command may only be run by a player."));
    }

    @Override
    public void onOPCommand(Player sender, String[] params) {
        this.onUserCommand(sender, params);
        // elevated permissions
        /*
        if(params.length == 2 && params[1] == "obtain") {
// spawn a fireball
} else {
this.onUserCommand(sender, params);
}
         */
    }

    @Override
    public void onUserCommand(Player sender, String[] params) {

        if (params.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify a skill name!");
            return;
        }
        String skillname = String.join(" ", params);
        Skill skill = Main.getSkillManager().getSkillByName(skillname);
        if (skill != null) {
            sender.getInventory().setItem(1,baseRune(skill));
        } else {
            sender.sendMessage(color("&cError: Skill does not exist."));
        }
    }

    public ItemStack baseRune(Skill skill) {
        ItemStack baseRune = new ItemStack(Material.INK_SACK, 1, (byte) (Math.random() * 10 + 5));
        ItemMeta runeMeta = baseRune.getItemMeta();
        runeMeta.setDisplayName(ChatColor.YELLOW + "Rune of " + skill.getName());
        ArrayList<String> runeLore = new ArrayList<String>();
        runeLore.add(ChatColor.GRAY + "Skill: " + ChatColor.RED + skill.getName());
        runeLore.add(ChatColor.YELLOW + "Rune");
        runeMeta.setLore(runeLore);
        runeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        baseRune.setItemMeta(runeMeta);
        return baseRune;
    }
}
