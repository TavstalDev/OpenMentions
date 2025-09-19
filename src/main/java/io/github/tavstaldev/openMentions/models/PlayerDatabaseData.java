package io.github.tavstaldev.openMentions.models;

import java.util.UUID;

/**
 * Represents the database data for a player.
 * This class stores information about a player's preferences and settings
 * for mention notifications.
 */
public class PlayerDatabaseData {
    /** The unique identifier of the player. */
    public UUID playerId;

    /** The name of the sound associated with the player's mention notifications. */
    public String soundName;

    /** The display option for the player's mention notifications. */
    public EMentionDisplay display;

    /** The preference for receiving mention notifications. */
    public EMentionPreference preference;

    /**
     * Constructs a new PlayerDatabaseData instance with the specified parameters.
     *
     * @param playerId The unique identifier of the player.
     * @param soundName The name of the sound associated with the player's mention notifications.
     * @param display The display option for the player's mention notifications.
     * @param preference The preference for receiving mention notifications.
     */
    public PlayerDatabaseData(UUID playerId, String soundName, EMentionDisplay display, EMentionPreference preference) {
        this.playerId = playerId;
        this.soundName = soundName;
        this.display = display;
        this.preference = preference;
    }
}