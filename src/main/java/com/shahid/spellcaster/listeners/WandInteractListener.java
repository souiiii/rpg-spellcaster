package com.shahid.spellcaster.listeners;

import com.shahid.spellcaster.SpellcasterPlugin;
import com.shahid.spellcaster.managers.CooldownManager;
import com.shahid.spellcaster.managers.WandManager;
import com.shahid.spellcaster.spells.Spell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class WandInteractListener implements Listener {

    private final SpellcasterPlugin plugin;
    private final WandManager wandManager;
    private final CooldownManager cooldownManager;

    public WandInteractListener(SpellcasterPlugin plugin) {
        this.plugin = plugin;
        this.wandManager = plugin.getWandManager();
        this.cooldownManager = plugin.getCooldownManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!wandManager.isWand(item))
            return;

        event.setCancelled(true);

        if (!player.hasPermission("spellcaster.cast")) {
            player.sendMessage(Component.text("✘ You don't have permission to cast spells!", NamedTextColor.RED));
            return;
        }

        String spellName = wandManager.getSpellName(item);
        Spell spell = plugin.getSpellRegistry().getSpell(spellName);

        if (spell == null) {
            player.sendMessage(Component.text("✘ Unknown spell: " + spellName, NamedTextColor.RED));
            return;
        }

        double cooldownSec = plugin.getConfig().getDouble("cooldowns." + spellName,
                plugin.getConfig().getDouble("cooldowns.default", 5.0));

        if (cooldownManager.isOnCooldown(player.getUniqueId(), spellName)) {
            double remaining = cooldownManager.getRemainingSeconds(player.getUniqueId(), spellName);
            player.sendActionBar(Component.text(
                    String.format("⏳ Cooldown: %.1fs", remaining), NamedTextColor.YELLOW));
            return;
        }

        spell.cast(player, player.getLocation());
        cooldownManager.setCooldown(player.getUniqueId(), spellName, cooldownSec);
    }
}