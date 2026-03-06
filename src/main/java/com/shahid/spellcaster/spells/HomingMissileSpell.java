package com.shahid.spellcaster.spells;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.particles.ParticleEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;

/**
 * Homing Missile Spell
 *
 * Launches a tracking Snowball projectile that seeks the nearest entity.
 * Every tick, its velocity is recalculated: v = normalize(target - missile) *
 * speed
 * Produces a blazing particle trail and detonates on impact.
 *
 * TaggedKey: "homing_missile" stored in entity's metadata for listener
 * detection.
 */
public class HomingMissileSpell implements Spell {

    private static final String NAME = "homing_missile";
    public static final String METADATA_KEY = "homing_missile_owner";

    private final SpellcasterPlugin plugin;
    private final ParticleEngine particles;

    public HomingMissileSpell(SpellcasterPlugin plugin) {
        this.plugin = plugin;
        this.particles = new ParticleEngine(plugin);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "§cLaunches a tracking fireball that seeks the nearest enemy.";
    }

    @Override
    public void cast(Player caster, Location origin) {
        double trackingRange = plugin.getConfig().getDouble("homing_missile.tracking-range", 24.0);
        double baseSpeed = plugin.getConfig().getDouble("homing_missile.base-speed", 0.6);
        double turnSpeed = plugin.getConfig().getDouble("homing_missile.turn-speed", 0.18);
        int lifetime = plugin.getConfig().getInt("homing_missile.lifetime-ticks", 100);
        int trailCount = plugin.getConfig().getInt("homing_missile.trail-particle-count", 6);

        // ── Spawn the Snowball missile ──────────────────────────────────────
        Location spawnLoc = caster.getEyeLocation();
        Vector direction = caster.getEyeLocation().getDirection().normalize().multiply(baseSpeed);
        Snowball missile = caster.getWorld().spawn(spawnLoc, Snowball.class, sb -> {
            sb.setShooter(caster);
            sb.setVelocity(direction);
            sb.setMetadata(METADATA_KEY,
                    new org.bukkit.metadata.FixedMetadataValue(plugin, caster.getUniqueId().toString()));
        });

        // ── Glow ─────────────────────────────────────────────────────────────
        missile.setGlowing(true);

        UUID missileId = missile.getUniqueId();

        // ── Homing tracking task ──────────────────────────────────────────────
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                // Safety checks
                Entity m = plugin.getServer().getEntity(missileId);
                if (m == null || m.isDead() || !m.isValid() || tick >= lifetime) {
                    cancel();
                    return;
                }

                // ── Find nearest living entity ────────────────────────────────
                Collection<Entity> nearby = m.getWorld()
                        .getNearbyEntities(m.getLocation(), trackingRange, trackingRange, trackingRange);

                LivingEntity target = null;
                double closestDist = Double.MAX_VALUE;

                for (Entity e : nearby) {
                    if (e.equals(caster) || e.equals(m))
                        continue;
                    if (!(e instanceof LivingEntity le))
                        continue;
                    double d = e.getLocation().distanceSquared(m.getLocation());
                    if (d < closestDist) {
                        closestDist = d;
                        target = le;
                    }
                }

                // ── Steer toward target ───────────────────────────────────────
                if (target != null) {
                    Vector toTarget = target.getLocation().add(0, 0.9, 0).toVector()
                            .subtract(m.getLocation().toVector())
                            .normalize();
                    Vector currentVel = m.getVelocity();
                    // Gradually blend current direction toward target direction
                    Vector newVel = currentVel.multiply(1 - turnSpeed)
                            .add(toTarget.multiply(turnSpeed))
                            .normalize()
                            .multiply(baseSpeed + 0.3); // Slightly accelerate
                    m.setVelocity(newVel);
                }

                // ── Particle trail ────────────────────────────────────────────
                Location mLoc = m.getLocation();
                m.getWorld().spawnParticle(Particle.FLAME, mLoc, trailCount, 0.05, 0.05, 0.05, 0.01);
                m.getWorld().spawnParticle(Particle.SMOKE_NORMAL, mLoc, 2, 0.02, 0.02, 0.02, 0.01);

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // ── Feedback ─────────────────────────────────────────────────────────
        origin.getWorld().playSound(origin, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.5f);
        caster.sendActionBar(Component.text("✦ Homing Missile fired!", NamedTextColor.RED));
    }

    /**
     * Detonation effect — called from ProjectileHitListener.
     */
    public void detonate(Location loc, Player shooter) {
        double damage = plugin.getConfig().getDouble("homing_missile.damage", 8.0);
        double blastRadius = plugin.getConfig().getDouble("homing_missile.blast-radius", 3.0);

        // ── Sphere burst ──────────────────────────────────────────────────────
        particles.drawSphere(loc, blastRadius, 80, Particle.EXPLOSION_LARGE);
        particles.drawRing(loc, 0.2, blastRadius, 50, Particle.FLAME);

        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_DEATH, 1.0f, 0.5f);

        // ── AoE damage ────────────────────────────────────────────────────────
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, blastRadius, blastRadius, blastRadius);
        for (Entity e : nearby) {
            if (!(e instanceof LivingEntity le))
                continue;
            if (shooter != null && e.equals(shooter))
                continue;
            double dist = e.getLocation().distance(loc);
            double scaled = damage * (1.0 - (dist / blastRadius));
            if (scaled > 0)
                le.damage(scaled, shooter);
        }
    }
}
