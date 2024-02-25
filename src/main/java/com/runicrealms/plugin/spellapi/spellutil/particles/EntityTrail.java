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

            PARTICLES.forEach(((entity, particleData) -> {
                if (entity.isDead() || (particleData.getDuration() != null && now >= particleData.getStart() + particleData.getDuration() * 50)) {
                    PARTICLES.remove(entity);
                    return;
                }

                entity.getWorld().spawnParticle(
                        particleData.getParticle(),
                        particleData.getOffset() == 0 ? entity.getLocation() : entity.getLocation().clone().add(0, particleData.getOffset(), 0),
                        1,
                        0,
                        0,
                        0,
                        0,
                        particleData.getExtra()
                );
            }));
        }, 0, 1);
    }

    /**
     * A method used to set a particle trail
     *
     * @param entity   the entity
     * @param particle the particle effect
     * @param duration the duration it should last in ticks (or null if no duration)
     * @param extra    extra particle data
     */
    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle, @Nullable Long duration, @Nullable Particle.DustOptions extra, float offset) {
        PARTICLES.put(entity, new ParticleData(particle, duration, extra, offset));
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle, @Nullable Long duration, float offset) {
        EntityTrail.entityTrail(entity, particle, duration, null, offset);
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle, float offset) {
        EntityTrail.entityTrail(entity, particle, null, offset);
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Particle particle) {
        EntityTrail.entityTrail(entity, particle, null, 0);
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Color color, float offset) {
        PARTICLES.put(entity, new ParticleData(Particle.REDSTONE, null, new Particle.DustOptions(color, 1), offset));
    }

    public static void entityTrail(@NotNull Entity entity, @NotNull Color color) {
        PARTICLES.put(entity, new ParticleData(Particle.REDSTONE, null, new Particle.DustOptions(color, 1), 0));
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
        private final float offset;

        public ParticleData(@NotNull Particle particle, @Nullable Long duration, @Nullable Particle.DustOptions extra, float offset) {
            this.particle = particle;
            this.duration = duration;
            this.start = System.currentTimeMillis();
            this.extra = extra;
            this.offset = offset;
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

        public float getOffset() {
            return offset;
        }
    }
}
