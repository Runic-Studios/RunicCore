package com.runicrealms.plugin.spellapi.spells.mage;

import com.google.common.util.concurrent.AtomicDouble;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.StaffAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * New pyromancer passive 1
 *
 * @author BoBoBalloon
 */
public class CinderedTouch extends Spell implements MagicDamageSpell, DistanceSpell {
    private static final double PERIOD = 0.5;
    private static final int BEAM_RADIUS = 1;
    private final Map<UUID, Integer> counts;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private int count;

    public CinderedTouch() {
        super("Cindered Touch", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("For every " + this.count + " enemies that you hit with &6Pyromancer&7 spells, your next basic attack is cindered! \n" +
                "Cindered attacks launch a wave of fire dealing (" + this.damage + " +&f " + this.damagePerLevel + " x&7 lvl) magicʔ damage " + this.distance + " blocks in front of you!");
        this.counts = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onStaffAttack(StaffAttackEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) {
            return;
        }

        Integer count = this.counts.get(event.getPlayer().getUniqueId());

        if (count == null || count < this.count) {
            return;
        }

        this.counts.remove(event.getPlayer().getUniqueId());
        event.setCancelled(true);

        Location castLocation = event.getPlayer().getEyeLocation().clone();

        AtomicDouble progress = new AtomicDouble(1);
        HorizontalCircleFrame particle = new HorizontalCircleFrame(BEAM_RADIUS, true);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (progress.get() > this.distance) {
                task.cancel();
                return;
            }

            progress.set(progress.get() + PERIOD);
            castLocation.add(castLocation.getDirection());
            particle.playParticle(event.getPlayer(), Particle.FLAME, castLocation);

            for (Entity entity : castLocation.getWorld().getNearbyEntities(castLocation, BEAM_RADIUS, BEAM_RADIUS, BEAM_RADIUS, entity -> this.isValidEnemy(event.getPlayer(), entity))) {
                if (entity instanceof LivingEntity target) {
                    DamageUtil.damageEntitySpell(this.damage, target, event.getPlayer(), false, this);
                }
            }
        }, 0, (long) PERIOD * 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSpellCast(SpellCastEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        if (!(event.getSpell() instanceof DragonsBreath || event.getSpell() instanceof Erupt || event.getSpell() instanceof Meteor)) {
            return;
        }

        Integer cached = this.counts.get(event.getCaster().getUniqueId());

        this.counts.put(event.getCaster().getUniqueId(), cached != null ? cached + 1 : 1);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.counts.remove(event.getPlayer().getUniqueId());
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

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number count = (Number) spellData.getOrDefault("count", 10);
        this.count = count.intValue();
    }
}

