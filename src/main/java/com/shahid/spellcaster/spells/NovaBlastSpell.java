package com.shahid.spellcaster.spells;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.particles.ParticleEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class NovaBlastSpell implements Spell {

    private static final String NAME = "nova_blast";

    private final SpellcasterPlugin plugin;
    private final ParticleEngine particles;

    public NovaBlastSpell(SpellcasterPlugin plugin) {
        this.plugin = plugin;
        this.particles = new ParticleEngine(plugin);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "§eDetonates a massive radial shockwave, blasting all enemies outward.";
    }

    @Override
    public void cast(Player caster, Location origin) {
        double radius = plugin.getConfig().getDouble("nova_blast.radius", 8.0);
        double maxDamage = plugin.getConfig().getDouble("nova_blast.max-damage", 12.0);
        double knockback = plugin.getConfig().getDouble("nova_blast.knockback-force", 2.0);
        int sphereCount = plugin.getConfig().getInt("nova_blast.sphere-particle-count", 200);
        int ringCount = plugin.getConfig().getInt("nova_blast.inner-ring-count", 60);

        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
        origin.getWorld().playSound(origin, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
        origin.getWorld().playSound(origin, Sound.BLOCK_BEACON_POWER_SELECT, 1.2f, 0.4f);

        origin.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, origin, 3, 1, 0.5, 1, 0);

        particles.drawSphere(origin, radius * 0.5, sphereCount, Particle.END_ROD);
        particles.drawSphere(origin, radius * 0.25, sphereCount / 2, Particle.EXPLOSION_NORMAL);
        particles.drawRing(origin, 0.1, radius, ringCount, Particle.SWEEP_ATTACK);

        particles.animateExpandingRing(
                origin.clone().add(0, 0.1, 0),
                radius, 20, Particle.CRIT_MAGIC);

        Collection<Entity> nearby = origin.getWorld()
                .getNearbyEntities(origin, radius, radius, radius);

        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity living))
                continue;
            if (entity.equals(caster))
                continue;

            Location entityLoc = living.getLocation();
            double dist = entityLoc.distance(origin);

            if (dist > radius)
                continue;

            Vector outward = entityLoc.toVector()
                    .subtract(origin.toVector());

            if (outward.lengthSquared() < 0.001) {
                outward = new Vector(0, 1, 0);
            } else {
                outward.normalize();
            }

            outward.setY(Math.max(outward.getY(), 0.4));
            outward.multiply(knockback * (1.0 - dist / radius));

            living.setVelocity(outward);

            double damage = maxDamage * (1.0 - (dist / radius));
            if (damage > 0.5)
                living.damage(damage, caster);

            if (dist < radius * 0.4) {
                living.setFireTicks(40);
            }
        }

        particles.drawCircle(origin, 0.5, 24, Particle.FLAME);

        caster.sendActionBar(Component.text("✦ NOVA BLAST!", NamedTextColor.YELLOW));
    }
}