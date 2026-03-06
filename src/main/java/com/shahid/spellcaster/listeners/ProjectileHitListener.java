package com.shahid.spellcaster.listeners;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.spells.HomingMissileSpell;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

/**
 * Listener for projectile impact events.
 * Triggers detonation when a Homing Missile (tagged Snowball) hits something.
 */
public class ProjectileHitListener implements Listener {

    private final SpellcasterPlugin plugin;

    public ProjectileHitListener(SpellcasterPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();

        // Only care about Snowballs with our homing missile metadata
        if (!(proj instanceof Snowball snowball))
            return;
        if (!snowball.hasMetadata(HomingMissileSpell.METADATA_KEY))
            return;

        // ── Look up the original shooter ───────────────────────────────────────
        List<MetadataValue> meta = snowball.getMetadata(HomingMissileSpell.METADATA_KEY);
        Player shooter = null;
        if (!meta.isEmpty()) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(meta.get(0).asString());
                shooter = plugin.getServer().getPlayer(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // ── Trigger detonation ────────────────────────────────────────────────
        HomingMissileSpell missileSpell = (HomingMissileSpell) plugin.getSpellRegistry().getSpell("homing_missile");

        if (missileSpell != null) {
            missileSpell.detonate(snowball.getLocation(), shooter);
        }

        // Remove the projectile entity
        snowball.remove();
    }
}
