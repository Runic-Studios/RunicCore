package com.runicrealms.plugin.commands.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("discord|gg")
public class DiscordCMD extends BaseCommand {

    @CatchUnknown
    @Default
    public void onCommandDiscord(CommandSender commandSender) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Only a player may run this command!");
            return;
        }
        player.sendMessage("");
        BaseComponent component = new TextComponent(ChatColor.LIGHT_PURPLE + "Click here to open the Runic Realms Discord: " + ChatColor.DARK_PURPLE + "https://discord.gg/runicrealms");
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/5FjVVd4"));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Runic Realms Discord")}));
        player.spigot().sendMessage(component);
        player.sendMessage("");
    }

}
