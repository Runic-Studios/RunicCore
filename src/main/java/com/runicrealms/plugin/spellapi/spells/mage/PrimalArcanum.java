package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.ArcanumEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PrimalArcanum extends Spell implements DurationSpell, RadiusSpell, ShieldingSpell {
    private double duration;
    private double manaPerSlash;
    private double maxAllies;
    private double radius;
    private double shield;
    private double shieldPerLevel;
    private double shieldPerSlash;
    private double shieldPerLevelPerLevel;

    public PrimalArcanum() {
        super("Primal Arcanum", CharacterClass.MAGE);
        this.setDescription("You instantly &eshield &7yourself and " +
                "up to " + maxAllies + " allies within " + radius + " blocks for " +
                "(" + shield + " + &f" + shieldPerLevel +
                "x&7 lvl) health! For the next " + duration + "s, " +
                "your &aSpectral Blade &7is empowered with arcane energy! " +
                "Each successful slash grants you a (" + shieldPerSlash + " + &f" + shieldPerLevelPerLevel +
                "x&7 lvl) health &eshield &7and restores " + manaPerSlash + " mana!");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number manaPerSlash = (Number) spellData.getOrDefault("mana-per-slash", 8);
        setManaPerSlash(manaPerSlash.doubleValue());
        Number maxAllies = (Number) spellData.getOrDefault("max-allies", 3);
        setMaxAllies(maxAllies.doubleValue());
        Number shieldPerSlash = (Number) spellData.getOrDefault("shield-per-slash", 5);
        setShieldPerSlash(shieldPerSlash.doubleValue());
        Number shieldPerSlashPerLevel = (Number) spellData.getOrDefault("shield-per-slash-per-level", 0.4);
        setShieldPerLevelPerLevel(shieldPerSlashPerLevel.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 1.0f);
        new HelixParticleFrame(3.0f, 30, 12.0F).playParticle(player, Particle.SPELL_WITCH, player.getLocation());
        shieldPlayer(player, player, shield, this);

        // Shield nearby allies
        int count = 1;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            if (count > maxAllies) return;
            if (entity.equals(player)) continue;
            Player ally = (Player) entity;
            shieldPlayer(player, ally, shield, this);
            count++;
        }

        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), player.getUniqueId(), SpellEffectType.ARCANUM);
        if (spellEffectOpt.isPresent()) {
            ArcanumEffect arcanumEffect = (ArcanumEffect) spellEffectOpt.get();
            arcanumEffect.refresh();
        } else {
            ArcanumEffect arcanumEffect = new ArcanumEffect(player, this.duration);
            arcanumEffect.initialize();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onArcanumSlash(BasicAttackEvent event) {
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer().getUniqueId())) return;
        UUID uuid = event.getPlayer().getUniqueId();
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.ARCANUM);
        if (spellEffectOpt.isEmpty()) return;
        RunicCore.getRegenManager().addMana(event.getPlayer(), (int) this.manaPerSlash);
        RunicCore.getSpellAPI().shieldPlayer(event.getPlayer(), event.getPlayer(), this.shieldPerSlash, this);
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    public void setManaPerSlash(double manaPerSlash) {
        this.manaPerSlash = manaPerSlash;
    }

    public void setMaxAllies(double maxAllies) {
        this.maxAllies = maxAllies;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setShieldPerSlash(double shieldPerSlash) {
        this.shieldPerSlash = shieldPerSlash;
    }

    public void setShieldPerLevelPerLevel(double shieldPerLevelPerLevel) {
        this.shieldPerLevelPerLevel = shieldPerLevelPerLevel;
    }
}

