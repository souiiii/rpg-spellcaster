package com.shahid.spellcaster.particles;

import com.shahid.spellcaster.SpellcasterPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ParticleEngine {

    private final SpellcasterPlugin plugin;

    public ParticleEngine(SpellcasterPlugin plugin) {
        this.plugin = plugin;
    }

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

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> world.spawnParticle(particle, point, 1, 0, 0, 0, 0));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void drawSpiral(Location centre, int loops, double height, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        double totalAngle = 2 * Math.PI * loops;
        double angleStep = totalAngle / count;
        double yStep = height / count;
        double radius = 1.8;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    double theta = angleStep * i;

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

    public void drawSphere(Location centre, double radius, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        double goldenRatio = Math.PI * (3.0 - Math.sqrt(5.0));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {
                    double y = 1.0 - ((double) i / (count - 1)) * 2;
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

    public void drawRing(Location centre, double inner, double outer, int count, Particle particle) {
        World world = centre.getWorld();
        if (world == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < count; i++) {

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