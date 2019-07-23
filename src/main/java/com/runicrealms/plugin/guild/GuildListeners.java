package com.runicrealms.plugin.guild;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.professions.Workstation;
import com.runicrealms.plugin.utilities.FilterUtil;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildSkull;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class GuildListeners implements Listener {

    private List<Integer> guildNPCList;
    private List<Player> hasTalked;
    private HashMap<UUID, ActionReason> chatActionMap;
    private final String heraldPrefix = ChatColor.GRAY + "[1/1] " + ChatColor.YELLOW + "Guild Herald: " + ChatColor.GOLD;
    private final ItemStack license = new ItemStack(Material.PAPER);
    private final int TIME_BETWEEN_TALK = 60; // seconds
    private static final int COST = 1000;

    private enum ActionReason {
        PURCHASE,
        NAME,
        PREFIX
    }

    public GuildListeners() {
        this.guildNPCList = new ArrayList<>();
        this.hasTalked = new ArrayList<>();
        this.chatActionMap = new HashMap<>();

        // ------------------------------------
        // ADD NPCS IDS FOR GUILD VENDORS HERE
        // ------------------------------------
        guildNPCList.add(313); // Azana
        guildNPCList.add(476); // Koldore
        guildNPCList.add(477); // DMR
        // Isfodar
        // Hilstead
        guildNPCList.add(478); // Wintervale
        guildNPCList.add(479); // Zenyth
        guildNPCList.add(480); // Naheen
        guildNPCList.add(481); // Naz'mora

        // ------------------------------------

        ItemMeta meta = license.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(ChatColor.YELLOW + "Guild Master's License");
        meta.setLore(Arrays.asList("", ChatColor.GRAY + "Give this to a "
                + ChatColor.YELLOW + "Guild Herald", ChatColor.GRAY + "to create a guild!", "",
                ChatColor.DARK_RED + "" + ChatColor.ITALIC + "If you lose this, you must",
                ChatColor.DARK_RED + "" + ChatColor.ITALIC + "buy another!"));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        license.setItemMeta(meta);
    }

    // ------------------------------------------------------------------
    // GUILD EVENTS
    // ------------------------------------------------------------------

    @EventHandler
    public void onGuildCreate(GuildCreateEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                RunicCore.getTabListManager().setupTab(e.getPlayer());
                RunicCore.getScoreboardHandler().updatePlayerInfo(e.getPlayer());
                RunicCore.getScoreboardHandler().updateSideInfo(e.getPlayer());
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onGuildJoin(GuildJoinEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player guildy : Guilds.getApi().getGuild(e.getPlayer().getUniqueId()).getOnlineAsPlayers()) {
                    RunicCore.getTabListManager().setupTab(guildy);
                    RunicCore.getScoreboardHandler().updatePlayerInfo(guildy);
                    RunicCore.getScoreboardHandler().updateSideInfo(guildy);
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onGuildLeave(GuildLeaveEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player guildy : e.getGuild().getOnlineAsPlayers()) {
                    if (guildy.getUniqueId() != e.getPlayer().getUniqueId()) {
                        RunicCore.getTabListManager().setupTab(guildy);
                        RunicCore.getScoreboardHandler().updatePlayerInfo(guildy);
                        RunicCore.getScoreboardHandler().updateSideInfo(guildy);
                    }
                }
                RunicCore.getTabListManager().setupTab(e.getPlayer());
                RunicCore.getScoreboardHandler().updatePlayerInfo(e.getPlayer());
                RunicCore.getScoreboardHandler().updateSideInfo(e.getPlayer());
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onGuildRemove(GuildRemoveEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player guildy : e.getGuild().getOnlineAsPlayers()) {
                    if (guildy.getUniqueId() != e.getPlayer().getUniqueId()) {
                        RunicCore.getTabListManager().setupTab(guildy);
                        RunicCore.getScoreboardHandler().updatePlayerInfo(guildy);
                        RunicCore.getScoreboardHandler().updateSideInfo(guildy);
                    }
                }
                RunicCore.getTabListManager().setupTab(e.getPlayer());
                RunicCore.getScoreboardHandler().updatePlayerInfo(e.getPlayer());
                RunicCore.getScoreboardHandler().updateSideInfo(e.getPlayer());
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    // ------------------------------------------------------------------
    // CITIZENS EVENTS
    // ------------------------------------------------------------------

    @EventHandler
    public void onGuildClick(NPCRightClickEvent event) {
        if (guildNPCList.contains(event.getNPC().getId())) {
            Player player = event.getClicker();

            if(player.getInventory().getItemInMainHand().equals(license)) {
                player.sendMessage(heraldPrefix + "It seems you have a " + ChatColor.YELLOW + "Guild Master's License" + ChatColor.GOLD + "! What would you like your Guild to be named?");
                player.sendMessage(ChatColor.DARK_AQUA + "Tip " + ChatColor.GOLD + "» " + ChatColor.GRAY + "Type the desired Guild name (Limits: 16 characters, No Spaces) or type \"" + ChatColor.RED + "cancel" + ChatColor.GRAY + "\" to leave!");
                chatActionMap.put(player.getUniqueId(), ActionReason.NAME);
            } else {

                if (player.getInventory().contains(Material.GOLD_NUGGET, COST)) {
                    player.sendMessage(heraldPrefix + "You seem like a trustworthy fellow, would you like to purchase a " + ChatColor.YELLOW + "Guild Master's License" + ChatColor.GOLD + " for " + ChatColor.GREEN + "" + ChatColor.BOLD + COST + "c" + ChatColor.GOLD + "?");
                    player.sendMessage(
                              ChatColor.DARK_AQUA + "Tip "
                            + ChatColor.GOLD + "» "
                            + ChatColor.GRAY + "Type \""
                            + ChatColor.GREEN + "Yes"
                            + ChatColor.GRAY + "\" or \""
                            + ChatColor.RED + "No"
                            + ChatColor.GRAY + "\" to purchase or type \""
                            + ChatColor.RED + "cancel"
                            + ChatColor.GRAY + "\" to leave!"
                                      );

                    chatActionMap.put(player.getUniqueId(), ActionReason.PURCHASE);
                } else if(hasTalked.contains(player)) {
                    player.sendMessage(heraldPrefix + ChatColor.RED + "Sorry! You don't have enough gold coins to purchase a " + ChatColor.YELLOW + "Guild Master's License" + ChatColor.RED + ".");
                } else {
                        player.sendMessage(heraldPrefix + ChatColor.GOLD + "Hey there lad! Would you be interested in a " + ChatColor.YELLOW + "Guild Master's License" + ChatColor.GOLD + " for " + ChatColor.GREEN + "" + ChatColor.BOLD + COST + "c" + ChatColor.GOLD + "?");
                        player.sendMessage(heraldPrefix + ChatColor.RED + "...oh, it doesn't look like you can afford it.");
                        hasTalked.add(player);

                        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                            if(hasTalked.contains(player)) {
                                hasTalked.remove(player);
                            }
                        }, 20 * TIME_BETWEEN_TALK);
                    }
                }
            }
        }

    // ------------------------------------------------------------------
    // PLAYER EVENTS
    // ------------------------------------------------------------------

    private String finalName = "";

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (chatActionMap.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            ActionReason reason = chatActionMap.get(event.getPlayer().getUniqueId());

            switch (reason) {
                case PURCHASE:
                    if (event.getMessage().equalsIgnoreCase("yes"))
                    {

                        Player pl = event.getPlayer();
                        ItemStack[] inv = pl.getInventory().getContents();

                        // take items from player
//                        int goldAmtToRemove = COST;
//                        for (int i = 0; i < inv.length; i++) {
//                            if (goldAmtToRemove <= 0) break;
//                            if (pl.getInventory().getItem(i) == null) continue;
//                            if (Objects.requireNonNull(pl.getInventory().getItem(i)).getType() == Material.GOLD_NUGGET) {
//                                int amt = Objects.requireNonNull(pl.getInventory().getItem(i)).getAmount();
//
//                                if (goldAmtToRemove < 64) amt = goldAmtToRemove;
//                                Objects.requireNonNull(pl.getInventory().getItem(i)).setAmount
//                                        (Objects.requireNonNull(pl.getInventory().getItem(i)).getAmount() - (amt));
//                                goldAmtToRemove -= amt;
//                            }
//                        }

                        Workstation.takeItem(pl, Material.GOLD_NUGGET, COST);

                        event.getPlayer().sendMessage(heraldPrefix + ChatColor.GREEN + "Ah! Great Choice lad, may you and your allies have a bountiful run and grow ever stronger!");
                        chatActionMap.remove(event.getPlayer().getUniqueId());


                        event.getPlayer().getInventory().addItem(license);
                    }
                    else if (event.getMessage().equalsIgnoreCase("no"))
                    {
                        event.getPlayer().sendMessage(heraldPrefix + ChatColor.RED + "Alright then, come back to me when you are ready lad!");
                        chatActionMap.remove(event.getPlayer().getUniqueId());
                    }
                    else if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("exit"))
                    {
                       chatActionMap.remove(event.getPlayer().getUniqueId());
                       event.getPlayer().sendMessage(heraldPrefix + ChatColor.RED + "Alright then, come back to me when you are ready lad!");
                    }
                    else
                    {
                        event.getPlayer().sendMessage(heraldPrefix + ChatColor.GRAY + "I'm sorry lad, I didn't catch that, what did you say?");
                    }
                    break;
                case NAME:
               if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("exit"))
                {
                    chatActionMap.remove(event.getPlayer().getUniqueId());
                    event.getPlayer().sendMessage(heraldPrefix + ChatColor.RED + "Alright then, come back to me when you are ready lad!");
                }
                    String name = event.getMessage();

                    if(!filterInput(name, ActionReason.NAME)) {
                        event.getPlayer().sendMessage(heraldPrefix + ChatColor.RED + "You cannot have this as your guild name, please choose another!");
                        return;
                    }

                    chatActionMap.remove(event.getPlayer().getUniqueId());

                    event.getPlayer().sendMessage(heraldPrefix + "You have registered a Guild with the name " + ChatColor.WHITE + name + ChatColor.GOLD + ", what would you like your Guild Prefix to be?");
                    event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Tip " + ChatColor.GOLD + "» " + ChatColor.GRAY + "Type the desired Guild prefix (Limits: 3 characters, No Spaces) or type \"" + ChatColor.RED + "cancel" + ChatColor.GRAY + "\" to leave!");

                    this.finalName = name;

                    chatActionMap.put(event.getPlayer().getUniqueId(), ActionReason.PREFIX);
                    break;
                case PREFIX:
                    if (event.getMessage().equalsIgnoreCase("cancel") || event.getMessage().equalsIgnoreCase("exit"))
                    {
                        chatActionMap.remove(event.getPlayer().getUniqueId());
                        event.getPlayer().sendMessage(heraldPrefix + ChatColor.RED + "Alright then, come back to me when you are ready lad!");
                    }
                    String prefix = event.getMessage();

                    if(!filterInput(prefix, ActionReason.PREFIX)) {
                        event.getPlayer().sendMessage(heraldPrefix + ChatColor.RED + "You cannot have this as your guild prefix, please choose another!");
                        return;
                    }

                    chatActionMap.remove(event.getPlayer().getUniqueId());

                    event.getPlayer().sendMessage(heraldPrefix + "You have chose the prefix " + ChatColor.WHITE + prefix.toUpperCase() + ChatColor.GOLD + "!");
                    event.getPlayer().getInventory().remove(license);
                    createGuild(event.getPlayer(), finalName, prefix.toUpperCase());
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if(chatActionMap.containsKey(event.getPlayer().getUniqueId())) {
            chatActionMap.remove(event.getPlayer().getUniqueId());
        }
        if(hasTalked.contains(event.getPlayer())) {
            hasTalked.remove(event.getPlayer());
        }
    }

    // ------------------------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------------------------

    private boolean filterInput(String string, ActionReason reason) {
        int length = string.length();
        if(reason.equals(ActionReason.NAME)) {
            if(length > 16 || length < 5) return false;
            if(filterString(string)) return false;
            if(Guilds.getApi().getGuild(string) != null) return false;
            return true;
        }
        else if(reason.equals(ActionReason.PREFIX)) {
            if(length != 3) return false;
            if(filterString(string)) return false;
            if(string.contains(" ")) return false;

            for(Guild guild : Guilds.getApi().getGuildHandler().getGuilds())
                if(guild.getPrefix() == string) return false;

            return true;
        }
        return true;
    }

    private boolean filterString(String string) {
        return FilterUtil.hasFilterWord(string);
    }

    private void createGuild(Player player, String name, String prefix) {
        Guild.GuildBuilder gb = Guild.builder();
        gb.id(UUID.randomUUID());
        gb.name(name.replace(" ", ""));
        gb.prefix(prefix);
        gb.status(Guild.Status.Private);
        gb.guildSkull(new GuildSkull(player));

        GuildMember master = new GuildMember(player.getUniqueId(), Guilds.getApi().getGuildHandler().getGuildRole(0), 0);
        gb.guildMaster(master);

        List<GuildMember> members = new ArrayList<>();
        members.add(master);

        gb.members(members);
        gb.home(null);
        gb.balance(0);
        gb.tier(Guilds.getApi().getGuildHandler().getGuildTier(1));

        gb.invitedMembers(new ArrayList<>());
        gb.allies(new ArrayList<>());
        gb.pendingAllies(new ArrayList<>());

        gb.vaults(new ArrayList<>());
        gb.codes(new ArrayList<>());

        Guild guild = gb.build();

        GuildCreateEvent event = new GuildCreateEvent(player, guild);
        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled()) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.5f);
            player.sendMessage(ChatColor.GREEN + "Your guild has been registered!");
            Guilds.getApi().getGuildHandler().addGuild(guild);
        }
    }
}