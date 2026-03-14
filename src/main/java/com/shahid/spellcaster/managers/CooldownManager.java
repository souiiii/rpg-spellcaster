package com.shahid.spellcaster.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(UUID playerId, String spellName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null)
            return false;
        Long expiry = playerCooldowns.get(spellName.toLowerCase());
        if (expiry == null)
            return false;
        return System.currentTimeMillis() < expiry;
    }

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

    public void setCooldown(UUID playerId, String spellName, double durationSeconds) {
        cooldowns
                .computeIfAbsent(playerId, k -> new HashMap<>())
                .put(spellName.toLowerCase(), System.currentTimeMillis() + (long) (durationSeconds * 1000));
    }

    public void clearCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }
}