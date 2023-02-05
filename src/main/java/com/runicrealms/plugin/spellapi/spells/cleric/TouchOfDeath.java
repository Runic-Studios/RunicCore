package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TouchOfDeath extends Spell implements HealingSpell, MagicDamageSpell {
    private static final int DAMAGE = 8;
    private static final int DURATION = 2;
    private static final int HEALING = 3;
    private static final double DAMAGE_PER_LEVEL = 0.3D;
    private static final double HEALING_PER_LEVEL = 0.4D;
    private final Map<UUID, Map<UUID, Long>> markedEnemiesMap = new HashMap<>();

    public TouchOfDeath() {
        super("Touch of Death",
                "Enemies who have been hit by your spells in the last 2s " +
                        "take (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) extra magicʔ damage from " +
                        "your basic attacks! Each empowered attack heals✦ you " +
                        "for (" + HEALING + " + &f" + HEALING_PER_LEVEL + "x&7 lvl)",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
        startMarkRemovalTask();
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @Override
    public int getHeal() {
        return HEALING;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
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
            HealUtil.healPlayer(HEALING, event.getPlayer(), event.getPlayer(), false, this);
        } else {
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (markedEnemiesMap.isEmpty()) return;
            for (UUID uuid : markedEnemiesMap.keySet()) {
                for (UUID markedEnemyUuid : markedEnemiesMap.get(uuid).keySet()) {
                    if (System.currentTimeMillis() - markedEnemiesMap.get(uuid).get(markedEnemyUuid) > (DURATION * 1000)) {
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

