package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.AllyVerifyEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.utilities.ActionBarUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.UUID;

public abstract class Spell implements ISpell, Listener {

    private final int manaCost;
    private final double cooldown;
    private final String name;
    private final String description;
    private final ChatColor color;
    private final CharacterClass reqClass;
    protected RunicCore plugin = RunicCore.getInstance();
    private boolean isPassive = false;

    public Spell(String name, String description, ChatColor color,
                 CharacterClass reqClass, double cooldown, int manaCost) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.reqClass = reqClass;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Add a custom status effect to an entity.
     *
     * @param entity            to be silenced
     * @param runicStatusEffect which status effect to add
     * @param durationInSecs    (in seconds) of effect
     */
    @Override
    public void addStatusEffect(Entity entity, RunicStatusEffect runicStatusEffect, double durationInSecs) {
        RunicCore.getSpellAPI().addStatusEffect(entity, runicStatusEffect, durationInSecs);
    }

    @Override
    public void execute(Player player, SpellItemType type) {

        if (isOnCooldown(player)) return; // ensure spell is not on cooldown
        UUID uuid = player.getUniqueId();

        // verify class
        boolean canCast = this.getReqClass() == CharacterClass.ANY
                || this.getReqClass().toString().equalsIgnoreCase(RunicCore.getCharacterAPI().getPlayerClass(uuid));

        if (!canCast) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            ActionBarUtil.sendTimedMessage(player, "&cYour class cannot cast this spell!", 3);
            return;
        }

        if (!this.attemptToExecute(player)) return; // check additional conditions

        // cast the spell
        int currentMana = RunicCore.getRegenManager().getCurrentManaList().get(player.getUniqueId());
        RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), currentMana - this.manaCost);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + getColor() + getName() + ChatColor.GREEN + "!"));
        RunicCore.getSpellAPI().addCooldown(player, this, this.getCooldown());
        this.executeSpell(player, type);
    }

    @Override
    public ChatColor getColor() {
        return this.color;
    }

    @Override
    public double getCooldown() {
        return this.cooldown;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public int getManaCost() {
        return this.manaCost;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public CharacterClass getReqClass() {
        return reqClass;
    }

    @Override
    public boolean hasPassive(UUID uuid, String passive) {
        return RunicCore.getSkillTreeAPI().hasPassiveFromSkillTree(uuid, passive);
    }

    @Override
    public boolean isInvulnerable(Entity entity) {
        return RunicCore.getSpellAPI().isInvulnerable(entity);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return RunicCore.getSpellAPI().isOnCooldown(player, this.getName());
    }

    @Override
    public boolean isRooted(Entity entity) {
        return RunicCore.getSpellAPI().isRooted(entity);
    }

    @Override
    public boolean isSilenced(Entity entity) {
        return RunicCore.getSpellAPI().isSilenced(entity);
    }

    @Override
    public boolean isStunned(Entity entity) {
        return RunicCore.getSpellAPI().isStunned(entity);
    }

    @Override
    public boolean isValidAlly(Player caster, Entity ally) {
        AllyVerifyEvent allyVerifyEvent = new AllyVerifyEvent(caster, ally);
        Bukkit.getServer().getPluginManager().callEvent(allyVerifyEvent);
        return !allyVerifyEvent.isCancelled();
    }

    @Override
    public boolean isValidEnemy(Player caster, Entity victim) {
        EnemyVerifyEvent enemyVerifyEvent = new EnemyVerifyEvent(caster, victim);
        Bukkit.getServer().getPluginManager().callEvent(enemyVerifyEvent);
        return !enemyVerifyEvent.isCancelled();
    }

    @Override
    public int percentMissingHealth(Entity entity, double percent) {
        if (!(entity instanceof LivingEntity)) return 0;
        LivingEntity livingEntity = (LivingEntity) entity;
        double max = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double missing = max - livingEntity.getHealth();
        return (int) (missing * percent);
    }

    @Override
    public boolean removeStatusEffect(Entity entity, RunicStatusEffect runicStatusEffect) {
        return RunicCore.getSpellAPI().removeStatusEffect(entity.getUniqueId(), runicStatusEffect);
    }

    public boolean attemptToExecute(Player player) {
        return true;
    }

    public void executeSpell(Player player, SpellItemType type) {

    }

    public boolean isPassive() {
        return isPassive;
    }

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

    public void setIsPassive(boolean isPassive) {
        this.isPassive = isPassive;
    }
}