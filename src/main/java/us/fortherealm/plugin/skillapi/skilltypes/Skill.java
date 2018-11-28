package us.fortherealm.plugin.skillapi.skilltypes;

import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public abstract class Skill implements ISkill, Listener {
    private String name, description;
    private ChatColor color;
    private ClickType clickType;
    private double cooldown;
    protected Main plugin = Main.getInstance();
    protected boolean doCooldown = true;

    public Skill(String name, String description, ChatColor color, ClickType clickType, double cooldown) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.clickType = clickType;
        this.cooldown = cooldown;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute(Player player, Action action, SkillItemType type) {
        switch(action){
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                if(this.clickType.equals(ClickType.LEFT_CLICK_ONLY) || this.clickType.equals(ClickType.BOTH)) {
                    if(!Main.getSkillManager().isOnCooldown(player, this)) {
                        this.onLeftClick(player, type);
                        if (doCooldown) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + getColor() + getName() + ChatColor.GREEN + "!"));
                            Main.getSkillManager().addCooldown(player, this, this.getCooldown());
                        }
                    }
                }
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                if(this.clickType.equals(ClickType.RIGHT_CLICK_ONLY) || this.clickType.equals(ClickType.BOTH)) {
                    if(!Main.getSkillManager().isOnCooldown(player, this)) {
                        this.onRightClick(player, type);
                        if (doCooldown) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + getColor() + getName() + ChatColor.GREEN + "!"));
                            Main.getSkillManager().addCooldown(player, this, this.getCooldown());
                        }
                    }
                }

                break;
            default:
                break;
        }

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ChatColor getColor() {
        return this.color;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public double getCooldown() {
        return this.cooldown;
    }

    @Override
    public ClickType getClickType() { return this.clickType; }


    public Vector rotateVectorAroundY(Vector vector, double degrees) {
        Vector newVector = vector.clone();
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sine = Math.sin(rad);
        double x = vector.getX();
        double z = vector.getZ();
        newVector.setX(cos * x - sine * z);
        newVector.setZ(sine * x + cos * z);
        return newVector;
    }

    public void onRightClick(Player player, SkillItemType type){}

    public void onLeftClick(Player player, SkillItemType type){}

    @Override
    public boolean isItem(ItemStack item)
    {
        if (!item.hasItemMeta()) {
            return false;
        }
        if (!item.getItemMeta().hasLore()) {
            return false;
        }
        if (item.hasItemMeta())
        {
            String loreAsString = ChatColor.stripColor(String.join(" ", item.getItemMeta().getLore()));
            return loreAsString.contains(getName());
        }
        return false;
    }


    public enum ClickType {
        LEFT_CLICK_ONLY,
        RIGHT_CLICK_ONLY,
        BOTH
    }
}