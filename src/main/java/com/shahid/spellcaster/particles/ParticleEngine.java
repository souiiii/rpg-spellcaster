package com.shahid.spellcaster.particles;

import com.shahid.spellcaster.SpellcasterPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Particle Geometry Engine
 *
 * All shape-calculation is done asynchronously; individual particle spawns
 * are dispatched back to the main thread via Bukkit.getScheduler().runTask()
 * because World#spawnParticle is not thread-safe.
 *
 * Math reference:
 * - Circle : x = r·cos(θ), z = r·sin(θ)
 * - Spiral : as circle with y incremented per step
 * - Sphere : spherical coords — x = r·sin(φ)·cos(θ),
 * y = r·cos(φ),
 * z = r·sin(φ)·sin(θ)
 * - Line : linear interp P = A + t·(B-A), t ∈ [0,1]
 * - Ring : thin circle band — inner ≤ r ≤ outer, random angle
 */
public class ParticleEngine {

    private final SpellcasterPlugin plugin;

    public ParticleEngine(SpellcasterPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Circle ───────────────────────────────────────────────────────────────

    /**
     * Draw a horizontal circle of particles around the given centre.
     *
     * @param centre   Centre location
     * @param radius   Circle radius in blocks
     * @param count    Number of particle points along the circumference
     * @param particle Particle type
     */
    public void drawCircle(Location centre, double radius, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        double step = 2 * Math.PI / count;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    double theta = step * i;
                    double x = radius * Math.cos(theta);
                    double z = radius * Math.sin(theta);
                    Location point = centre.clone().add(x, 0, z);

                    // Dispatch to main thread for world-safe spawn
                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> world.spawnParticle(particle, point, 1, 0, 0, 0, 0));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    // ── Spiral ───────────────────────────────────────────────────────────────

    /**
     * Draw a helical spiral rising from centre.
     *
     * @param centre   Base location
     * @param loops    Number of full rotations
     * @param height   Total rise in blocks
     * @param count    Total particle steps
     * @param particle Particle type
     */
    public void drawSpiral(Location centre, int loops, double height, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        double totalAngle = 2 * Math.PI * loops;
        double angleStep = totalAngle / count;
        double yStep = height / count;
        double radius = 1.8; // Base spiral radius

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    double theta = angleStep * i;
                    // Radius shrinks as we rise to create a cone-spiral
                    double r = radius * (1.0 - (double) i / count);
                    double x = r * Math.cos(theta);
                    double y = yStep * i;
                    double z = r * Math.sin(theta);
                    Location point = centre.clone().add(x, y, z);

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> world.spawnParticle(particle, point, 1, 0, 0, 0, 0));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    // ── Sphere ───────────────────────────────────────────────────────────────

    /**
     * Draw a hollow sphere of particles centred on a location.
     * Uses Fibonacci sphere distribution for even spacing.
     *
     * @param centre   Centre location
     * @param radius   Sphere radius in blocks
     * @param count    Approximated point count (actual may vary)
     * @param particle Particle type
     */
    public void drawSphere(Location centre, double radius, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        // Fibonacci sphere (golden ratio) for even distribution
        double goldenRatio = Math.PI * (3.0 - Math.sqrt(5.0));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    double y = 1.0 - ((double) i / (count - 1)) * 2; // -1 to 1
                    double r = Math.sqrt(1 - y * y);
                    double theta = goldenRatio * i;
                    double x = Math.cos(theta) * r;
                    double z = Math.sin(theta) * r;
                    Location point = centre.clone().add(x * radius, y * radius, z * radius);

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> world.spawnParticle(particle, point, 1, 0, 0, 0, 0));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    // ── Line ─────────────────────────────────────────────────────────────────

    /**
     * Draw a straight line of particles between two points using lerp.
     * P(t) = A + t·(B-A), t ∈ [0,1]
     *
     * @param from     Start location
     * @param to       End location
     * @param count    Number of points along the line
     * @param particle Particle type
     */
    public void drawLine(Location from, Location to, int count, Particle particle) {
        World world = from.getWorld();
        if (world == null)
            return;

        Vector direction = to.toVector().subtract(from.toVector());

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i <= count; i++) {
                    double t = (double) i / count;
                    Location point = from.clone().add(direction.clone().multiply(t));

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> world.spawnParticle(particle, point, 1, 0, 0, 0, 0));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    // ── Ring ─────────────────────────────────────────────────────────────────

    /**
     * Draw a flat annular ring (donut) — useful for shockwave effects.
     *
     * @param centre   Centre location
     * @param inner    Inner radius
     * @param outer    Outer radius
     * @param count    Particle count
     * @param particle Particle type
     */
    public void drawRing(Location centre, double inner, double outer, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    // Uniform point in annulus: r = sqrt(rand * (outer² - inner²) + inner²)
                    double t = Math.random();
                    double r = Math.sqrt(t * (outer * outer - inner * inner) + inner * inner);
                    double theta = Math.random() * 2 * Math.PI;
                    double x = r * Math.cos(theta);
                    double z = r * Math.sin(theta);
                    Location point = centre.clone().add(x, 0, z);

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> world.spawnParticle(particle, point, 1, 0, 0, 0, 0));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    // ── Animated Ring Expansion ───────────────────────────────────────────────

    /**
     * Animates an expanding ring from radius 0 to maxRadius over durationTicks.
     * Useful for shockwave / nova effects.
     */
    public void animateExpandingRing(Location centre, double maxRadius,
            int durationTicks, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;
        int pointsPerFrame = 36;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTicks) {
                    cancel();
                    return;
                }
                double r = maxRadius * ((double) tick / durationTicks);
                double step = 2 * Math.PI / pointsPerFrame;
                for (int i = 0; i < pointsPerFrame; i++) {
                    double theta = step * i;
                    double x = r * Math.cos(theta);
                    double z = r * Math.sin(theta);
                    Location point = centre.clone().add(x, 0.1, z);
                    world.spawnParticle(particle, point, 1, 0, 0, 0, 0);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
