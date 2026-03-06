package com.shahid.spellcaster.spells;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Base interface for all spells in the RPG Spellcaster system.
 */
public interface Spell {

    /**
     * @return Internal identifier (e.g. "gravity_pull")
     */
    String getName();

    /**
     * @return Human-readable description shown in /spellcaster list
     */
    String getDescription();

    /**
     * Cast the spell from the player's position/direction.
     *
     * @param caster  The player casting the spell
     * @param origin  The location the spell originates from
     */
    void cast(Player caster, Location origin);
}
