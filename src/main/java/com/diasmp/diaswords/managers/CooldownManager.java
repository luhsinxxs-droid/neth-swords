package com.diasmp.diaswords.managers;

import com.diasmp.diaswords.SwordType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks ability cooldowns per player, per sword type.
 * Times are stored as the epoch millisecond at which the cooldown expires.
 */
public class CooldownManager {

    private final Map<UUID, Map<SwordType, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * @return true if the player's ability for this sword is currently on cooldown.
     */
    public boolean isOnCooldown(UUID playerId, SwordType type) {
        Map<SwordType, Long> playerMap = cooldowns.get(playerId);
        if (playerMap == null) return false;
        Long expiry = playerMap.get(type);
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    /**
     * @return remaining cooldown in seconds (rounded up), or 0 if not on cooldown.
     */
    public long getRemainingSeconds(UUID playerId, SwordType type) {
        Map<SwordType, Long> playerMap = cooldowns.get(playerId);
        if (playerMap == null) return 0L;
        Long expiry = playerMap.get(type);
        if (expiry == null) return 0L;
        long remainingMs = expiry - System.currentTimeMillis();
        if (remainingMs <= 0) return 0L;
        return (remainingMs + 999) / 1000; // round up
    }

    public void setCooldown(UUID playerId, SwordType type, long seconds) {
        long expiry = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(type, expiry);
    }

    public void clear(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public void clearAll() {
        cooldowns.clear();
    }
}
