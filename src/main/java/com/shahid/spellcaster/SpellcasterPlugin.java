package com.shahid.spellcaster;

import com.shahid.spellcaster.commands.SpellcasterCommand;
import com.shahid.spellcaster.commands.WandCommand;
import com.shahid.spellcaster.listeners.ProjectileHitListener;
import com.shahid.spellcaster.listeners.WandInteractListener;
import com.shahid.spellcaster.managers.CooldownManager;
import com.shahid.spellcaster.managers.SpellRegistry;
import com.shahid.spellcaster.managers.WandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SpellcasterPlugin extends JavaPlugin {

    private static SpellcasterPlugin instance;

    private WandManager wandManager;
    private SpellRegistry spellRegistry;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.cooldownManager = new CooldownManager();
        this.spellRegistry   = new SpellRegistry(this);
        this.wandManager     = new WandManager(this);

        getServer().getPluginManager().registerEvents(new WandInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);

        getCommand("wand").setExecutor(new WandCommand(this));
        getCommand("spellcaster").setExecutor(new SpellcasterCommand(this));

        getLogger().info("╔══════════════════════════════════╗");
        getLogger().info("║   RPG Spellcaster v1.0.0 ready   ║");
        getLogger().info("╚══════════════════════════════════╝");
        getLogger().info("Registered spells: " + spellRegistry.getSpellNames());
    }

    @Override
    public void onDisable() {

        getServer().getScheduler().cancelTasks(this);
        getLogger().info("RPG Spellcaster disabled. All tasks cancelled.");
    }

    public static SpellcasterPlugin getInstance() {
        return instance;
    }

    public WandManager getWandManager() {
        return wandManager;
    }

    public SpellRegistry getSpellRegistry() {
        return spellRegistry;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}