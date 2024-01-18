package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * New bard spell 1
 *
 * @author BoBoBalloon
 */
public class Battlecry extends Spell implements AttributeSpell, DurationSpell, MagicDamageSpell, Tempo.Influenced {
    private static final Stat STAT = Stat.INTELLIGENCE;
    private static final double DEGREES = Math.PI / 3;
    private final Map<UUID, Pair<Integer, Long>> buffed;
    private double duration;
    private double multiplier;
    private double baseValue;
    private int attackRadius;
    private double buffRadius;
    private double damage;
    private double damagePerLevel;

    public Battlecry() {
        super("Battlecry", CharacterClass.CLERIC);
        this.setDescription("You shout in a large " + this.attackRadius + " block area in front of you, dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage and knocking back targets.\n" +
                "Allies within " + this.buffRadius + " blocks of you gain (" + this.baseValue + " +&f " + this.multiplier + "x&e " + STAT.getPrefix() + "&7)% increased physical⚔ and magicʔ damage for the next " + this.duration + "s.");
        this.buffed = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        this.removeExtraDuration(player);

//        for (Entity entity : EntityUtil.getEnemiesInCone(player, this.attackRadius, DEGREES, entity -> !player.equals(entity) && this.isValidEnemy(player, entity))) {
//            LivingEntity target = (LivingEntity) entity;
//            player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, target.getLocation(), 5, Math.random() * 2, Math.random(), Math.random() * 2);
//            DamageUtil.damageEntitySpell(this.damage, target, player, this);
//        }

        int stat = RunicCore.getStatAPI().getStat(player.getUniqueId(), STAT.getIdentifier());
        long now = System.currentTimeMillis();

        for (Entity entity : player.getNearbyEntities(this.buffRadius, this.buffRadius, this.buffRadius)) {
            if (!(entity instanceof LivingEntity target) || !this.isValidAlly(player, target)) {
                continue;
            }

            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation(), 5, Math.random() * 2, Math.random(), Math.random() * 2);
            this.buffed.put(target.getUniqueId(), Pair.pair(stat, now));
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.AMBIENT, 0.25F, 1F);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.damage(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.damage(event, event.getPlayer());
    }

    private void damage(@NotNull RunicDamageEvent event, @NotNull Player caster) {
        Pair<Integer, Long> data = this.buffed.get(caster.getUniqueId());

        if (data == null || System.currentTimeMillis() > data.second + (this.getDuration(caster) * 1000)) {
            this.removeExtraDuration(caster);
            return;
        }

        double percent = (this.baseValue + (data.first * this.multiplier)) / 100;
        int amount = (int) (event.getAmount() * percent);

        event.setAmount(event.getAmount() + amount);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.buffed.remove(event.getPlayer().getUniqueId());
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number attackRadius = (Number) spellData.getOrDefault("attack-radius", 4);
        this.attackRadius = attackRadius.intValue();
        Number buffRadius = (Number) spellData.getOrDefault("buff-radius", 5);
        this.buffRadius = buffRadius.doubleValue();
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
        return STAT.getIdentifier();
    }

    @Override
    @Deprecated
    public void setStatName(String statName) {

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
}

