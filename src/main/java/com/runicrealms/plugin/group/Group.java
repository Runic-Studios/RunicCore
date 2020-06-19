package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.GUIItem;
import com.runicrealms.plugin.utilities.HeadUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Group {

    private Set<Player> members;
    private Map<Player, ItemStack> memberIcons;
    private GroupPurpose purpose;
    private ItemStack icon;

    public Group(GroupPurpose purpose) {
        this.purpose = purpose;
        this.members = new HashSet<Player>();
        this.memberIcons = new HashMap<Player, ItemStack>();
        this.icon = this.purpose.getIcon().clone();
    }

    public GroupPurpose getPurpose() {
        return this.purpose;
    }

    public void addMember(Player player) {
        this.members.add(player);
        this.rebuildIcon();
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            this.memberIcons.put(player, GUIItem.setName(HeadUtil.getHead(player), "&e" + player.getName()));
        });
    }

    public void removeMember(Player player) {
        this.members.remove(player);
        this.rebuildIcon();
        this.memberIcons.remove(player);
    }

    public boolean hasMember(Player player) {
        return this.members.contains(player);
    }

    public Map<Player, ItemStack> getMemberIcons() {
        return this.memberIcons;
    }

    public void rebuildIcon() {
        this.icon = this.purpose.getIcon().clone();
        StringBuilder builder = new StringBuilder("&7");
        int count = 0;
        for (Player player : this.members) {
            builder.append(player.getName());
            count += player.getName().length();
            if (count >= 20) {
                builder.append("\n&7");
                count = 0;
            }
        }
        GUIItem.setLore(this.icon, builder.toString().split("\n"));
        GUIItem.setName(this.icon, this.icon.getItemMeta().getDisplayName() + " &6" + "[" + this.members.size() + "/" + this.purpose.getMaxMembers() + "]");
    }

    public ItemStack getIcon() {
        return this.icon;
    }

}
