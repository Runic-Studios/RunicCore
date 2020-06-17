package com.runicrealms.plugin.group;

import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Group {

    private Set<Player> members;
    private GroupPurpose purpose;
    private ItemStack icon;

    public Group(GroupPurpose purpose) {
        this.purpose = purpose;
        this.members = new HashSet<Player>();
        this.icon = this.purpose.getIcon().clone();
    }

    public GroupPurpose getPurpose() {
        return this.purpose;
    }

    public void addMember(Player player) {
        this.members.add(player);
        this.rebuildIcon();
    }

    public void removeMember(Player player) {
        this.members.remove(player);
        this.rebuildIcon();
    }

    public boolean hasMember(Player player) {
        return this.members.contains(player);
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
