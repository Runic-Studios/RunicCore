package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.utilities.ActionBarUtil;
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

public abstract class Spell implements ISpell, Listener {

    private boolean isPassive = false;
    private final int manaCost;
    private final double cooldown;
    private final String name;
    private final String description;
    private final ChatColor color;
    private final ClassEnum reqClass;
    protected RunicCore plugin = RunicCore.getInstance();

    public Spell(String name, String description, ChatColor color,
                 ClassEnum reqClass, double cooldown, int manaCost) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.reqClass = reqClass;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void execute(Player pl, SpellItemType type) {

        if (RunicCore.getSpellManager().isOnCooldown(pl, this.getName())) return; // ensure spell is not on cooldown

        // verify class
        boolean canCast = false;
        if (this.getReqClass() != ClassEnum.RUNIC) {
            if (this.getReqClass().toString().toLowerCase().equals
                    (RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName().toLowerCase()))
                canCast = true;
        }

        if (!canCast) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            ActionBarUtil.sendTimedMessage(pl, "&cYour class cannot cast this spell!", 3);
            return;
        }

        if (!verifyMana(pl)) return; // verify the mana
        if (!this.attemptToExecute(pl)) return; // check additional conditions

        // cast the spell
        int currentMana = RunicCore.getRegenManager().getCurrentManaList().get(pl.getUniqueId());
        RunicCore.getRegenManager().getCurrentManaList().put(pl.getUniqueId(), currentMana - this.manaCost);
        pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + getColor() + getName() + ChatColor.GREEN + "!"));
        RunicCore.getSpellManager().addCooldown(pl, this, this.getCooldown());
        this.executeSpell(pl, type);
    }

    private boolean verifyMana(Player player) {
        int currentMana = RunicCore.getRegenManager().getCurrentManaList().get(player.getUniqueId());
        if (currentMana < this.manaCost) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            ActionBarUtil.sendTimedMessage(player, "&cYou don't have enough mana!", 2);
            return false;
        }
        return true;
    }

    /**
     * Method to check for valid enemy before applying healing calculation. True if enemy can be healed.
     * @param caster player who used spell
     * @param ally player who was hit by spell
     * @return whether target is valid
     */
    @Override
    public boolean verifyAlly(Player caster, Entity ally) {

        // target must be alive
        if (!(ally instanceof LivingEntity)) return false;
        LivingEntity livingAlly = (LivingEntity) ally;

        // ignore NPCs
        if (livingAlly.hasMetadata("NPC")) return false;
        if (livingAlly instanceof ArmorStand) return false;

        // skip the target player if the caster has a party and the target is NOT in it
        if (ally instanceof Player) {
            return RunicCore.getPartyManager().getPlayerParty(caster) == null
                    || RunicCore.getPartyManager().getPlayerParty(caster).hasMember((Player) ally);
        }
        return true;
    }

    /**
     * Method to check for valid enemy before applying damage calculation. True if enemy can be damaged.
     * @param caster player who used spell
     * @param victim mob or player who was hit by spell
     * @return whether target is valid
     */
    @Override
    public boolean verifyEnemy(Player caster, Entity victim) {
        EnemyVerifyEvent e = new EnemyVerifyEvent(caster, victim);
        Bukkit.getServer().getPluginManager().callEvent(e);
        return !e.isCancelled();
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
        try {
            String spell = AttributeUtil.getSpell(pl.getInventory().getItem(0), "primarySpell"); // passive
            return RunicCore.getSpellManager().getSpellByName(spell);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isPassive() {
        return isPassive;
    }

    public void setIsPassive(boolean isPassive) {
        this.isPassive = isPassive;
    }

    public boolean attemptToExecute(Player pl) {
        return true;
    }

    public void executeSpell(Player player, SpellItemType type) {

    }

    // determines which spell to cast
    @Override
    public boolean isFound(ItemStack item, String spellSlot) {
        String spell = AttributeUtil.getSpell(item, spellSlot);
        return spell.equals(getName());
    }

    /**
     * Add a custom status effect to an entity.
     * @param entity to be silenced
     * @param effectEnum which status effect to add
     * @param duration (in seconds) of effect
     */
    @Override
    public void addStatusEffect(Entity entity, EffectEnum effectEnum, int duration) {
        if (effectEnum == EffectEnum.SILENCE) {
            RunicCore.getSpellManager().getSilencedEntities().add(entity.getUniqueId());
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 0.5f, 0.2f);
            Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin,
                    () -> RunicCore.getSpellManager().getSilencedEntities().remove(entity.getUniqueId()), duration * 20L);
        }
    }

}