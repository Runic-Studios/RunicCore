package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class SlashEffect {

    public static void slashHorizontal(Location location) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();
        boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
        slashHorizontal(location, topOrBottom, leftOrRight, Particle.CRIT, .05f);

    }

    public static void slashVertical(Player player) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();
        boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
        slashVertical(player, topOrBottom, leftOrRight, Particle.CRIT, .05f);
    }

    /**
     * Creates a horizontal-style slash
     *
     * @param location
     * @param particle
     * @param color    optional param for redstone particle
     */
    public static void slashHorizontal(Location location, Particle particle, Color... color) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();
        boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
        slashHorizontal(location, topOrBottom, leftOrRight, particle, .05f, color);
    }

    /**
     * Creates a vertical-style slash
     *
     * @param player
     * @param particle
     * @param color    optional param for redstone particle
     */
    public static void slashVertical(Player player, Particle particle, Color... color) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();
        boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
        slashVertical(player, topOrBottom, leftOrRight, particle, .05f, color);
    }

    /**
     * Creates a vertical-style slash
     *
     * @param leftOrRight control which direction the slash comes from
     */
    public static void slashVertical(Player player, Particle particle,
                                     boolean leftOrRight, Color... color) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();
        slashVertical(player, leftOrRight, leftOrRight, particle, .05f, color);
    }

    /**
     * @param density of the particles
     */
    public static void slashVertical(Player player, Particle particle,
                                     boolean leftOrRight, float density, Color... color) {
        slashVertical(player, leftOrRight, leftOrRight, particle, density, color);
    }

    public static void slashHorizontal(Player player, Particle particle, float density) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();

        boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
        slashVertical(player, topOrBottom, leftOrRight, particle, density);
    }

    public static void slashVertical(Player player, Particle particle, float density) {
        boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();
        boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
        slashVertical(player, topOrBottom, leftOrRight, particle, density);
    }

    /**
     * draws a slash particle in front of the player
     *
     * @param player      player to draw the particles for
     * @param topOrBottom if true slash top to bottom
     * @param leftOrRight if true slashes from left to right
     * @param particle    the particle to display
     * @param density     space between particles
     * @param color       optional param for redstone particle
     */
    public static void slashVertical(Player player, boolean topOrBottom, boolean leftOrRight,
                                     Particle particle, float density, Color... color) {

        Location playerLocationClone = player.getLocation().clone();

        // Clamp the players
        if (playerLocationClone.getPitch() > 60 || playerLocationClone.getPitch() < -60)
            playerLocationClone.setPitch(0);

        playerLocationClone.add(0, 1, 0);

        int direction = 1;
        if (topOrBottom) direction = -1;
        int count = 0;
        int timer = 0;

        if (leftOrRight) {
            for (double i = -1.5; i < 1.5; i += density) {
                if (count > 10) {

                    timer++;
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                    count = 0;
                } else {
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                }
                count++;
            }
        } else {
            for (double i = 1.5; i > -1.5; i -= density) {
                if (count > 10) {
                    timer++;
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                    count = 0;
                } else {
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                player.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                }
                count++;
            }
        }

    }

    /**
     * draws a slash particle in front of the player
     *
     * @param location    to draw the particles for
     * @param topOrBottom if true slash top to bottom
     * @param leftOrRight if true slashes from left to right
     * @param particle    the particle to display
     * @param density     space between particles
     * @param color       optional param for redstone particle
     */
    public static void slashHorizontal(Location location, boolean topOrBottom, boolean leftOrRight,
                                       Particle particle, float density, Color... color) {
        Location playerLocationClone = location.clone();
        if (playerLocationClone.getPitch() > 60 || playerLocationClone.getPitch() < -60)
            playerLocationClone.setPitch(0);
        playerLocationClone.add(0, 1, 0);
        int direction = 1;
        if (topOrBottom) direction = -1;
        int count = 0;
        int timer = 0;
        if (leftOrRight) {
            for (double i = -1; i < 1; i += density) {
                if (count > 10) {
                    timer++;
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                    count = 0;
                } else {
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                }
                count++;
            }
        } else {
            for (double i = 1; i > -1; i -= density) {
                if (count > 10) {
                    timer++;
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                    count = 0;
                } else {
                    double finalI = i;
                    int finalDirection = direction;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location location = playerLocationClone.clone().add(playerLocationClone.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
                            if (particle == Particle.REDSTONE && color.length > 0) {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0,
                                        new Particle.DustOptions(color[0], 1));
                            } else {
                                location.getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
                            }
                        }
                    }.runTaskLater(RunicCore.getInstance(), timer);
                }
                count++;
            }
        }
    }

}
