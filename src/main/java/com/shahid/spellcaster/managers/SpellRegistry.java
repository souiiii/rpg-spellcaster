package com.shahid.spellcaster.managers;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.spells.*;

import java.util.*;

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

    public Spell getSpell(String name) {
        return name == null ? null : spells.get(name.toLowerCase());
    }

    public Collection<Spell> getAllSpells() {
        return Collections.unmodifiableCollection(spells.values());
    }

    public List<String> getSpellNames() {
        return new ArrayList<>(spells.keySet());
    }
}