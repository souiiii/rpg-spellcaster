package com.shahid.spellcaster.commands;

import com.shahid.spellcaster.SpellcasterPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WandCommand implements CommandExecutor, TabCompleter {

    private final SpellcasterPlugin plugin;

    public WandCommand(SpellcasterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /wand <spell> [tier 1-3]", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Spells: " + plugin.getSpellRegistry().getSpellNames(),
                    NamedTextColor.GRAY));
            return true;
        }

        String spellName = args[0].toLowerCase();

        if (plugin.getSpellRegistry().getSpell(spellName) == null) {
            player.sendMessage(Component.text("✘ Unknown spell: §e" + spellName, NamedTextColor.RED));
            player.sendMessage(Component.text("Available: " + plugin.getSpellRegistry().getSpellNames(),
                    NamedTextColor.GRAY));
            return true;
        }

        int tier = 1;
        if (args.length >= 2) {
            try {
                tier = Integer.parseInt(args[1]);
                tier = Math.max(1, Math.min(3, tier));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("✘ Tier must be a number (1-3).", NamedTextColor.RED));
                return true;
            }
        }

        ItemStack wand = plugin.getWandManager().createWand(spellName, tier);
        player.getInventory().addItem(wand);
        player.sendMessage(Component.text("✦ You received a ", NamedTextColor.GREEN)
                .append(Component.text(spellName.replace("_", " "), NamedTextColor.AQUA))
                .append(Component.text(" Wand (Tier " + tier + ")!", NamedTextColor.GREEN)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String name : plugin.getSpellRegistry().getSpellNames()) {
                if (name.startsWith(partial))
                    completions.add(name);
            }
        } else if (args.length == 2) {
            completions.addAll(List.of("1", "2", "3"));
        }
        return completions;
    }
}