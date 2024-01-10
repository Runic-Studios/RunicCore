package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.AllyVerifyEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Spell implements ISpell, Listener {
    private static final String SPELLS_DIRECTORY = "RunicCore/spells";
    private final String name;
    private final CharacterClass reqClass;
    protected RunicCore plugin = RunicCore.getInstance();
    private double cooldown;
    private int manaCost;
    private String description = "";
    private boolean isPassive = false;
    private boolean displayCastMessage = true;

    /**
     * Creates this spell object once on startup. Loads its values from flat file
     *
     * @param name     of the spell
     * @param reqClass to use the spell
     */
    public Spell(@NotNull String name, @NotNull CharacterClass reqClass) {
        this.name = name;
        this.reqClass = reqClass;
        this.loadSpellData(); // Load values like mana, cooldown, etc. from file
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean hasSpellEffect(UUID uuid, SpellEffectType identifier) {
        return RunicCore.getSpellEffectAPI().hasSpellEffect(uuid, identifier);
    }

    @Override
    public Optional<SpellEffect> getSpellEffect(UUID casterUuid, UUID recipientUuid, SpellEffectType identifier) {
        return RunicCore.getSpellEffectAPI().getSpellEffect(casterUuid, recipientUuid, identifier);
    }

    @Override
    public void addStatusEffect(@NotNull LivingEntity livingEntity, @NotNull RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage, @Nullable LivingEntity applier) {
        RunicCore.getStatusEffectAPI().addStatusEffect(livingEntity, runicStatusEffect, durationInSecs, displayMessage, applier);
    }

    @Override
    public void addStatusEffect(@NotNull LivingEntity livingEntity, @NotNull RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage) {
        RunicCore.getStatusEffectAPI().addStatusEffect(livingEntity, runicStatusEffect, durationInSecs, displayMessage);
    }

    @Override
    public boolean execute(@NotNull Player player, @NotNull SpellItemType type) {

        if (isOnCooldown(player)) return false; // ensure spell is not on cooldown
        UUID uuid = player.getUniqueId();

        // verify class
        boolean canCast = this.getReqClass() == CharacterClass.ANY
                || this.getReqClass().toString().equalsIgnoreCase(RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(uuid));

        if (!canCast) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            ActionBarUtil.sendTimedMessage(player, "&cYour class cannot cast this spell!", 3);
            return false;
        }

        if (!this.attemptToExecute(player))
            return false; // check additional conditions (being on ground)

        // cast the spell
        int currentMana = RunicCore.getRegenManager().getCurrentManaList().get(player.getUniqueId());
        RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), currentMana - this.manaCost);

        if (this.displayCastMessage) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "You cast " + ChatColor.WHITE + getName() + ChatColor.GREEN + "!"));
        }

        RunicCore.getSpellAPI().addCooldown(player, this, this.getCooldown());
        this.executeSpell(player, type);
        return true;
    }

    @Override
    public double getCooldown() {
        return this.cooldown;
    }

    @NotNull
    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getManaCost() {
        return this.manaCost;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @NotNull
    @Override
    public CharacterClass getReqClass() {
        return reqClass;
    }

    @Override
    public boolean hasPassive(@NotNull UUID uuid, @NotNull String passive) {
        return RunicCore.getSkillTreeAPI().hasPassiveFromSkillTree(uuid, passive);
    }

    @Override
    public boolean hasStatusEffect(@NotNull UUID uuid, @NotNull RunicStatusEffect runicStatusEffect) {
        return RunicCore.getStatusEffectAPI().hasStatusEffect(uuid, runicStatusEffect);
    }

    @Override
    public void healPlayer(@NotNull Player caster, @NotNull Player recipient, double amount, @Nullable Spell spell) {
        RunicCore.getSpellAPI().healPlayer(caster, recipient, amount, spell);
    }

    @Override
    public boolean isOnCooldown(@NotNull Player player) {
        return RunicCore.getSpellAPI().isOnCooldown(player, this.getName());
    }

    @Override
    public boolean isValidAlly(@NotNull Player caster, @NotNull Entity ally) {
        AllyVerifyEvent allyVerifyEvent = new AllyVerifyEvent(caster, ally);
        Bukkit.getServer().getPluginManager().callEvent(allyVerifyEvent);
        return !allyVerifyEvent.isCancelled();
    }

    @Override
    public boolean isValidEnemy(@NotNull Player caster, @NotNull Entity victim) {
        EnemyVerifyEvent enemyVerifyEvent = new EnemyVerifyEvent(caster, victim);
        Bukkit.getServer().getPluginManager().callEvent(enemyVerifyEvent);
        return !enemyVerifyEvent.isCancelled();
    }

    @Override
    public int percentMissingHealth(@NotNull Entity entity, double percent) {
        if (!(entity instanceof LivingEntity livingEntity)) return 0;
        double max = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double missing = max - livingEntity.getHealth();
        return (int) (missing * percent);
    }

    @Override
    public boolean removeStatusEffect(@NotNull Entity entity, @NotNull RunicStatusEffect runicStatusEffect) {
        return RunicCore.getStatusEffectAPI().removeStatusEffect(entity.getUniqueId(), runicStatusEffect);
    }

    @Override
    public void shieldPlayer(@NotNull Player caster, @NotNull Player recipient, double amount, @Nullable Spell spell) {
        RunicCore.getSpellAPI().shieldPlayer(caster, recipient, amount, spell);
    }

    public boolean attemptToExecute(Player player) {
        return true;
    }

    public void executeSpell(Player player, SpellItemType type) {

    }

    public boolean isPassive() {
        return isPassive;
    }

    /**
     * Loads the spell balance values from flat file
     */
    private void loadSpellData() {
        File filePath = new File(RunicCore.getInstance().getDataFolder().getParent(), SPELLS_DIRECTORY + "/" + this.getClass().getSimpleName() + ".yml");

        if (filePath.exists()) {
            Yaml yaml = new Yaml();
            try {
                FileInputStream fileInputStream = new FileInputStream(filePath);
                Map<String, Object> spellData = yaml.load(fileInputStream);
                this.cooldown = (int) spellData.getOrDefault("cooldown", 0);
                this.manaCost = (int) spellData.getOrDefault("mana", 0);
                loadSpellSpecificData(spellData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            RunicCore.getInstance().getLogger().log(Level.SEVERE, "Missing spell data file for " + this.name);
        }
    }

    /**
     * Loads the values associated with this unique spell-type.
     * E.g. AttributeSpell, MagicDamageSpell, etc.
     *
     * @param spellData the map from the yml file
     */
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        if (this instanceof AttributeSpell attributeSpell) {
            attributeSpell.loadAttributeData(spellData);
        }
        if (this instanceof DistanceSpell distanceSpell) {
            distanceSpell.loadDistanceData(spellData);
        }
        if (this instanceof DurationSpell durationSpell) {
            durationSpell.loadDurationData(spellData);
        }
        if (this instanceof HealingSpell healingSpell) {
            healingSpell.loadHealingData(spellData);
        }
        if (this instanceof MagicDamageSpell magicDamageSpell) {
            magicDamageSpell.loadMagicData(spellData);
        }
        if (this instanceof PhysicalDamageSpell physicalDamageSpell) {
            physicalDamageSpell.loadPhysicalData(spellData);
        }
        if (this instanceof RadiusSpell radiusSpell) {
            radiusSpell.loadRadiusData(spellData);
        }
        if (this instanceof ShieldingSpell shieldingSpell) {
            shieldingSpell.loadShieldingData(spellData);
        }
        if (this instanceof WarmupSpell warmupSpell) {
            warmupSpell.loadWarmupData(spellData);
        }
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

    public boolean isDisplayingCastMessage() {
        return this.displayCastMessage;
    }

    public void setDisplayCastMessage(boolean displayCastMessage) {
        this.displayCastMessage = displayCastMessage;
    }
}