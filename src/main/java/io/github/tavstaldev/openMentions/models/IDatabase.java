package io.github.tavstaldev.openMentions.models;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface representing the database operations for managing player data.
 * Provides methods for loading, updating, and retrieving player-related information.
 */
public interface IDatabase {

    /**
     * Loads the database and initializes any required resources.
     */
    void load();

    /**
     * Unloads the database and releases any allocated resources.
     */
    void unload();

    /**
     * Checks and ensures the database schema is up-to-date.
     */
    void checkSchema();

    /**
     * Adds a new player's data to the database.
     *
     * @param playerId The unique identifier of the player.
     * @param soundKey The sound key associated with the player.
     * @param display The display option for the player's mention notifications.
     * @param preference The preference for receiving mention notifications.
     */
    void addData(UUID playerId, String soundKey, EMentionDisplay display, EMentionPreference preference);

    /**
     * Updates the sound key for a specific player in the database.
     *
     * @param playerId The unique identifier of the player.
     * @param soundKey The new sound key to be associated with the player.
     */
    void updateSound(UUID playerId, String soundKey);

    /**
     * Updates the display option for a specific player in the database.
     *
     * @param playerId The unique identifier of the player.
     * @param display The new display option for the player's mention notifications.
     */
    void updateDisplay(UUID playerId, EMentionDisplay display);

    /**
     * Updates the mention preference for a specific player in the database.
     *
     * @param playerId The unique identifier of the player.
     * @param preference The new preference for receiving mention notifications.
     */
    void updatePreference(UUID playerId, EMentionPreference preference);

    /**
     * Updates all data for a specific player in the database.
     *
     * @param playerId The unique identifier of the player.
     * @param soundKey The new sound key to be associated with the player.
     * @param display The new display option for the player's mention notifications.
     * @param preference The new preference for receiving mention notifications.
     */
    void updateData(UUID playerId, String soundKey, EMentionDisplay display, EMentionPreference preference);

    /**
     * Removes a player's data from the database.
     *
     * @param playerId The unique identifier of the player.
     */
    void removeData(UUID playerId);

    /**
     * Retrieves the data for a specific player from the database.
     *
     * @param playerId The unique identifier of the player.
     * @return The player's data, or null if no data is found.
     */
    Optional<PlayerDatabaseData> getData(UUID playerId);

    void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    void removeIgnoredPlayer(UUID playerId, UUID ignoredPlayerId);

    boolean isPlayerIgnored(UUID playerId, UUID ignoredPlayerId);
}