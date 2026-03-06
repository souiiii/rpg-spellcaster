package com.shahid.spellcaster.commands;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.spells.Spell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * /spellcaster <list|reload>
 *
 * Admin utility command. Lists registered spells or reloads config.
 */
public class SpellcasterCommand implements CommandExecutor, TabCompleter {

    private final SpellcasterPlugin plugin;

    public SpellcasterCommand(SpellcasterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "list" -> {
                Collection<Spell> spells = plugin.getSpellRegistry().getAllSpells();
                sender.sendMessage(Component.text("══ Registered Spells (" + spells.size() + ") ══",
                        NamedTextColor.GOLD));
                int i = 1;
                for (Spell s : spells) {
                    sender.sendMessage(
                            Component.text(" " + i++ + ". ", NamedTextColor.YELLOW)
                                    .append(Component.text(s.getName(), NamedTextColor.AQUA))
                                    .append(Component.text(" — ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text(s.getDescription())));
                }
                sender.sendMessage(Component.text("Use /wand <spell> to get a wand.", NamedTextColor.GRAY));
            }

            case "reload" -> {
                if (!sender.hasPermission("spellcaster.admin")) {
                    sender.sendMessage(Component.text("✘ No permission.", NamedTextColor.RED));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(Component.text("✔ Config reloaded!", NamedTextColor.GREEN));
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("── RPG Spellcaster Commands ──", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/spellcaster list   ", NamedTextColor.AQUA)
                .append(Component.text("— List all available spells", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/spellcaster reload ", NamedTextColor.AQUA)
                .append(Component.text("— Reload config.yml (admin)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/wand <spell> [tier]", NamedTextColor.AQUA)
                .append(Component.text("— Get a wand", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("list", "reload")) {
                if (sub.startsWith(args[0].toLowerCase()))
                    completions.add(sub);
            }
        }
        return completions;
    }
}
