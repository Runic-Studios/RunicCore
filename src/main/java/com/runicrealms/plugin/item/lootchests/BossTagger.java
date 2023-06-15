package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BossTagger implements Listener {
    private static final double DAMAGE_PERCENT = .05; // threshold to receive loot (4%)
    private static final List<String> BOSS_INTERNAL_NAMES = new ArrayList<>();
    private final HashMap<UUID, HashMap<Player, Integer>> bossFighters; // a single boss is mapped to many players (damage threshold tracked here)
    private final HashMap<UUID, HashSet<UUID>> bossLooters;
    private final HashMap<UUID, BossChest> activeBossLootChests;

    public BossTagger() {
        bossFighters = new HashMap<>();
        bossLooters = new HashMap<>();
        activeBossLootChests = new HashMap<>();
        BOSS_INTERNAL_NAMES.addAll(Arrays.asList
                (
                        "sebath",
                        "Jorundr",
                        "GlitchedEntity",
                        "TheLibrarian",
                        "sandGuy",
                        "Eldrid"
                ));
    }

    /**
     * @param internalName the unique internal name of the boss
     * @return true if it is the name of a dungeon boss
     */
    public static boolean isBoss(String internalName) {
        return BOSS_INTERNAL_NAMES.contains(internalName);
    }

    /**
     * @param entity that will be tracked
     * @return true if it is the name of a dungeon boss
     */
    public static boolean isBoss(Entity entity) {
        if (!MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).isPresent()) return false;
        ActiveMob am = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).get();
        return BOSS_INTERNAL_NAMES.contains(am.getMobType());
    }

    /**
     * Maps a boss unique id to a block which represents its chest
     *
     * @return a key, value map of boss uuid to chest
     */
    public HashMap<UUID, BossChest> getActiveBossLootChests() {
        return activeBossLootChests;
    }

    /**
     * Get a set of players who should receive priority boss loot (and tokens)
     *
     * @param bossId uuid of the boss
     * @return a set of uuid's of players
     */
    public HashSet<UUID> getBossLooters(UUID bossId) {
        return bossLooters.get(bossId);
    }

    /**
     * Generate loot for each slayer when the boss is defeated
     */
    @EventHandler
    public void onBossDeath(MythicMobDeathEvent event) {
        if (!isBoss(event.getMobType().getInternalName())) return;
        if (!bossFighters.containsKey(event.getMob().getUniqueId())) return;
        bossFighters.get(event.getEntity().getUniqueId()).forEach((player, integer) -> {
            player.sendMessage(ChatColor.YELLOW + "You dealt " + ChatColor.RED + ChatColor.BOLD + integer + ChatColor.YELLOW + " damage to the boss!");
            if (bossLooters.get(event.getEntity().getUniqueId()).contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.GREEN + "You qualified for boss loot!");
            } else {
                player.sendMessage(ChatColor.RED + "You did not qualify for boss loot!");
            }
        });
        bossFighters.get(event.getEntity().getUniqueId()).clear(); // Clear damage tracking map
    }

    /**
     * Prepare boss on spawn
     */
    @EventHandler
    public void onBossSpawn(MythicMobSpawnEvent event) {
        MythicMob boss = event.getMobType();
        if (!boss.hasFaction()) return;
        if (!boss.getFaction().equalsIgnoreCase("boss")) return;
        HashMap<Player, Integer> fighters = new HashMap<>();
        HashSet<UUID> looters = new HashSet<>();
        bossFighters.put(event.getEntity().getUniqueId(), fighters);
        bossLooters.put(event.getEntity().getUniqueId(), looters);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        trackBossDamage(event.getPlayer(), event.getVictim(), event.getAmount());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onSpellDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        trackBossDamage(event.getPlayer(), event.getVictim(), event.getAmount());
    }

    /**
     * Keeps track of damage during boss fight to determine who gets loot priority.
     *
     * @param player      who damaged boss
     * @param entity      the boss
     * @param eventAmount the damage from the event
     */
    private void trackBossDamage(Player player, Entity entity, int eventAmount) {
        if (!isBoss(entity)) return;
        if (bossLooters.get(entity.getUniqueId()) == null) return;
        UUID playerId = player.getUniqueId();
        UUID bossId = entity.getUniqueId();
        LivingEntity livingEntity = (LivingEntity) entity;
        double threshold = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * DAMAGE_PERCENT;
        if (!bossFighters.get(bossId).containsKey(player))
            bossFighters.get(bossId).put(player, 0);
        int currentDamageToBossFromPlayer = bossFighters.get(bossId).get(player);
        bossFighters.get(bossId).put(player, currentDamageToBossFromPlayer + eventAmount);
        currentDamageToBossFromPlayer = bossFighters.get(entity.getUniqueId()).get(player);
        if (currentDamageToBossFromPlayer >= threshold) {
            bossLooters.get(entity.getUniqueId()).add(playerId);
        }
    }
}
