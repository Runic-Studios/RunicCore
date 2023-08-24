package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class used to add trails to entities
 *
 * @author BoBoBalloon
 */
public final class EntityTrail {
    private static final Map<Entity, ParticleData> PARTICLES = new ConcurrentHashMap<>();

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (PARTICLES.isEmpty()) {
                return;
            }

            long now = System.currentTimeMillis();

            for (Map.Entry<Entity, ParticleData> entry : PARTICLES.entrySet()) {
                ParticleData data = entry.getValue();

                if (entry.getKey().isDead() || (data.getDuration() != null && now >= data.getStart() + data.getDuration() * 50)) {
                    PARTICLES.remove(entry.getKey());
                    continue;
                }

                entry.getKey().getWorld().spawnParticle(data.getParticle(), entry.getKey().getLocation(), 1, 0, 0, 0, 0, data.getExtra());
            }
        }, 0, 1);
    }

    private EntityTrail() {

    }

    /**
     * A method used to set a particle trail
     *
     * @param entity   the entity
     * @param particle the particle effect
     * @param duration the duration it should last in ticks (or null if no duration)
     * @param extra    extra particle data
     */
    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle, @Nullable Long duration, @Nullable Particle.DustOptions extra) {
        PARTICLES.put(entity, new ParticleData(particle, duration, extra));
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle, @Nullable Long duration) {
        EntityTrail.entityTrail(entity, particle, duration, null);
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle) {
        EntityTrail.entityTrail(entity, particle, null);
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Color color) {
        PARTICLES.put(entity, new ParticleData(Particle.REDSTONE, null, new Particle.DustOptions(color, 1)));
    }

    public static void removeTrail(@NotNull Entity entity) {
        PARTICLES.remove(entity);
    }

    /**
     * A class used to keep track of extra data for the particle effect
     *
     * @author BoBoBalloon
     */
    private static class ParticleData {
        private final Particle particle;
        private final Long duration;
        private final long start;
        private final Particle.DustOptions extra;

        public ParticleData(@NotNull Particle particle, @Nullable Long duration, @Nullable Particle.DustOptions extra) {
            this.particle = particle;
            this.duration = duration;
            this.start = System.currentTimeMillis();
            this.extra = extra;
        }

        @NotNull
        public Particle getParticle() {
            return this.particle;
        }

        /**
         * @return how many ticks the effect should last
         */
        @Nullable
        public Long getDuration() {
            return this.duration;
        }

        /**
         * @return the time that the effect started
         */
        public long getStart() {
            return this.start;
        }

        @Nullable
        public Particle.DustOptions getExtra() {
            return this.extra;
        }
    }
}
