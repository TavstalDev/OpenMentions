package io.github.tavstaldev.openMentions.managers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the caching of player data.
 * This class provides methods to add, remove, and retrieve player data
 * stored in a cache for efficient access.
 */
public class PlayerCacheManager {
    private static final Map<UUID, LocalDateTime> _cooldown = new HashMap<>();

    /**
     * Sets a cooldown time for a specific player.
     *
     * @param playerId The unique identifier of the player.
     * @param time The time until which the cooldown is active.
     */
    public static void setCooldown(UUID playerId, LocalDateTime time) {
        _cooldown.put(playerId, time);
    }

    /**
     * Checks if a specific player is currently on cooldown.
     *
     * @param playerId The unique identifier of the player.
     * @return True if the player is on cooldown, false otherwise.
     */
    public static boolean isOnCooldown(UUID playerId) {
        LocalDateTime cooldownTime = _cooldown.get(playerId);
        if (cooldownTime == null) {
            return false; // No cooldown set for this player
        }
        return LocalDateTime.now().isBefore(cooldownTime); // Check if current time is before the cooldown time
    }
}