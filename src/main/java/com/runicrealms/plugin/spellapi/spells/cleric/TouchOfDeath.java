package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TouchOfDeath extends Spell implements DurationSpell, MagicDamageSpell {
    private final Map<UUID, Map<UUID, Long>> markedEnemiesMap = new HashMap<>();
    private double damage;
    private double duration;
    private double damagePerLevel;

    public TouchOfDeath() {
        super("Touch Of Death", CharacterClass.CLERIC);
        this.setIsPassive(true);
        startMarkRemovalTask();
        this.setDescription("Enemies who have been hit by your spells in the last " + duration + "s " +
                "take (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) extra magicʔ damage from " +
                "your basic attacks!");
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
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpell() == null) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() instanceof TouchOfDeath) return; // Can't trigger itself
        if (event.getSpell() instanceof Encore) return; // Can't be triggered by Encore
        if (!markedEnemiesMap.containsKey(event.getPlayer().getUniqueId())) {
            markedEnemiesMap.put(event.getPlayer().getUniqueId(), new HashMap<>());
        }
        markedEnemiesMap.get(event.getPlayer().getUniqueId()).put(event.getVictim().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.isBasicAttack()) {
            if (markedEnemiesMap.get(event.getPlayer().getUniqueId()) == null) return;
            if (markedEnemiesMap.get(event.getPlayer().getUniqueId()).get(event.getVictim().getUniqueId()) == null)
                return;
            DamageUtil.damageEntitySpell(damage, event.getVictim(), event.getPlayer(), this);
        } else {
            // Refresh the marked map
            if (!markedEnemiesMap.containsKey(event.getPlayer().getUniqueId())) {
                markedEnemiesMap.put(event.getPlayer().getUniqueId(), new HashMap<>());
            }
            markedEnemiesMap.get(event.getPlayer().getUniqueId()).put(event.getVictim().getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Running task to remove marks from enemies
     */
    private void startMarkRemovalTask() {
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            if (markedEnemiesMap.isEmpty()) return;
            for (UUID uuid : markedEnemiesMap.keySet()) {
                for (UUID markedEnemyUuid : markedEnemiesMap.get(uuid).keySet()) {
                    if (System.currentTimeMillis() - markedEnemiesMap.get(uuid).get(markedEnemyUuid) > (duration * 1000)) {
                        markedEnemiesMap.get(uuid).remove(markedEnemyUuid);
                        if (markedEnemiesMap.get(uuid).isEmpty()) {
                            markedEnemiesMap.remove(uuid);
                        }
                    }
                }
            }
        }, 0, 5L);
    }


}

