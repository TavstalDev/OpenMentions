package io.github.tavstaldev.openMentions.managers;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages the caching of player data.
 * This class provides methods to add, remove, and retrieve player data
 * stored in a cache for efficient access.
 */
public class PlayerCacheManager {
    private static final Map<UUID, LocalDateTime> _cooldown = new HashMap<>();
    private static final Set<UUID> _markedForRemoval = new HashSet<>();

    /**
     * Sets a cooldown time for a specific player.
     *
     * @param playerId The unique identifier of the player.
     * @param time The time until which the cooldown is active.
     */
    public static void setCooldown(UUID playerId, LocalDateTime time) {
        _cooldown.put(playerId, time);
    }

    public static void removeCooldown(UUID playerId) {
        _cooldown.remove(playerId);
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

    /**
     * Marks a player for removal by adding their UUID to the removal set.
     *
     * @param playerId The UUID of the player to mark for removal.
     */
    public static void markForRemoval(UUID playerId) {
        _markedForRemoval.add(playerId);
    }

    /**
     * Unmarks a player for removal by removing their UUID from the removal set.
     *
     * @param playerId The UUID of the player to unmark for removal.
     */
    public static void unmarkForRemoval(UUID playerId) {
        _markedForRemoval.remove(playerId);
    }

    /**
     * Checks if a player is marked for removal.
     *
     * @param playerId The UUID of the player to check.
     * @return true if the player is marked for removal, false otherwise.
     */
    public static boolean isMarkedForRemoval(UUID playerId) {
        return _markedForRemoval.contains(playerId);
    }

    /**
     * Checks if the set of players marked for removal is empty.
     *
     * @return true if no players are marked for removal, false otherwise.
     */
    public static boolean isMarkedForRemovalEmpty() {
        return _markedForRemoval.isEmpty();
    }

    /**
     * Retrieves the set of UUIDs representing players marked for removal.
     *
     * @return A Set of UUIDs of players marked for removal.
     */
    public static Set<UUID> getMarkedForRemovalSet() {
        return new HashSet<>(_markedForRemoval); // Return a copy to prevent external modification
    }
}