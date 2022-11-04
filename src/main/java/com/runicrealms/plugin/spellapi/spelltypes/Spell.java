package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.api.event.AllyVerifyEvent;
import com.runicrealms.plugin.classes.ClassEnum;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.UUID;

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
    public void execute(Player player, SpellItemType type) {

        if (isOnCooldown(player)) return; // ensure spell is not on cooldown
        UUID uuid = player.getUniqueId();

        // verify class
        boolean canCast = this.getReqClass() == ClassEnum.ANY || this.getReqClass().toString().equalsIgnoreCase(RunicCoreAPI.getPlayerClass(uuid));

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
        RunicCore.getSpellManager().addCooldown(player, this, this.getCooldown());
        this.executeSpell(player, type);
    }

    /**
     * Method to check for valid enemy before applying healing / buff spell calculation. True if enemy can be healed.
     *
     * @param caster player who used spell
     * @param ally   entity who was hit by spell
     * @return whether target is valid
     */
    @Override
    public boolean isValidAlly(Player caster, Entity ally) {
        AllyVerifyEvent allyVerifyEvent = new AllyVerifyEvent(caster, ally);
        Bukkit.getServer().getPluginManager().callEvent(allyVerifyEvent);
        return !allyVerifyEvent.isCancelled();
    }

    @Override
    public boolean hasPassive(UUID uuid, String passive) {
        return RunicCoreAPI.hasPassive(uuid, passive);
    }

    /**
     * Method to check for valid enemy before applying damage calculation. True if enemy can be damaged.
     *
     * @param caster player who used spell
     * @param victim mob or player who was hit by spell
     * @return whether target is valid
     */
    @Override
    public boolean isValidEnemy(Player caster, Entity victim) {
        EnemyVerifyEvent enemyVerifyEvent = new EnemyVerifyEvent(caster, victim);
        Bukkit.getServer().getPluginManager().callEvent(enemyVerifyEvent);
        return !enemyVerifyEvent.isCancelled();
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
    public int getManaCost() {
        return this.manaCost;
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

    /**
     * Add a custom status effect to an entity.
     *
     * @param entity         to be silenced
     * @param effectEnum     which status effect to add
     * @param durationInSecs (in seconds) of effect
     */
    @Override
    public void addStatusEffect(Entity entity, EffectEnum effectEnum, double durationInSecs) {
        if (effectEnum == EffectEnum.SILENCE) {
            entity.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "silenced!");
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 0.5f, 1.0f);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    RunicCore.getSpellManager().getSilencedEntities().remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            RunicCore.getSpellManager().getSilencedEntities().put(entity.getUniqueId(), task);
        } else if (effectEnum == EffectEnum.STUN) {
            entity.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "stunned!");
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    RunicCore.getSpellManager().getStunnedEntities().remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            RunicCore.getSpellManager().getStunnedEntities().put(entity.getUniqueId(), task);
            if (!(entity instanceof Player)) { // since there's no entity move event, we do it the old fashioned way for mobs
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 3));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (durationInSecs * 20), 127));
            }
        } else if (effectEnum == EffectEnum.ROOT) {
            entity.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + ChatColor.BOLD + "rooted!");
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    RunicCore.getSpellManager().getRootedEntites().remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            RunicCore.getSpellManager().getRootedEntites().put(entity.getUniqueId(), task);
            if (!(entity instanceof Player)) { // since there's no entity move event, we do it the old fashioned way for mobs
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (durationInSecs * 20), 3));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (durationInSecs * 20), 127));
            }
        } else if (effectEnum == EffectEnum.INVULN) {
            entity.sendMessage(ChatColor.GREEN + "You are now " + ChatColor.DARK_GREEN + ChatColor.BOLD + "invulnerable!");
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.1f);
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    RunicCore.getSpellManager().getInvulnerableEntities().remove(entity.getUniqueId());
                }
            }.runTaskLaterAsynchronously(plugin, (long) (durationInSecs * 20L));
            RunicCore.getSpellManager().getInvulnerableEntities().put(entity.getUniqueId(), task);
        }
    }

    @Override
    public boolean isInvulnerable(Entity entity) {
        return RunicCore.getSpellManager().getInvulnerableEntities().containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return RunicCore.getSpellManager().isOnCooldown(player, this.getName());
    }

    @Override
    public boolean isSilenced(Entity entity) {
        return RunicCore.getSpellManager().getSilencedEntities().containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isStunned(Entity entity) {
        return RunicCore.getSpellManager().getStunnedEntities().containsKey(entity.getUniqueId());
    }

    @Override
    public boolean isRooted(Entity entity) {
        return RunicCore.getSpellManager().getRootedEntites().containsKey(entity.getUniqueId());
    }

    /**
     * Used for execute skills that rely on percent missing health.
     *
     * @param entity  mob/player to check hp for
     * @param percent multiplier for missing health (.25 * missing health, etc.)
     * @return the percent times missing health
     */
    @Override
    public int percentMissingHealth(Entity entity, double percent) {
        if (!(entity instanceof LivingEntity)) return 0;
        LivingEntity livingEntity = (LivingEntity) entity;
        double max = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double missing = max - livingEntity.getHealth();
        return (int) (missing * percent);
    }
}