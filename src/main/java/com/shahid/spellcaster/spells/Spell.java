package com.shahid.spellcaster.spells;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Spell {

    String getName();

    String getDescription();

    void cast(Player caster, Location origin);
}