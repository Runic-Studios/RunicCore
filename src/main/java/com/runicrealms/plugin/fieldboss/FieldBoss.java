package com.runicrealms.plugin.fieldboss;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.loot.chest.BossTimedLoot;
import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FieldBoss implements Listener {

    /*
    TODO:
    - Add tribute chest, organic way to activate
    - Only allow boss to aggro on players fighting it
     */

    private static final double DOME_PARTICLE_DENSITY = 0.1; // particles per square block
    private static final double CIRCLE_PARTICLE_COUNT = 32;
    private static final int ADDITIONAL_ATTENTION_RADIUS = 30; // block number added to radius in which players will be notified of field boss stuff happening
    private static final int DEATH_TIMEOUT_MILLIS = 1000 * 60 * 2;

    private final String identifier;
    private final String bossName;
    private final String mmID;
    private final Location domeCentre; // Centre of the dome, not where the shrine is
    private final double domeRadius;
    private final Location circleCentre;
    private final double circleRadius;
    private final @Nullable GuildScore guildScore;
    private final BossTimedLoot loot;
    private final Map<ActiveState.PlayerState, Set<PacketContainer>> domeParticles = new HashMap<>();
    private final Set<PacketContainer> circleParticles = new HashSet<>();
    private @Nullable ActiveState active; // null means inactive
    private boolean spoilsActive = false;

    public FieldBoss(String identifier,
                     String bossName,
                     String mmID,
                     Location domeCentre,
                     double domeRadius,
                     Location circleCentre,
                     double circleRadius,
                     @Nullable GuildScore guildScore,
                     BossTimedLoot loot) {
        this.identifier = identifier;
        this.bossName = bossName;
        this.mmID = mmID;
        this.domeCentre = domeCentre;
        this.domeRadius = domeRadius;
        this.circleCentre = circleCentre;
        this.circleRadius = circleRadius;
        this.guildScore = guildScore;
        this.loot = loot;
        try (MythicBukkit mythicBukkit = MythicBukkit.inst()) {
            if (mythicBukkit.getAPIHelper().getMythicMob(mmID) == null)
                throw new IllegalArgumentException("Field Boss " + identifier + " has invalid MM ID " + mmID);
        }
        if (!Objects.equals(domeCentre.getWorld(), circleCentre.getWorld()))
            throw new IllegalArgumentException("Dome and circle must be in the same world!");
        if (Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), this::loadParticles);
        else loadParticles();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    private static PacketContainer createDustParticlePacket(double x, double y, double z, Color color) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getIntegers().write(0, 14); // Particle ID, 14 is dust
        packet.getBooleans().write(0, true); // Long distance
        packet.getDoubles().write(0, x); // X
        packet.getDoubles().write(1, y); // Y
        packet.getDoubles().write(2, z); // Z
        packet.getFloat().write(0, 0f); // offset x
        packet.getFloat().write(1, 0f); // offset y
        packet.getFloat().write(2, 0f); // offset z
        packet.getFloat().write(3, 0f); // max speed
        packet.getIntegers().write(1, 1); // particle count
        packet.getFloat().write(4, ((float) color.getRed()) / 255f); // dust data red
        packet.getFloat().write(5, ((float) color.getGreen()) / 255f);
        packet.getFloat().write(6, ((float) color.getBlue()) / 255f);
        packet.getFloat().write(4, 1f); // dust data scale
        return packet;
    }

    private static PacketContainer createFireworkParticlePacket(double x, double y, double z) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getIntegers().write(0, 26); // Particle ID, 26 is firework
        packet.getBooleans().write(0, true); // Long distance
        packet.getDoubles().write(0, x); // X
        packet.getDoubles().write(1, y); // Y
        packet.getDoubles().write(2, z); // Z
        packet.getFloat().write(0, 0f); // offset x
        packet.getFloat().write(1, 0f); // offset y
        packet.getFloat().write(2, 0f); // offset z
        packet.getFloat().write(3, 0f); // max speed
        packet.getIntegers().write(1, 1); // particle count
        return packet;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean attemptActivate(CommandSender sender) {
        if (spoilsActive) {
            sender.sendMessage(ChatColor.RED + "Field boss cannot be activated while spoils are still active!");
            return false;
        }
        if (active != null) {
            sender.sendMessage(ChatColor.RED + "Field boss cannot be activated while it is still active!");
            return false;
        }
        activate();
        return true;
    }

    private void activate() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> active = new ActiveState());
        } else {
            active = new ActiveState();
        }
    }

    public boolean isActive() {
        return this.active != null;
    }

    public ActiveState getActiveState() {
        if (this.active == null)
            throw new IllegalStateException("Cannot get field boss active state when it is inactive!");
        return this.active;
    }

    private void loadParticles() {
        Map<Color, Set<PacketContainer>> possibleColors = new HashMap<>();
        Arrays.stream(ActiveState.PlayerState.values())
                .filter(state -> state.domeParticleColor != null)
                .map(state -> state.domeParticleColor)
                .distinct()
                .forEach(color -> possibleColors.put(color, new HashSet<>()));

        // Thank you chat gpt
        double surfaceArea = 4 * Math.PI * Math.pow(domeRadius, 2); // surface area of sphere
        int count = (int) (DOME_PARTICLE_DENSITY * surfaceArea); // number of particles
        double goldenAngle = Math.PI * (3 - Math.sqrt(5));  // Golden angle in radians
        for (int i = 0; i < count; i++) {
            double y = 1 - (i / (float) (count - 1)) * 2; // y goes from 1 to -1
            double radiusAtY = Math.sqrt(1 - y * y); // radius at y
            double phi = goldenAngle * i;
            double x = Math.cos(phi) * radiusAtY;
            double z = Math.sin(phi) * radiusAtY;
            Location particleLocation = domeCentre.clone().add(new Location(domeCentre.getWorld(), x * domeRadius, y * domeRadius, z * domeRadius));
            if (particleLocation.getBlock().getType() != Material.AIR) continue;
            for (Color color : possibleColors.keySet()) {
                possibleColors.get(color).add(createDustParticlePacket(particleLocation.getX(), particleLocation.getY(), particleLocation.getZ(), color));
            }
        }

        for (ActiveState.PlayerState state : ActiveState.PlayerState.values()) {
            domeParticles.put(state, possibleColors.get(state.domeParticleColor));
        }

        for (int i = 0; i < CIRCLE_PARTICLE_COUNT; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_PARTICLE_COUNT;
            double x = circleCentre.getX() + circleRadius * Math.cos(angle);
            double y = circleCentre.getY();
            double z = circleCentre.getZ() + circleRadius * Math.sin(angle);
            circleParticles.add(createFireworkParticlePacket(x, y, z));
        }
    }

    public static class GuildScore {

        private int amount;
        private Distribution distribution;

        private GuildScore() {
        }

        public static GuildScore split(int amount, double participationSplit, double damageSplit) {
            GuildScore guildScore = new GuildScore();
            guildScore.amount = amount;
            guildScore.distribution = guildScore.new SplitDistribution(participationSplit, damageSplit);
            return guildScore;
        }

        public Distribution getDistribution() {
            return this.distribution;
        }

        public interface Distribution {

            void distribute(Map<UUID, Integer> damageDealt, double maxHealth);

        }

        public class SplitDistribution implements Distribution {

            private final double participationSplit;
            private final double damageSplit;

            public SplitDistribution(double participationSplit, double damageSplit) {
                this.participationSplit = participationSplit;
                this.damageSplit = damageSplit;
            }

            @Override
            public void distribute(Map<UUID, Integer> damageDealt, double maxHealth) {
                Map<UUID, Integer> damageScores = new HashMap<>();
                if (damageDealt.size() == 1) {
                    damageScores.put(damageDealt.keySet().stream().findFirst().orElseThrow(), amount);
                } else {
                    double totalSplit = participationSplit + damageSplit;
                    double participationPercent = participationSplit / totalSplit;
                    double damagePercent = damageSplit / totalSplit;

                    int damageScoreTotal = (int) Math.round(amount * participationPercent);
                    double participationScoreTotal = amount * damagePercent;
                    double participationScoreForEach = participationScoreTotal / ((double) damageDealt.size());

                    for (UUID damager : damageDealt.keySet()) {
                        double percentDamage = (double) damageDealt.get(damager) / maxHealth;
                        damageScores.put(damager, (int) Math.round(damageScoreTotal * percentDamage + participationScoreForEach));
                    }

                    RunicCommon.getGuildsAPI().addBulkGuildScore(damageScores, true);
                }
            }

        }

    }

    /**
     * Represents an active field boss. This includes a field boss during its warmup state.
     */
    public class ActiveState implements Listener {

        private final Map<Player, PlayerState> playerStates = new ConcurrentHashMap<>();

        private final UUID entityID;
        private final Map<Player, Long> participants = new HashMap<>(); // Long represents last death timestamp, for cooldown
        private final BukkitTask particleTask;
        private final Map<UUID, Integer> damageDealt = new HashMap<>();
        private final BukkitTask playerStateTask;
        private @Nullable BukkitTask movementTask;
        private State state = State.WARMUP;

        private ActiveState() {
            Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
            updatePlayerStates();
            playerStateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    updatePlayerStates();
                }
            }.runTaskTimerAsynchronously(RunicCore.getInstance(), 20, 20);
            particleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerState playerState = playerStates.get(player);
                        if (playerState == null) continue;
                        if (playerState.canSpectate) {
                            domeParticles.get(playerState).forEach(packet -> ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet));
                        }
                    }
                }
            }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 4);
            try {
                try (MythicBukkit mythicBukkit = MythicBukkit.inst()) {
                    Entity mob = mythicBukkit.getAPIHelper().spawnMythicMob(mmID, domeCentre);
                    this.entityID = mob.getUniqueId();
                }
            } catch (InvalidMobTypeException exception) {
                throw new IllegalArgumentException("Invalid mythic mobs ID: " + mmID);
            }
            new WarmupCircle(() -> movementTask = new BukkitRunnable() {
                @Override
                public void run() {
                    teleportDomePlayers();
                }
            }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 8));
        }

        private boolean canEnterFight(Player player) {
            Long lastDeath = participants.get(player);
            if (lastDeath == null) return false; // not a participant
            return lastDeath + DEATH_TIMEOUT_MILLIS <= System.currentTimeMillis();
        }

        private void updatePlayerStates() {
            double radiusSquared = domeRadius * domeRadius;
            double additionalAttentionRadius = (domeRadius + ADDITIONAL_ATTENTION_RADIUS) * (domeRadius + ADDITIONAL_ATTENTION_RADIUS);
            double circleRadiusSquared = circleRadius * circleRadius;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(domeCentre.getWorld())) {
                    playerStates.put(player, PlayerState.NOT_PRESENT);
                    continue;
                }
                if (state == State.WARMUP) {
                    if (player.getLocation().distanceSquared(circleCentre) <= circleRadiusSquared) {
                        playerStates.put(player, PlayerState.WARMUP_IN_CIRCLE);
                    } else {
                        double distanceSquared = player.getLocation().distanceSquared(domeCentre);
                        if (distanceSquared <= additionalAttentionRadius) {
                            playerStates.put(player, PlayerState.WARMUP_SPECTATING);
                        } else {
                            playerStates.put(player, PlayerState.NOT_PRESENT);
                        }
                    }
                } else if (state == State.ACTIVE) {
                    boolean canEnterFight = canEnterFight(player);
                    double distanceSquared = player.getLocation().distanceSquared(domeCentre);
                    if (canEnterFight) {
                        if (distanceSquared <= radiusSquared) {
                            playerStates.put(player, PlayerState.ACTIVE_IN_DOME);
                        } else if (distanceSquared <= additionalAttentionRadius) {
                            playerStates.put(player, PlayerState.ACTIVE_CAN_ENTER_DOME);
                        } else {
                            playerStates.put(player, PlayerState.NOT_PRESENT);
                        }
                    } else {
                        if (distanceSquared <= additionalAttentionRadius) {
                            playerStates.put(player, PlayerState.ACTIVE_SPECTATING_DOME);
                        } else {
                            playerStates.put(player, PlayerState.NOT_PRESENT);
                        }
                    }
                }
            }
        }

        private void teleportDomePlayers() {
            double radiusSquared = domeRadius * domeRadius;
            double radiusCheatingInsideSquared = (domeRadius + 4) * (domeRadius + 4);
            double radiusCheatingOutsideSquared = (domeRadius - 4) * (domeRadius - 4);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() != domeCentre.getWorld()) continue;
                double distanceSquared = player.getLocation().distanceSquared(domeCentre);
                PlayerState state = playerStates.get(player);
                if (state == null) continue;
                if (state.lockedInsideDome) { // Inside the dome
                    if (distanceSquared > radiusCheatingInsideSquared) { // Player is cheating? they are far away from the dome, teleport them back in
                        Vector direction = player.getLocation().toVector().subtract(domeCentre.toVector()).normalize();
                        Location newLocation = domeCentre.clone().add(direction.multiply(domeRadius - 1));
                        if (newLocation.getBlock().getType() != Material.AIR) newLocation = domeCentre;
                        newLocation.setYaw(player.getLocation().getYaw());
                        newLocation.setPitch(player.getLocation().getPitch());
                        Location finalNewLocation = newLocation;
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.teleport(finalNewLocation));
                    } else if (distanceSquared > radiusSquared) { // Just nudge them back in
                        Vector direction = domeCentre.toVector().subtract(player.getLocation().toVector()).normalize();
                        Vector newVelocity = player.getVelocity().multiply(0.5).add(direction.multiply(0.5));
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.setVelocity(newVelocity));
                    }
                } else if (state.lockedOutsideDome) { // Outside the dome
                    if (distanceSquared < radiusCheatingOutsideSquared) { // Player is cheating? they are far into the dome, teleport them back out
                        Vector direction = player.getLocation().toVector().subtract(domeCentre.toVector()).normalize();
                        Location newLocation = domeCentre.clone().add(direction.multiply(domeRadius + 1));
                        if (newLocation.getBlock().getType() != Material.AIR) newLocation = domeCentre;
                        newLocation.setYaw(player.getLocation().getYaw());
                        newLocation.setPitch(player.getLocation().getPitch());
                        Location finalNewLocation = newLocation;
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.teleport(finalNewLocation));
                    } else if (distanceSquared < radiusSquared) { // Just nudge them back out
                        Vector direction = domeCentre.toVector().subtract(player.getLocation().toVector()).normalize().multiply(-1);
                        Vector newVelocity = player.getVelocity().multiply(0.5).add(direction.multiply(0.5));
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.setVelocity(newVelocity));
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onMagicDamage(MagicDamageEvent event) {
            if (!event.getVictim().getUniqueId().equals(entityID)) return;
            PlayerState state = playerStates.get(event.getPlayer());
            if (state == null) return;
            if (!state.isFighting) event.setCancelled(true);
            else trackBossDamage(event.getPlayer(), event.getAmount());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPhysicalDamage(PhysicalDamageEvent event) {
            if (!event.getVictim().getUniqueId().equals(entityID)) return;
            PlayerState state = playerStates.get(event.getPlayer());
            if (state == null) return;
            if (!state.isFighting) event.setCancelled(true);
            else trackBossDamage(event.getPlayer(), event.getAmount());
        }

        private void trackBossDamage(Player player, int amount) {
            if (!damageDealt.containsKey(player.getUniqueId()))
                damageDealt.put(player.getUniqueId(), 0);
            damageDealt.put(player.getUniqueId(), damageDealt.get(player.getUniqueId()) + amount);
        }

        @EventHandler
        public void onRunicDeath(RunicDeathEvent event) {
            if (participants.containsKey(event.getVictim()))
                participants.put(event.getVictim(), System.currentTimeMillis());
        }

        @EventHandler
        public void onMythicMobDeath(MythicMobDeathEvent event) {
            if (event.getEntity().getUniqueId().equals(entityID)) {
                if (guildScore != null)
                    guildScore.getDistribution().distribute(damageDealt, event.getMob().getEntity().getMaxHealth());
                deactivate(true);
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            participants.remove(event.getPlayer());
            playerStates.remove(event.getPlayer());
        }

        public void deactivate(boolean success) {
            if (state != State.ACTIVE)
                throw new IllegalStateException("Cannot deactivate active state when still in warmup!");
            HandlerList.unregisterAll(this);
            particleTask.cancel();
            playerStateTask.cancel();
            if (movementTask != null) movementTask.cancel();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerState playerState = playerStates.get(player);
                if (playerState == null) continue;
                if (playerState.canSpectate)
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 2.0f);
            }
            active = null;
            if (success) {
                for (UUID player : damageDealt.keySet()) {
                    Player online = Bukkit.getPlayer(player);
                    if (online == null) continue;
                    online.sendMessage(ChatColor.GREEN + "You defeated the " + ChatColor.YELLOW + bossName + ChatColor.GREEN + "! Collect the spoils from the chest near you.");
                    RunicCore.getLootAPI().displayTimedLootChest(online, loot.getLootChest());
                }
                Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                    spoilsActive = false;
                }, loot.getLootChest().getDuration() * 20L);
                spoilsActive = true;
            }
            damageDealt.clear();
        }

        private enum PlayerState {
            // These occur during State.ACTIVE:
            ACTIVE_IN_DOME(true, true, true, true, false, false, Color.RED),
            ACTIVE_SPECTATING_DOME(true, false, false, false, true, false, Color.RED),
            ACTIVE_CAN_ENTER_DOME(true, true, false, false, false, false, Color.GREEN),
            ACTIVE_WAIT_ENTER_DOME(true, true, false, false, true, false, Color.RED),

            // These occur during State.WARMUP:
            WARMUP_IN_CIRCLE(true, true, true, false, false, true, Color.OLIVE),
            WARMUP_SPECTATING(true, false, false, false, false, false, Color.YELLOW),

            // Applies to both states:
            NOT_PRESENT(false, false, false, false, false, false, null);

            private final boolean canSpectate; // Will the player receive messages/sounds relating to field boss?
            private final boolean isParticipant; // Will the player receive loot once it is done (are they one of the people fighting the boss)
            private final boolean isFighting; // Are they actively fighting the boss (can they damage it/can it target them)
            private final boolean lockedInsideDome; // Are they locked inside the dome
            private final boolean lockedOutsideDome; // Are they locked outside the dome
            private final boolean isInCircle; // Are they in the summoning circle?
            private final Color domeParticleColor; // What color should the dome particles be for them

            PlayerState(boolean canSpectate, boolean isParticipant, boolean isFighting, boolean lockedInsideDome, boolean lockedOutsideDome, boolean isInCircle, Color domeParticleColor) {
                this.canSpectate = canSpectate;
                this.isParticipant = isParticipant;
                this.isFighting = isFighting;
                this.lockedInsideDome = lockedInsideDome;
                this.lockedOutsideDome = lockedOutsideDome;
                this.isInCircle = isInCircle;
                this.domeParticleColor = domeParticleColor;
            }
        }

        public enum State {
            ACTIVE, WARMUP
        }

        public class WarmupCircle {

            private final static int CIRCLE_DURATION = 15; // seconds

            private final Runnable onFinish;

            private WarmupCircle(Runnable onFinish) {
                this.onFinish = onFinish;
                state = State.WARMUP;
                AtomicInteger counter = new AtomicInteger(CIRCLE_DURATION);

                BukkitTask circleTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        spawnCircleParticles();
                    }
                }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 10);

                Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(circleCentre.clone().add(0, 3, 0));
                hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
                hologram.getLines().appendText(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "FIELD BOSS");
                hologram.getLines().appendText(ChatColor.RED + "Stand in the summoning circle");
                hologram.getLines().appendText(ChatColor.RED + "to fight " + ChatColor.YELLOW + bossName);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerState playerState = playerStates.get(player);
                    if (playerState == null) continue;
                    if (playerState.canSpectate) {
                        player.sendTitle(ChatColor.GREEN + "Stand in the Circle", ChatColor.DARK_GREEN + "To fight " + ChatColor.YELLOW + ChatColor.BOLD + bossName, 10, 60, 10);
                        player.sendMessage(ChatColor.GREEN + "Stand in the summoning circle to fight " + ChatColor.YELLOW + ChatColor.BOLD + bossName);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 0.8f);
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int counterCurrent = counter.get();
                        if (counterCurrent == 15 || counterCurrent == 10 || counterCurrent <= 5) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                PlayerState playerState = playerStates.get(player);
                                if (playerState == null) continue;
                                if (playerState.canSpectate) {
                                    player.sendMessage(ChatColor.DARK_GREEN.toString() + counter + " seconds" + ChatColor.GREEN + " until field boss activates...");
                                }
                            }
                        }
                        counterCurrent = counter.decrementAndGet();
                        if (counterCurrent <= 0) {
                            this.cancel();
                            circleTask.cancel();
                            Bukkit.getScheduler().runTask(RunicCore.getInstance(), hologram::delete);
                            deactivate();
                        }
                    }
                }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20);
            }

            private void spawnCircleParticles() {
                assert circleCentre.getWorld() != null;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerState playerState = playerStates.get(player);
                    if (playerState == null) continue;
                    if (playerState.canSpectate) {
                        circleParticles.forEach(packet -> ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet));
                    }
                }
            }

            private void deactivate() { // Order of events in this function is EXTREMELY important
                updatePlayerStates(); // Update to get circle positions
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerState playerState = playerStates.get(player);
                    if (playerState == null) continue;
                    if (playerState.isInCircle) {
                        participants.put(player, 0L);
                    }
                }
                state = State.ACTIVE;
                updatePlayerStates(); // Update now that we are active state, and we have assigned participants
                String playersFighting = participants.keySet().stream().map(Player::getName).collect(Collectors.joining(", "));
                participants.keySet().forEach(player -> player.sendMessage(ChatColor.GREEN + "Players fighting field boss: " + playersFighting));
                double radiusSquared = domeRadius * domeRadius;
                Bukkit.getOnlinePlayers().stream()
                        .filter((player) -> playerStates.get(player) == null || !playerStates.get(player).isParticipant
                                && player.getWorld() == domeCentre.getWorld()
                                && player.getLocation().distanceSquared(domeCentre) <= radiusSquared)
                        .forEach((player) -> Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                            player.teleport(circleCentre);
                            player.sendMessage(ChatColor.RED + "You did not stand in the summoning circle and cannot fight the field boss!");
                        }));
                participants.keySet().forEach((player) -> player.sendTitle(ChatColor.DARK_RED + bossName, ChatColor.RED + "Field Boss", 10, 30, 10));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerState playerState = playerStates.get(player);
                    if (playerState == null) continue;
                    if (playerState.canSpectate) {
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 2.0f);
                    }
                }
                onFinish.run();
            }

        }

    }

}
