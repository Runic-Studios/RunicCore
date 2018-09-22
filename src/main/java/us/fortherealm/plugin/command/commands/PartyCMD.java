package us.fortherealm.plugin.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.Command;
import us.fortherealm.plugin.parties.Invite;
import us.fortherealm.plugin.parties.Party;

public class PartyCMD extends Command {

    public PartyCMD() {
        super("party", "Party Command", "ftr.party");
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] params) {
        sender.sendMessage("Only players can run this command!");
    }

    @Override
    public void onOPCommand(Player sender, String[] params) {
        if(params.length == 0) {
            // TODO: Open PartyCMD GUI
            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                    ChatColor.RED + "Type " + ChatColor.AQUA + "/party help" + ChatColor.RED + " to see commands!");
            return;
        }

        if(params[0].equalsIgnoreCase("admin"))
        {
            switch(params[1]) {
                case "disband":
                    String targetPlayer = params[1];
                    Player target = Bukkit.getPlayer(targetPlayer);

                    if (target != null) {
                        Party targetParty = Main.getPartyManager().getPlayerParty(target);
                        if(targetParty != null) {
                            Main.getPartyManager().disbandParty(targetParty);
                            sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                    ChatColor.RED + "You have forcefully disbanded " + ChatColor.GOLD + target.getName() + ChatColor.RED + "'s party!");
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                    ChatColor.GOLD + target.getName() + ChatColor.RED + " is not in a party!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "Player " + ChatColor.GOLD + params[2] + ChatColor.RED + " not found!");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.RED + "Subcommand " + ChatColor.GOLD + params[1] + ChatColor.RED + " not found!");
            }
        } else {
            this.onUserCommand(sender, params);
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] params) {
        if(params.length == 0) {
            // TODO: Open PartyCMD GUI
            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                    ChatColor.RED + "Type " + ChatColor.AQUA + "/party help" + ChatColor.RED + " to see commands!");
            return;
        }

        Party party = Main.getPartyManager().getPlayerParty(sender);

        switch(params[0].toLowerCase()) {
            case "help":
                // TODO: Help Screen
                break;
            case "create":
                if(party == null) {
                    Main.getPartyManager().addParty(new Party(sender.getUniqueId()));
                    sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.GREEN + "You have created a party!");
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.RED + "You are already in a party!");
                }
                break;
            case "disband":
                if(party != null) {
                    if(party.getLeader().equals(sender.getUniqueId())) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You disbanded your party!");
                        party.sendMemberMessage("&3&lParty &7&l> &cYour party has been disbanded. &7Reason: Leader Left");
                        Main.getPartyManager().disbandParty(party);
                    } else {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You must be the party leader to do this!");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.RED + "You must be in a party to do this!");
                }
                break;
            case "invite":
                if(party != null) {
                    if(party.getLeader().equals(sender.getUniqueId())) {
                        if(params[1] != null) {
                            Player target = Bukkit.getPlayer(params[1]);
                            if(target != null) {
                                if(Main.getPartyManager().addInvite(new Invite(party, target))) {
                                    party.sendMessage("&3&lParty &7&l> &6" + sender.getName() + " &ahas invited &e" + params[1] + " &a to the party!");

                                    target.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                            ChatColor.GREEN + "You have been invited to " + ChatColor.GOLD + sender.getName() + ChatColor.GREEN + "'s party! " +
                                            "Type " + ChatColor.AQUA + "/party join" + ChatColor.GREEN + " to join!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                        ChatColor.RED + "Player " + ChatColor.GOLD + params[1] + ChatColor.RED + " not found!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                    ChatColor.RED + "Not Enough Arguments");
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You must be the party leader to do this!");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.RED + "You must be in a party to do this!");
                }
                break;
            case "leave":
                if(party != null) {
                    if(party.getLeader().equals(sender.getUniqueId())) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You left your party!");
                        party.sendMemberMessage("&3&lParty &7&l> &cYour party has been disbanded. &7Reason: Leader Left");
                        Main.getPartyManager().disbandParty(party);
                    } else {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You left your party!");
                        party.removeMember(sender.getUniqueId());
                        party.sendMessage("&3&lParty &7&l> &6" + sender.getName() + " &chas left the party!");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.RED + "You must be in a party to do this!");
                }
                break;
            case "kick":
                if(party != null) {
                    if(party.getLeader().equals(sender.getUniqueId())) {
                        if(params[1] != null) {
                            Player target = Bukkit.getPlayer(params[1]);

                            if(target != null) {
                                if(target.getUniqueId().equals(sender.getUniqueId())) {
                                    party.sendMessage("&3&lParty &7&l> &cYou can't kick yourself from the party!");
                                    break;
                                }
                                sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                        ChatColor.RED + "You kicked " + ChatColor.GOLD + target.getName() + ChatColor.RED + " from your party!");
                                party.removeMember(target.getUniqueId());
                                party.sendMemberMessage("&3&lParty &7&l> &6" + sender.getName() + " &chas kicked &e" + params[1] + " &c from the party!");
                            } else {
                                sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                        ChatColor.RED + "Player " + ChatColor.GOLD + params[1] + ChatColor.RED + " not found!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                    ChatColor.RED + "Not Enough Arguments");
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You must be the party leader to do this!");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                            ChatColor.RED + "You must be in a party to do this!");
                }
                break;
            case "join":
                if(party == null) {
                    Invite invite = Main.getPartyManager().getActiveInvite(sender);
                    if(invite != null) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.GREEN + "You joined " + ChatColor.GOLD + invite.getInviter() + ChatColor.GREEN + "'s party!");
                        invite.getParty().sendMemberMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.GOLD +  sender.getName() + ChatColor.GREEN + " has joined the party!");
                        invite.getParty().addMember(sender.getUniqueId());
                        Main.getPartyManager().removeInvite(invite);
                    } else {
                        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
                                ChatColor.RED + "You don't have an invite to a party!");
                    }
                }
                break;
            default:
                break;
        }
    }
}

