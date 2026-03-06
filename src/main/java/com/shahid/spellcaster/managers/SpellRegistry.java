package com.shahid.spellcaster.managers;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.spells.*;

import java.util.*;

/**
 * Registry holding all available spells by name.
 * All spells are registered at plugin startup.
 */
public class SpellRegistry {

    private final Map<String, Spell> spells = new LinkedHashMap<>();

    public SpellRegistry(SpellcasterPlugin plugin) {
        registerAll(plugin);
    }

    private void registerAll(SpellcasterPlugin plugin) {
        register(new GravityPullSpell(plugin));
        register(new HomingMissileSpell(plugin));
        register(new WallOfFireSpell(plugin));
        register(new NovaBlastSpell(plugin));
    }

    private void register(Spell spell) {
        spells.put(spell.getName().toLowerCase(), spell);
    }

    /**
     * @return The spell for the given name, or null if not found.
     */
    public Spell getSpell(String name) {
        return name == null ? null : spells.get(name.toLowerCase());
    }

    /**
     * @return Unmodifiable view of all registered spells.
     */
    public Collection<Spell> getAllSpells() {
        return Collections.unmodifiableCollection(spells.values());
    }

    public List<String> getSpellNames() {
        return new ArrayList<>(spells.keySet());
    }
}
