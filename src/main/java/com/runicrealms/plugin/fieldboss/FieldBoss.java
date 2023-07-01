package com.runicrealms.plugin.fieldboss;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FieldBoss implements Listener {

    private static final double DOME_PARTICLE_DENSITY = 0.1; // particles per square block
    private static final double CIRCLE_PARTICLE_COUNT = 32;
    // TODO use:
    private static final int ADDITIONAL_ATTENTION_RADIUS = 30; // block number added to radius in which players will be notified of field boss stuff happening

    private final String identifier;
    private final String bossName;
    private final String mmID;
    private final Location domeCentre; // Centre of the dome, not where the shrine is
    private final double domeRadius;
    private final Location tributeChest;
    private final Location circleCentre;
    private final double circleRadius;
    private final @Nullable GuildScore guildScore;
    private final Set<Location> domeParticleLocations = new HashSet<>();
    private final Set<Location> circleParticleLocations = new HashSet<>();
    private @Nullable ActiveState active; // null means inactive
    private @Nullable BukkitTask spoilsTask; // null means spoils active

    public FieldBoss(String identifier,
                     String bossName,
                     String mmID,
                     Location domeCentre,
                     double domeRadius,
                     Location tributeChest,
                     Location circleCentre,
                     double circleRadius,
                     @Nullable GuildScore guildScore) {
        this.identifier = identifier;
        this.bossName = bossName;
        this.mmID = mmID;
        this.domeCentre = domeCentre;
        this.domeRadius = domeRadius;
        this.tributeChest = tributeChest;
        this.circleCentre = circleCentre;
        this.circleRadius = circleRadius;
        this.guildScore = guildScore;
        try (MythicBukkit mythicBukkit = MythicBukkit.inst()) {
            if (mythicBukkit.getAPIHelper().getMythicMob(mmID) == null)
                throw new IllegalArgumentException("Field Boss " + identifier + " has invalid MM ID " + mmID);
        }
        if (tributeChest.getBlock().getType() != Material.CHEST) tributeChest.getBlock().setType(Material.CHEST);
        if (Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), this::loadParticleLocations);
        else loadParticleLocations();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void activate() {
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

    private void loadParticleLocations() {
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
            domeParticleLocations.add(particleLocation);
        }

        for (int i = 0; i < CIRCLE_PARTICLE_COUNT; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_PARTICLE_COUNT;
            double x = circleCentre.getX() + circleRadius * Math.cos(angle);
            double z = circleCentre.getZ() + circleRadius * Math.sin(angle);
            double y = circleCentre.getY();
            circleParticleLocations.add(new Location(circleCentre.getWorld(), x, y, z));
        }
    }

    @EventHandler
    public void onPlayerOpenChest(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.CHEST
                && event.getClickedBlock().getLocation().getWorld() == tributeChest.getWorld()
                && event.getClickedBlock().getX() == tributeChest.getBlockX()
                && event.getClickedBlock().getY() == tributeChest.getBlockY()
                && event.getClickedBlock().getZ() == tributeChest.getBlockZ()) {
            if (spoilsTask == null) {

            } else {

            }
        }
    }

    private Stream<? extends Player> getPlayersInDome() {
        double radiusSquared = domeRadius * domeRadius;
        return Bukkit.getOnlinePlayers().stream().filter((player) ->
                player.getWorld() == domeCentre.getWorld()
                        && player.getLocation().distanceSquared(domeCentre) <= radiusSquared);
    }

    private Stream<? extends Player> getPlayersInArea() {
        double radiusSquared = (domeRadius + 30) * (domeRadius + 30);
        return Bukkit.getOnlinePlayers().stream().filter((player) ->
                player.getWorld() == domeCentre.getWorld()
                        && player.getLocation().distanceSquared(domeCentre) <= radiusSquared);
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

        private final UUID entityID;
        private final Set<Player> participants = new HashSet<>();
        private final BukkitTask particleTask;
        private final Map<UUID, Integer> damageDealt = new HashMap<>();
        private @Nullable BukkitTask movementTask;
        private State state = State.WARMUP;

        private ActiveState() {
            Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
            particleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    spawnDomeParticles();
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
            new WarmupCircle(() -> {
                state = State.ACTIVE;
                movementTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        teleportDomePlayers();
                    }
                }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 8);
            });
        }

        private void spawnDomeParticles() {
            assert domeCentre.getWorld() != null;
            for (Location particleLocation : domeParticleLocations) {
                domeCentre.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, state.particleDustOptions, true);
            }
        }

        private void teleportDomePlayers() {
            double radiusSquared = domeRadius * domeRadius;
            double radiusCheatingInsideSquared = (domeRadius + 4) * (domeRadius + 4);
            double radiusCheatingOutsideSquared = (domeRadius - 4) * (domeRadius - 4);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() != domeCentre.getWorld()) continue;
                double distanceSquared = player.getLocation().distanceSquared(domeCentre);
                if (participants.contains(player)) { // Inside the dome
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
                } else { // Outside the dome
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
            if (state == State.WARMUP || !participants.contains(event.getPlayer()))
                event.setCancelled(true);
            else trackBossDamage(event.getPlayer(), event.getAmount());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPhysicalDamage(PhysicalDamageEvent event) {
            if (!event.getVictim().getUniqueId().equals(entityID)) return;
            if (state == State.WARMUP || !participants.contains(event.getPlayer()))
                event.setCancelled(true);
            else trackBossDamage(event.getPlayer(), event.getAmount());
        }

        private void trackBossDamage(Player player, int amount) {
            if (!damageDealt.containsKey(player.getUniqueId()))
                damageDealt.put(player.getUniqueId(), 0);
            damageDealt.put(player.getUniqueId(), damageDealt.get(player.getUniqueId()) + amount);
        }

        @EventHandler
        public void onRunicDeath(RunicDeathEvent event) {
            participants.remove(event.getVictim());
        }

        @EventHandler
        public void onMythicMobDeath(MythicMobDeathEvent event) {
            if (event.getEntity().getUniqueId().equals(entityID)) {
                if (guildScore != null)
                    guildScore.getDistribution().distribute(damageDealt, event.getMob().getEntity().getMaxHealth());
                deactivate();
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            participants.remove(event.getPlayer());
        }

        private void deactivate() {
            if (state != State.ACTIVE)
                throw new IllegalStateException("Cannot deactivate active state when still in warmup!");
            HandlerList.unregisterAll(this);
            particleTask.cancel();
            if (movementTask != null) movementTask.cancel();
            getPlayersInDome().forEach((player) -> player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 2.0f));
            participants.forEach((player) -> player.sendMessage(ChatColor.GREEN + "You defeated the " + ChatColor.YELLOW + bossName + ChatColor.GREEN + "! Collect the spoils from the chest near you."));
            active = null;
            for (Map.Entry<UUID, Integer> entry : damageDealt.entrySet()) {
                Player target = Bukkit.getPlayer(entry.getKey());
                if (target == null) continue;
                target.sendMessage(ChatColor.GREEN + "You dealt " + ChatColor.DARK_GREEN + ChatColor.BOLD + entry.getValue() + ChatColor.GREEN + " damage to the boss!");
            }
            damageDealt.clear();
        }

        public enum State {
            ACTIVE(new Particle.DustOptions(Color.RED, 1)),
            WARMUP(new Particle.DustOptions(Color.YELLOW, 1));

            private final Particle.DustOptions particleDustOptions;

            State(Particle.DustOptions particleDustOptions) {
                this.particleDustOptions = particleDustOptions;
            }
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
                getPlayersInArea().forEach((player) -> {
                    player.sendTitle(ChatColor.GREEN + "Stand in the Circle", ChatColor.DARK_GREEN + "To fight " + ChatColor.YELLOW + ChatColor.BOLD + bossName, 10, 60, 10);
                    player.sendMessage(ChatColor.GREEN + "Stand in the summoning circle to fight " + ChatColor.YELLOW + ChatColor.BOLD + bossName);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.0f, 0.8f);
                });
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int counterCurrent = counter.get();
                        if (counterCurrent == 15 || counterCurrent == 10 || counterCurrent <= 5) {
                            getPlayersInArea().forEach(player -> {
                                player.sendMessage(ChatColor.DARK_GREEN.toString() + counter + " seconds" + ChatColor.GREEN + " until field boss activates...");
                            });
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
                for (Location particleLocation : circleParticleLocations) {
                    circleCentre.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, particleLocation, 1, 0, 0, 0, 0, null, true);
                }
            }

            private void deactivate() {
                double circleRadiusSquared = circleRadius * circleRadius;
                participants.addAll(Bukkit.getOnlinePlayers().stream()
                        .filter((player) -> player.getWorld() == circleCentre.getWorld()
                                && player.getLocation().distanceSquared(circleCentre) <= circleRadiusSquared)
                        .collect(Collectors.toUnmodifiableSet()));
                double radiusSquared = domeRadius * domeRadius;
                Bukkit.getOnlinePlayers().stream()
                        .filter((player) -> !participants.contains(player)
                                && player.getWorld() == domeCentre.getWorld()
                                && player.getLocation().distanceSquared(domeCentre) <= radiusSquared)
                        .forEach((player) -> Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.teleport(circleCentre)));
                participants.forEach((player) -> player.sendTitle(ChatColor.DARK_RED + bossName, ChatColor.RED + "Field Boss", 10, 30, 10));
                getPlayersInArea().forEach((player) -> player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 2.0f));
                onFinish.run();
            }

        }

    }

}
