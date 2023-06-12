package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runicrealms.plugin.RunicCore;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

@CommandAlias("tempunban|untempban")
@CommandPermission("runiccore.tempunban")
public class TempunbanCMD extends BaseCommand {

    private static UUID getUUID(String name) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            String uuid = (((JsonObject) new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();
            return UUID.fromString(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    @Default
    @CatchUnknown
    @Syntax("<player>")
    @CommandCompletion("@online")
    public void onCommand(CommandSender sender, @Single String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            UUID uuid = getUUID(targetName);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Could not find player " + targetName);
                return;
            }
            LuckPermsProvider.get().getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
                boolean tempbanned = false;
                for (Node node : user.getNodes()) {
                    if (!(node instanceof PermissionNode permissionNode)) continue;
                    if (!permissionNode.getPermission().equalsIgnoreCase("runic.tempbanned")) continue;
                    tempbanned = true;
                    break;
                }
                if (!tempbanned) {
                    sender.sendMessage(ChatColor.RED + "That player has not been tempbanned!");
                    return;
                }
                Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " permission unset runic.tempbanned");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " meta unset runic.tempban.timestamp");
                });
                String name;
                if (sender instanceof Player senderPlayer) name = senderPlayer.getName();
                else name = "CONSOLE";
                Bukkit.getOnlinePlayers().stream().filter((player) -> player.hasPermission("runicchat.staff")).forEach(player ->
                        sender.sendMessage(ChatColor.GREEN + "[Staff] " + name + " temp-unbanned " + targetName)
                );
            });
        });
    }

}
