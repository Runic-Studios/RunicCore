package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.cleric.SongOfWarEffect;
import com.runicrealms.plugin.spellapi.modeled.ModeledStandAnimated;
import com.runicrealms.plugin.spellapi.modeled.StandSlot;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * New bard spell 1
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Battlecry extends Spell implements AttributeSpell, DurationSpell, MagicDamageSpell, RadiusSpell, Tempo.Influenced {
    private static final int MODEL_DATA_BEAM = 2494;
    private static final int[] MODEL_DATA_BEAM_ARRAY = new int[]{
            MODEL_DATA_BEAM,
            2495,
            2496,
            2497,
            2498,
            2499,
//            MODEL_DATA_BEAM,
//            2495,
//            2496,
//            2497,
//            2498,
//            2499,
    };
    private double duration;
    private double baseValue;
    private double damage;
    private double damagePerLevel;
    private double knockback;
    private double multiplier;
    private double radius;
    private String statName;

    /**
     * Instantiates a new Battlecry.
     */
    public Battlecry() {
        super("Battlecry", CharacterClass.CLERIC);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("You shout in a " + this.radius + " block area around you, " +
                "dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage " +
                "and knocking enemies back! You and allies within the same radius " +
                "gain &csong of war &7for " + this.duration + "s!" +
                "\n\n&2&lEFFECT &cSong of War" +
                "\n&7Allies affected by &csong of war &7deal (" + this.baseValue + " +&f " +
                this.multiplier + "x&e " + prefix + "&7)% " +
                "increased physical⚔ and magicʔ damage!");
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number knockback = (Number) spellData.getOrDefault("knockback", 2.0);
        setKnockback(knockback.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        playEffect(player, player.getLocation());
        applySongOfWar(player, player); // Apply song of war to caster, since we're not using .getWorld() for entity check

        for (Entity entity : player.getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (TargetUtil.isValidAlly(player, entity)) {
                applySongOfWar(player, (Player) entity);
            } else if (TargetUtil.isValidEnemy(player, entity)) {
                DamageUtil.damageEntitySpell(this.damage, (LivingEntity) entity, player, this);
                KnockbackUtil.knockBackCustom(player, (LivingEntity) entity, this.knockback);
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.AMBIENT, 0.5f, 1.0f);
    }

    private void playEffect(Player player, Location location) {
        final Vector vector = player.getLocation().getDirection().normalize().multiply(2.0);
        new ModeledStandAnimated(
                player,
                location.add(vector),
                new Vector(0, 0, 0),
                MODEL_DATA_BEAM,
                1.0,
                1.0,
                StandSlot.ARM,
                null,
                MODEL_DATA_BEAM_ARRAY
        );
    }

    private void applySongOfWar(Player player, Player recipient) {
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), recipient.getUniqueId(), SpellEffectType.SONG_OF_WAR);
        if (spellEffectOpt.isPresent()) {
            SongOfWarEffect songOfWarEffect = (SongOfWarEffect) spellEffectOpt.get();
            songOfWarEffect.refresh();
        } else {
            SongOfWarEffect songOfWarEffect = new SongOfWarEffect(recipient, this.duration);
            songOfWarEffect.initialize();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.SONG_OF_WAR);
        if (spellEffectOpt.isEmpty()) return;
        double bonusDamage = event.getAmount() * this.percentAttribute(event.getPlayer());
        event.setAmount((int) (event.getAmount() + bonusDamage));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.SONG_OF_WAR);
        if (spellEffectOpt.isEmpty()) return;
        double bonusDamage = event.getAmount() * this.percentAttribute(event.getPlayer());
        event.setAmount((int) (event.getAmount() + bonusDamage));
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return this.statName;
    }

    @Override
    @Deprecated
    public void setStatName(String statName) {
        this.statName = statName;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    /**
     * Sets knockback.
     *
     * @param knockback the knockback
     */
    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }
}

