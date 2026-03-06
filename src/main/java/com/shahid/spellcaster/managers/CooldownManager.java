package com.shahid.spellcaster.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player, per-spell cooldowns using System.currentTimeMillis().
 * No persistence — resets on server restart (intentional for this plugin).
 */
public class CooldownManager {

    // Map<playerUUID, Map<spellName, expireTimeMs>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /**
     * @return true if the player is still on cooldown for the given spell.
     */
    public boolean isOnCooldown(UUID playerId, String spellName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null)
            return false;
        Long expiry = playerCooldowns.get(spellName.toLowerCase());
        if (expiry == null)
            return false;
        return System.currentTimeMillis() < expiry;
    }

    /**
     * @return Remaining cooldown seconds, or 0 if not on cooldown.
     */
    public double getRemainingSeconds(UUID playerId, String spellName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null)
            return 0;
        Long expiry = playerCooldowns.get(spellName.toLowerCase());
        if (expiry == null)
            return 0;
        long remaining = expiry - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000.0 : 0;
    }

    /**
     * Records that the player cast the spell now; expires after durationSeconds.
     */
    public void setCooldown(UUID playerId, String spellName, double durationSeconds) {
        cooldowns
                .computeIfAbsent(playerId, k -> new HashMap<>())
                .put(spellName.toLowerCase(), System.currentTimeMillis() + (long) (durationSeconds * 1000));
    }

    /**
     * Clears all cooldowns for a player (e.g. on logout / admin clear).
     */
    public void clearCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }
}
