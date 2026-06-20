package com.diasmp.diaswords.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Helper methods for spawning the visual particle effects used by sword abilities.
 * Centralized here so effect tweaks don't require touching ability logic.
 */
public final class ParticleUtils {

    private ParticleUtils() {}

    /** Trail of particles left behind a dashing player. */
    public static void dashTrail(Location loc) {
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 6, 0.3, 0.2, 0.3, 0.02);
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 4, 0.2, 0.2, 0.2, 0.05);
    }

    /** Burst when a dash begins. */
    public static void dashBurst(Location loc) {
        loc.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.4, 0.4, 0.4, 0.1);
        loc.getWorld().spawnParticle(Particle.POOF, loc, 10, 0.3, 0.3, 0.3, 0.05);
    }

    /** Red drain wisps for the vampire sword's heal-on-hit. */
    public static void vampireDrain(Location loc) {
        loc.getWorld().spawnParticle(Particle.DUST, loc, 14, 0.3, 0.4, 0.3, 0.0,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 0, 0), 1.3f));
        loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 3, 0.2, 0.2, 0.2, 0.0);
    }

    /** Brief electric sparks at the strike location for lightning sword procs (non-skybolt version). */
    public static void lightningSparks(Location loc) {
        loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 25, 0.4, 0.5, 0.4, 0.15);
    }

    /** Icy burst centered on the caster for frost sword. */
    public static void frostBurst(Location loc, double radius) {
        loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 40, radius * 0.6, 0.6, radius * 0.6, 0.05);
        loc.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, loc, 20, radius * 0.5, 0.4, radius * 0.5, 0.02);
    }

    /** Freeze particles on a rooted target. */
    public static void frostFreeze(Location loc) {
        loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 10, 0.3, 0.6, 0.3, 0.02);
    }

    /** Spiraling pull-in particles for vortex sword, drawn between caster and target. */
    public static void vortexSpiral(Location from, Location to) {
        Vector dir = to.toVector().subtract(from.toVector());
        double distance = dir.length();
        if (distance < 0.1) return;
        dir.normalize();
        int steps = (int) Math.max(4, distance * 3);
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            Location point = from.clone().add(dir.clone().multiply(distance * t));
            point.getWorld().spawnParticle(Particle.PORTAL, point, 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    /** Pulse at the vortex caster's feet when activated. */
    public static void vortexPulse(Location loc) {
        loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 30, 0.6, 0.2, 0.6, 0.1);
    }

    /** Trail left behind the explosive sword's fireball projectile. */
    public static void fireballTrail(Location loc) {
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0.1, 0.1, 0.1, 0.01);
        loc.getWorld().spawnParticle(Particle.SMOKE, loc, 2, 0.1, 0.1, 0.1, 0.01);
    }
}
