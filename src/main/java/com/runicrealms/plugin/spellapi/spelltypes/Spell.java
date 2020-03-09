package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
import com.runicrealms.plugin.utilities.ActionBarUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;

public abstract class Spell implements ISpell, Listener {

    private String name, description;
    private ChatColor color;
    private ClassEnum reqClass;
    private double cooldown;
    protected RunicCore plugin = RunicCore.getInstance();
    private int manaCost;
    private boolean isPassive = false;

    public Spell(String name, String description, ChatColor color, ClassEnum reqClass, double cooldown, int manaCost) {

        this.name = name;
        this.description = description;
        this.color = color;
        this.reqClass = reqClass;
        this.cooldown = cooldown;
        this.manaCost = manaCost;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute(Player player, SpellItemType type) {

        if (!RunicCore.getSpellManager().isOnCooldown(player, this)) { // ensure spell is not on cooldown

            // verify class
            if (this.getReqClass() != ClassEnum.RUNIC) {
                if (!this.getReqClass().toString().toLowerCase().equals
                        (RunicCore.getCacheManager().getPlayerCache(player.getUniqueId()).getClassName().toLowerCase())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                    ActionBarUtil.sendTimedMessage(player, "&cYour class cannot cast this spell!", 3);
                    return;
                }
            }

            if (!verifyMana(player)) return; // verify the mana

            if (!this.attemptToExecute(player)) return; // check additional conditions

            // cast the spell
            int currentMana = RunicCore.getManaManager().getCurrentManaList().get(player.getUniqueId());
            RunicCore.getManaManager().getCurrentManaList().put(player.getUniqueId(), currentMana - this.manaCost);
            RunicCore.getScoreboardHandler().updateSideInfo(player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + getColor() + getName() + ChatColor.GREEN + "!"));
            RunicCore.getSpellManager().addCooldown(player, this, this.getCooldown());
            this.executeSpell(player, type);
        }
    }

    private boolean verifyMana(Player player) {
        int currentMana = RunicCore.getManaManager().getCurrentManaList().get(player.getUniqueId());
        if (currentMana < this.manaCost) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            ActionBarUtil.sendTimedMessage(player, "&cYou don't have enough mana!", 2);
            return false;
        }
        return true;
    }

    @Override
    public boolean verifyAlly(Player caster, Entity ally) {

        // target must be alive
        if (!(ally instanceof LivingEntity)) return false;
        LivingEntity livingAlly = (LivingEntity) ally;

        // ignore NPCs
        if (livingAlly.hasMetadata("NPC")) return false;
        if (livingAlly instanceof ArmorStand) return false;

        // skip the player if they're not in the party
        return RunicCore.getPartyManager().getPlayerParty(caster) == null
                || RunicCore.getPartyManager().getPlayerParty(caster).hasMember(ally.getUniqueId());
    }

    @Override
    public boolean verifyEnemy(Player caster, Entity victim) {

        // bugfix for armor stands
        if (victim instanceof ArmorStand) return false;

        // target must be alive
        if (!(victim instanceof LivingEntity)) return false;
        LivingEntity livingVictim = (LivingEntity) victim;

        // ignnore caster
        if (caster.equals(victim)) return false;

        // ignore NPCs
        if (livingVictim.hasMetadata("NPC")) return false;

        // outlaw check
        if (livingVictim instanceof Player && (!OutlawManager.isOutlaw(((Player) livingVictim)) || !OutlawManager.isOutlaw(caster))) {

            // for PvP zones, grab all regions the player is standing in
            // -----------------------------------------------------------------------------------------
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(caster.getLocation()));
            Set<ProtectedRegion> regions = set.getRegions();
            if (regions == null) return false;
            for (ProtectedRegion region : regions) {
                if (region.getId().contains("pvp")) {
                    return true;
                }
            }
            // -----------------------------------------------------------------------------------------
            return false;
        }

        // skip party members
        return RunicCore.getPartyManager().getPlayerParty(caster) == null
                || !RunicCore.getPartyManager().getPlayerParty(caster).hasMember(victim.getUniqueId());
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
    public ClassEnum getReqClass() {
        return reqClass;
    }

    @Override
    public double getCooldown() {
        return this.cooldown;
    }

    @Override
    public int getManaCost() { return this.manaCost; }


    protected Vector rotateVectorAroundY(Vector vector, double degrees) {
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

    public Spell getRunicPassive(Player pl) {
        String spell = AttributeUtil.getSpell(pl.getInventory().getItem(0), "primarySpell"); // passive
        return RunicCore.getSpellManager().getSpellByName(spell);
    }

    public boolean getIsPassive() {
        return isPassive;
    }

    public void setIsPassive(boolean isPassive) {
        this.isPassive = isPassive;
    }

    public boolean attemptToExecute(Player pl) {
        return true;
    }

    public void executeSpell(Player player, SpellItemType type){}

    // determines which spell to cast
    @Override
    public boolean isFound(ItemStack item, String spellSlot) {
        String spell = AttributeUtil.getSpell(item, spellSlot);
        return spell.equals(getName());
    }
}