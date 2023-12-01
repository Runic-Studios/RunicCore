package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandAlias("distribute")
@CommandPermission("runic.op")
@Conditions("is-op")
public class DistributeCommand extends BaseCommand {
    @CatchUnknown
    @Default
    private void onHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.format("""
                &cCommand usage:
                /distribute mob <uuid> <template-id,template-id,template-id> <probability,probability,probability>
                /distribute party <player> <template-id,template-id,template-id> <probability,probability,probability>"""));
    }

    @Subcommand("mob")
    @CommandCompletion("@nothing")
    private void onMobDistribute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ColorUtil.format("&cInvalid arguments!"));
            this.onHelp(sender);
            return;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ColorUtil.format("&cThe first argument is not a uuid!"));
            return;
        }

        Optional<ActiveMob> optional = MythicBukkit.inst().getMobManager().getActiveMob(uuid);

        if (optional.isEmpty()) {
            sender.sendMessage(ColorUtil.format("&cNo mythicmob with that uuid is spawned!"));
            return;
        }

        String[] templateIds = args[1].split(",");
        String[] rawChances = args[2].split(",");

        Set<Player> targets = optional.get().getThreatTable().getAllThreatTargets()
                .stream()
                .map(AbstractEntity::getBukkitEntity)
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toSet());

        this.execute(sender, templateIds, rawChances, targets);
    }

    @Subcommand("party")
    @CommandCompletion("@players @nothing")
    private void onPartyDistribute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ColorUtil.format("&cInvalid arguments!"));
            this.onHelp(sender);
            return;
        }

        Player player = Bukkit.getPlayerExact(args[0]);

        if (player == null) {
            sender.sendMessage(ColorUtil.format("&cThat player is not online!"));
            return;
        }

        String[] templateIds = args[1].split(",");
        String[] rawChances = args[2].split(",");

        Set<Player> targets = new HashSet<>();

        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());

        if (party != null) {
            targets.addAll(party.getMembersWithLeader());
        } else {
            targets.add(player);
        }

        this.execute(sender, templateIds, rawChances, targets);
    }

    /**
     * A method which contains the main logic for the command
     *
     * @param sender      the sender of the command
     * @param templateIds the ids of the items
     * @param rawChances  the pre-parsed proportions
     * @param targets     the targets to iterate over
     */
    private void execute(@NotNull CommandSender sender, @NotNull String[] templateIds, @NotNull String[] rawChances, @NotNull Set<Player> targets) {
        if (templateIds.length != rawChances.length) {
            sender.sendMessage(ColorUtil.format("&cThe provided templates and probabilities do not match"));
            return;
        }

        double[] chances = new double[rawChances.length];
        for (int i = 0; i < rawChances.length; i++) {
            double parsed;
            try {
                parsed = Double.parseDouble(rawChances[i]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.format("&cNot all of the provided probabilities are real numbers!"));
                return;
            }

            chances[i] = parsed;
        }

        for (Player player : targets) {
            for (int i = 0; i < templateIds.length; i++) {
                if (RunicItemsAPI.getTemplate(templateIds[i]) == null) {
                    sender.sendMessage(ColorUtil.format("&c" + templateIds[i] + " is not a valid runic item id!"));
                    return;
                }

                RunicItem item = RunicItemsAPI.generateItemFromTemplate(templateIds[i]);

                if (chances[i] >= Math.random()) {
                    RunicItemsAPI.addItem(player.getInventory(), item.generateItem());
                }
            }
        }
    }
}
