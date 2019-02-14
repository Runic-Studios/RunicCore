package us.fortherealm.plugin.skillapi.skilltypes;

import org.bukkit.Sound;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.item.GearScanner;

public abstract class Skill implements ISkill, Listener {

    private String name, description;
    private ChatColor color;
    private double cooldown;
    protected Main plugin = Main.getInstance();
    protected boolean doCooldown = true;
    private int manaCost;

    public Skill(String name, String description, ChatColor color, double cooldown, int manaCost) {

        this.name = name;
        this.description = description;
        this.color = color;
        this.cooldown = cooldown;
        this.manaCost = manaCost;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute(Player player, SkillItemType type) {
        if (!Main.getSkillManager().isOnCooldown(player, this)) {
            if (doCooldown) {
                // verify enough mana
                if (!verifyMana(player)) return;
                this.executeSkill(player, type);
            }
        }
    }

    private boolean verifyMana(Player player) {
        int currentMana = Main.getManaManager().getCurrentManaList().get(player.getUniqueId());
        if (currentMana < this.manaCost) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough mana!");
            return false;
        }
        Main.getManaManager().getCurrentManaList().put(player.getUniqueId(), currentMana - this.manaCost);
        Main.getScoreboardHandler().updateSideInfo(player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + getColor() + getName() + ChatColor.GREEN + "!"));
        Main.getSkillManager().addCooldown(player, this, this.getCooldown());
        return true;
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
    public int getManaCost() { return this.manaCost; }


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

    public void executeSkill(Player player, SkillItemType type){}

    // determines which skill to cast
    @Override
    public boolean isFound(ItemStack item, String spellSlot) {
        String spell = AttributeUtil.getSpell(item, spellSlot);
        return spell.equals(getName());
    }
}