package com.runicrealms.plugin.donator;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.common.util.ColorUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.UUID;

@CommandAlias("addboost")
@Conditions("is-op")
public class AddBoostCommand extends BaseCommand {

//    private static String getUUID(String name) {
//        String uuid = "";
//        try {
//            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
//            uuid = (((JsonObject) new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
//            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
//            in.close();
//        } catch (Exception e) {
//            System.out.println("Unable to get UUID of: " + name + "!");
//            uuid = "er";
//        }
//        return uuid;
//    }

    @CatchUnknown
    @Default
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ColorUtil.format("&cInvalid usage, try: /addboost <target-uuid> <boosttype>"));
            return;
        }
        UUID target;
        try {
            target = UUID.fromString(args[0]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ColorUtil.format("&cInvalid target UUID in arg position 1"));
            return;
        }
        LuckPerms luckPerms = LuckPermsProvider.get();
        UserManager userManager = luckPerms.getUserManager();
        userManager.loadUser(target).thenAcceptAsync(user -> {
            CachedMetaData meta = user.getCachedData().getMetaData();
            int boosts = 1;
            if (meta.getMeta().containsKey("runic.boost." + args[1])) {
                boosts = Integer.parseInt(Objects.requireNonNull(meta.getMetaValue("runic.boost." + args[1]))) + 1;
                user.data().clear(NodeType.META.predicate(metaNode -> metaNode.getMetaKey().equalsIgnoreCase("runic.boost." + args[1])));
            }
            user.data().add(MetaNode.builder("runic.boost." + args[1], Integer.toString(boosts)).build());
            luckPerms.getUserManager().saveUser(user);
        });
    }
}
