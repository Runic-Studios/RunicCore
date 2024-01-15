package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HolyFervorEffect implements SpellEffect {
    private final Player caster;
    private final double duration;
    boolean x = true;
    boolean o = false;
    private final boolean[][] wingShapeMatrix = {
            {o, o, o, o, o, o, o, o, o, o, o, o, o, o, o, o},
            {o, o, o, x, o, o, o, o, o, o, o, o, x, o, o, o},
            {o, o, x, x, o, o, o, o, o, o, o, o, x, x, o, o},
            {o, x, x, x, x, o, o, o, o, o, o, x, x, x, x, o},
            {o, x, x, x, x, o, o, o, o, o, o, x, x, x, x, o},
            {o, o, x, x, x, x, o, o, o, o, x, x, x, x, o, o},
            {o, o, o, x, x, x, x, o, o, x, x, x, x, o, o, o},
            {o, o, o, o, x, x, x, x, x, x, x, x, o, o, o, o},
            {o, o, o, o, o, x, x, x, x, x, x, o, o, o, o, o},
            {o, o, o, o, o, o, x, x, x, x, o, o, o, o, o, o},
            {o, o, o, o, o, x, x, o, o, x, x, o, o, o, o, o},
            {o, o, o, o, x, x, x, o, o, x, x, x, o, o, o, o},
            {o, o, o, o, x, x, o, o, o, o, x, x, o, o, o, o},
            {o, o, o, o, x, o, o, o, o, o, o, x, o, o, o, o},
            {o, o, o, o, o, o, o, o, o, o, o, o, o, o, o, o},
    };
    private long startTime;

    /**
     * @param caster   player who is incendiary
     * @param duration (in seconds) before the effect expires
     */
    public HolyFervorEffect(Player caster, double duration) {
        this.caster = caster;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.executeSpellEffect(); // show particles
    }

    public static Vector rotateAroundAxisY(Vector v, double fire) {
        double x, z, cos, sin;
        cos = Math.cos(fire);
        sin = Math.sin(fire);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static Vector getBackVector(Location loc) {
        final float newZ = (float) (loc.getZ() + (1 * Math.sin(Math.toRadians(loc.getYaw() + 90))));
        final float newX = (float) (loc.getX() + (1 * Math.cos(Math.toRadians(loc.getYaw() + 90))));
        return new Vector(newX - loc.getX(), 0, newZ - loc.getZ());
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.HOLY_FERVOR;
    }

    @Override
    public boolean isBuff() {
        return true;
    }

    @Override
    public Player getCaster() {
        return caster;
    }

    @Override
    public LivingEntity getRecipient() {
        return caster;
    }

    @Override
    public void tick(int globalCounter) {
        if (caster.isDead()) {
            this.cancel();
            return;
        }
        if (globalCounter % 20 == 0) { // Show particle once per second
            executeSpellEffect();
        }
    }

    @Override
    public void executeSpellEffect() {
        drawParticles(caster, caster.getEyeLocation());
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    private void drawParticles(Player player, Location location) {
        double space = 0.20;
        // This value centers the wings horizontally; tweak as necessary.
        double defX = location.getX() - (space * wingShapeMatrix[0].length / 2) + 0.1f;
        double x = defX;
        // This value adjusts the vertical start position of the wings; reduce it to bring wings closer.
        double y = location.clone().getY() + 1.5;
        // This adjusts the angle of the wings; tweak the division factor and the added constant.
        double fire = -((location.getYaw() + 180) / 60);
        fire += (location.getYaw() < -180 ? 3.25 : 2.985);

        for (boolean[] booleans : wingShapeMatrix) {
            for (boolean aBoolean : booleans) {
                if (aBoolean) {
                    Location target = location.clone();
                    target.setX(x);
                    target.setY(y);

                    Vector v = target.toVector().subtract(location.toVector());
                    Vector v2 = getBackVector(location);
                    v = rotateAroundAxisY(v, fire);
                    // Adjust this to change the distance of the wings from the player's back.
                    v2.setY(0).multiply(-0.4);

                    location.add(v);
                    location.add(v2);
                    for (int k = 0; k < 3; k++) {
                        player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 1, 0, 0, 0, 0);
                    }
                    location.subtract(v2);
                    location.subtract(v);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }

}
