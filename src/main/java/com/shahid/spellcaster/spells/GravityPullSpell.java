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

import java.util.Collection;

public class GravityPullSpell implements Spell {

    private static final String NAME = "gravity_pull";

    private final SpellcasterPlugin plugin;
    private final ParticleEngine particles;

    public GravityPullSpell(SpellcasterPlugin plugin) {
        this.plugin = plugin;
        this.particles = new ParticleEngine(plugin);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "§5Pulls all nearby entities to you with arcane gravity.";
    }

    @Override
    public void cast(Player caster, Location origin) {
        double radius = plugin.getConfig().getDouble("gravity_pull.radius", 12.0);
        double force = plugin.getConfig().getDouble("gravity_pull.force", 1.4);
        double baseDamage = plugin.getConfig().getDouble("gravity_pull.damage", 3.5);
        int partCount = plugin.getConfig().getInt("gravity_pull.particle-count", 80);
        int durationTick = plugin.getConfig().getInt("gravity_pull.duration-ticks", 20);

        origin.getWorld().playSound(origin, Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 0.5f);
        origin.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.7f);

        particles.animateExpandingRing(origin, radius, 15, Particle.PORTAL);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTick) {
                    cancel();
                    return;
                }

                Collection<Entity> nearby = origin.getWorld()
                        .getNearbyEntities(origin, radius, radius, radius);

                for (Entity entity : nearby) {
                    if (!(entity instanceof LivingEntity living))
                        continue;
                    if (entity.equals(caster))
                        continue;

                    Vector towardCaster = origin.toVector()
                            .subtract(living.getLocation().toVector())
                            .normalize()
                            .multiply(force);

                    towardCaster.setY(towardCaster.getY() + 0.3);
                    living.setVelocity(towardCaster);

                    if (tick % 3 == 0) {
                        particles.drawSpiral(living.getLocation(), 1, 1.0,
                                partCount / 4, Particle.ENCHANTMENT_TABLE);
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<Entity> nearby = origin.getWorld()
                        .getNearbyEntities(origin, radius, radius, radius);

                for (Entity entity : nearby) {
                    if (!(entity instanceof LivingEntity living))
                        continue;
                    if (entity.equals(caster))
                        continue;

                    double dist = living.getLocation().distance(origin);
                    double damage = baseDamage * (1.0 - (dist / radius));
                    if (damage > 0)
                        living.damage(Math.max(damage, 0.5), caster);
                }

                particles.drawSphere(origin, 2.0, 60, Particle.SPELL_WITCH);
                origin.getWorld().playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.3f);
            }
        }.runTaskLater(plugin, durationTick + 1L);

        caster.sendActionBar(Component.text("✦ Gravity Pull!", NamedTextColor.LIGHT_PURPLE));
    }
}