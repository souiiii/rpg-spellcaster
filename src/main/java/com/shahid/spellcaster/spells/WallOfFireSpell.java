package com.shahid.spellcaster.spells;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.particles.ParticleEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WallOfFireSpell implements Spell {

    private static final String NAME = "wall_of_fire";

    private final SpellcasterPlugin plugin;
    private final ParticleEngine particles;

    public WallOfFireSpell(SpellcasterPlugin plugin) {
        this.plugin = plugin;
        this.particles = new ParticleEngine(plugin);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "§6Creates a blazing wall of fire in front of you.";
    }

    @Override
    public void cast(Player caster, Location origin) {
        double wallWidth = plugin.getConfig().getDouble("wall_of_fire.width", 8.0);
        double wallHeight = plugin.getConfig().getDouble("wall_of_fire.height", 4.0);
        double depth = plugin.getConfig().getDouble("wall_of_fire.depth", 0.5);
        double damagePerTick = plugin.getConfig().getDouble("wall_of_fire.damage-per-tick", 1.5);
        int durationSec = plugin.getConfig().getInt("wall_of_fire.duration-seconds", 6);
        int density = plugin.getConfig().getInt("wall_of_fire.particle-density", 5);

        Vector look = caster.getEyeLocation().getDirection().setY(0).normalize();
        Vector up = new Vector(0, 1, 0);

        Vector right = look.clone().crossProduct(up).normalize();

        Vector wallUp = right.clone().crossProduct(look).normalize();

        Location wallCentre = caster.getEyeLocation().add(look.multiply(3.0));

        List<Location> wallPoints = buildWallPoints(wallCentre, right, wallUp,
                wallWidth, wallHeight, density);

        int totalTicks = durationSec * 20;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= totalTicks) {
                    cancel();
                    return;
                }

                Particle p = (tick % 6 < 3) ? Particle.FLAME : Particle.LAVA;
                for (Location pt : wallPoints) {
                    wallCentre.getWorld().spawnParticle(p, pt, 1, 0.1, 0.1, 0.1, 0.01);
                }

                if (tick % 10 == 0) {
                    damageEntitiesInWall(wallCentre, right, wallUp,
                            wallWidth, wallHeight, depth, damagePerTick, caster);
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        origin.getWorld().playSound(origin, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 0.8f);
        origin.getWorld().playSound(origin, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
        caster.sendActionBar(Component.text("✦ Wall of Fire! (" + durationSec + "s)", NamedTextColor.GOLD));
    }

    private List<Location> buildWallPoints(Location centre, Vector right, Vector up,
            double width, double height, int density) {
        List<Location> points = new ArrayList<>();
        double stepW = 1.0 / density;
        double stepH = 1.0 / density;

        for (double w = -width / 2; w <= width / 2; w += stepW) {
            for (double h = 0; h <= height; h += stepH) {
                Location pt = centre.clone()
                        .add(right.clone().multiply(w))
                        .add(up.clone().multiply(h));
                points.add(pt);
            }
        }
        return points;
    }

    private void damageEntitiesInWall(Location centre, Vector right, Vector up,
            double width, double height, double depth,
            double damage, Player shooter) {
        double halfW = width / 2;
        double halfD = depth / 2;

        Collection<Entity> nearby = centre.getWorld()
                .getNearbyEntities(centre, halfW + 1, height / 2 + 1, halfD + 1);

        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity le))
                continue;
            if (entity.equals(shooter))
                continue;

            Vector diff = entity.getLocation().toVector().subtract(centre.toVector());

            double projRight = Math.abs(diff.dot(right));
            double projUp = diff.dot(up);
            double projDepth = Math.abs(diff.dot(centre.getDirection() != null
                    ? right.clone().crossProduct(up).normalize()
                    : new Vector(1, 0, 0)));

            if (projRight <= halfW && projUp >= 0 && projUp <= height && projDepth <= halfD + 0.5) {
                le.damage(damage, shooter);
                le.setFireTicks(30);
            }
        }
    }
}