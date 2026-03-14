package com.shahid.spellcaster.managers;

import com.shahid.spellcaster.SpellcasterPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class WandManager {

    private final NamespacedKey keyWandId;
    private final NamespacedKey keySpellName;
    private final NamespacedKey keyWandTier;

    private final SpellcasterPlugin plugin;

    public WandManager(SpellcasterPlugin plugin) {
        this.plugin = plugin;
        this.keyWandId = new NamespacedKey(plugin, "wand_id");
        this.keySpellName = new NamespacedKey(plugin, "spell_name");
        this.keyWandTier = new NamespacedKey(plugin, "wand_tier");
    }

    public ItemStack createWand(String spellName, int tier) {
        Material mat = tierMaterial(tier);
        ItemStack wand = new ItemStack(mat);
        ItemMeta meta = wand.getItemMeta();

        String displaySpell = formatSpellName(spellName);
        Component name = Component.text("✦ ", NamedTextColor.YELLOW)
                .append(Component.text(displaySpell + " Wand", tierColor(tier))
                        .decoration(TextDecoration.BOLD, true)
                        .decoration(TextDecoration.ITALIC, false));
        meta.displayName(name);

        meta.lore(List.of(
                Component.empty(),
                Component.text("  Spell: ", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(displaySpell, NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false)),
                Component.text("  Tier: ", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(tierLabel(tier), tierColor(tier))
                                .decoration(TextDecoration.ITALIC, false)),
                Component.empty(),
                Component.text("  Right-click to cast!", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, true)));

        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyWandId, PersistentDataType.STRING, UUID.randomUUID().toString());
        pdc.set(keySpellName, PersistentDataType.STRING, spellName.toLowerCase());
        pdc.set(keyWandTier, PersistentDataType.INTEGER, tier);

        wand.setItemMeta(meta);
        return wand;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(keyWandId, PersistentDataType.STRING);
    }

    public String getSpellName(ItemStack item) {
        if (!isWand(item))
            return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(keySpellName, PersistentDataType.STRING);
    }

    public int getWandTier(ItemStack item) {
        if (!isWand(item))
            return 1;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Integer tier = pdc.get(keyWandTier, PersistentDataType.INTEGER);
        return tier != null ? tier : 1;
    }

    private Material tierMaterial(int tier) {
        return switch (tier) {
            case 2 -> Material.BLAZE_ROD;
            case 3 -> Material.NETHER_STAR;
            default -> Material.STICK;
        };
    }

    private NamedTextColor tierColor(int tier) {
        return switch (tier) {
            case 2 -> NamedTextColor.GOLD;
            case 3 -> NamedTextColor.LIGHT_PURPLE;
            default -> NamedTextColor.GREEN;
        };
    }

    private String tierLabel(int tier) {
        return switch (tier) {
            case 2 -> "★★☆ Rare";
            case 3 -> "★★★ Legendary";
            default -> "★☆☆ Common";
        };
    }

    private String formatSpellName(String raw) {
        String[] parts = raw.split("[_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1)
                    sb.append(part.substring(1).toLowerCase());
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}