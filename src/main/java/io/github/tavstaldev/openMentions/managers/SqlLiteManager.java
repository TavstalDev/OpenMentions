package io.github.tavstaldev.openMentions.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OMConfig;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import io.github.tavstaldev.openMentions.models.EMentionPreference;
import io.github.tavstaldev.openMentions.models.IDatabase;
import io.github.tavstaldev.openMentions.models.PlayerDatabaseData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages SQLite database operations for the OpenMentions plugin.
 * Implements the IDatabase interface to handle player data storage and retrieval.
 */
public class SqlLiteManager implements IDatabase {
    private OMConfig _config;
    private final Cache<@NotNull UUID, PlayerDatabaseData> _playerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private final Cache<@NotNull UUID, Set<UUID>> _ignoredPlayerCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * Logger instance for logging messages related to SqlLiteManager.
     */
    private static final PluginLogger _logger = OpenMentions.Logger().WithModule(SqlLiteManager.class);

    /**
     * Loads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void load() {
        _config = OpenMentions.Config();
    }

    /**
     * Unloads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void unload() {
    }

    /**
     * Creates a connection to the SQLite database.
     *
     * @return A Connection object to the SQLite database, or null if an error occurs.
     */
    public Connection CreateConnection() {
        try {
            if (_config == null)
                _config = OpenMentions.Config();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/OpenMentions/%s.db", _config.storageFilename));
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Ensures the database schema is created. Creates the players table if it does not exist.
     */
    @Override
    public void checkSchema() {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_players (" +
                            "PlayerId VARCHAR(36) PRIMARY KEY, " +
                            "Sound VARCHAR(200) NOT NULL, " +
                            "Display VARCHAR(32) NOT NULL, " +
                            "Preference VARCHAR(32) NOT NULL);",
                    _config.storageTablePrefix
            );
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();

            sql = String.format("CREATE TABLE IF NOT EXISTS %s_ignores (" +
                            "PlayerId VARCHAR(36) NOT NULL, " +
                            "IgnoredId VARCHAR(36) NOT NULL, " +
                            "PRIMARY KEY (PlayerId, IgnoredId));",
                    _config.storageTablePrefix
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Adds a new player's data to the database.
     *
     * @param playerId   The UUID of the player.
     * @param soundKey   The sound key associated with the player.
     * @param display    The display preference of the player.
     * @param preference The mention preference of the player.
     */
    @Override
    public void addData(UUID playerId, String soundKey, EMentionDisplay display, EMentionPreference preference) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("INSERT INTO %s_players (PlayerId, Sound, Display, Preference) " +
                            "VALUES (?, ?, ?, ?);",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, soundKey);
                statement.setString(3, display.name());
                statement.setString(4, preference.name());
                statement.executeUpdate();
            }

            PlayerDatabaseData data = new PlayerDatabaseData(playerId, soundKey, display, preference);
            _playerCache.put(playerId, data);
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while adding player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the sound key for a specific player in the database.
     *
     * @param playerId The UUID of the player.
     * @param soundKey The new sound key to associate with the player.
     */
    @Override
    public void updateSound(UUID playerId, String soundKey) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Sound=? WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, soundKey);
                statement.setString(2, playerId.toString());
                statement.executeUpdate();
            }

            if (_playerCache.getIfPresent(playerId) != null) {
                PlayerDatabaseData data = _playerCache.getIfPresent(playerId);
                if (data == null)
                    return;
                data.soundName = soundKey;
                _playerCache.put(playerId, data);
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the display preference for a specific player in the database.
     *
     * @param playerId The UUID of the player.
     * @param display  The new display preference to associate with the player.
     */
    @Override
    public void updateDisplay(UUID playerId, EMentionDisplay display) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Display=? WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, display.name());
                statement.setString(2, playerId.toString());
                statement.executeUpdate();
            }

            if (_playerCache.getIfPresent(playerId) != null) {
                PlayerDatabaseData data = _playerCache.getIfPresent(playerId);
                if (data == null)
                    return;
                data.display = display;
                _playerCache.put(playerId, data);
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the mention preference for a specific player in the database.
     *
     * @param playerId   The UUID of the player.
     * @param preference The new mention preference to associate with the player.
     */
    @Override
    public void updatePreference(UUID playerId, EMentionPreference preference) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Preference=? WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, preference.name());
                statement.setString(2, playerId.toString());
                statement.executeUpdate();
            }

            if (_playerCache.getIfPresent(playerId) != null) {
                PlayerDatabaseData data = _playerCache.getIfPresent(playerId);
                if (data == null)
                    return;
                data.preference = preference;
                _playerCache.put(playerId, data);
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates all data for a specific player in the database.
     *
     * @param playerId   The UUID of the player.
     * @param soundKey   The new sound key to associate with the player.
     * @param display    The new display preference to associate with the player.
     * @param preference The new mention preference to associate with the player.
     */
    @Override
    public void updateData(UUID playerId, String soundKey, EMentionDisplay display, EMentionPreference preference) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("UPDATE %s_players SET Sound=?, Display=?, Preference=? WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, soundKey);
                statement.setString(2, display.name());
                statement.setString(3, preference.name());
                statement.setString(4, playerId.toString());
                statement.executeUpdate();
            }

            if (_playerCache.getIfPresent(playerId) != null) {
                PlayerDatabaseData data = _playerCache.getIfPresent(playerId);
                if (data == null)
                    return;
                data.preference = preference;
                data.display = display;
                data.soundName = soundKey;
                _playerCache.put(playerId, data);
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while updating player data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a player's data from the database.
     *
     * @param playerId The UUID of the player to remove.
     */
    @Override
    public void removeData(UUID playerId) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("DELETE FROM %s_players WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }

            if (_playerCache.getIfPresent(playerId) != null) {
                _playerCache.invalidate(playerId);
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Retrieves a specific player's data from the database.
     *
     * @param playerId The UUID of the player to retrieve.
     * @return A PlayerDatabaseData object representing the player's data, or null if not found.
     */
    @Override
    public Optional<PlayerDatabaseData> getData(UUID playerId) {
        var data = _playerCache.getIfPresent(playerId);
        if (data != null) {
            return Optional.of(data);
        }

        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_players WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new PlayerDatabaseData(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getString("Sound"),
                                EMentionDisplay.valueOf(result.getString("Display")),
                                EMentionPreference.valueOf(result.getString("Preference"))
                        );
                    }
                }
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while finding player data...\n%s", ex.getMessage()));
            return Optional.empty();
        }

        if (data != null) {
            _playerCache.put(playerId, data);
        }
        return Optional.ofNullable(data);
    }

    /**
     * Adds a player to the ignored list of another player in the database.
     *
     * @param playerId        The UUID of the player who is ignoring another player.
     * @param ignoredPlayerId The UUID of the player being ignored.
     */
    @Override
    public void addIgnoredPlayer(UUID playerId, UUID ignoredPlayerId) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("INSERT INTO %s_ignores (PlayerId, IgnoredId) " +
                            "VALUES (?, ?);",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, ignoredPlayerId.toString());
                statement.executeUpdate();
            }

            Set<UUID> ignoredSet = _ignoredPlayerCache.getIfPresent(playerId);
            if (ignoredSet != null) {
                ignoredSet.add(ignoredPlayerId);
            } else {
                _ignoredPlayerCache.put(playerId, Set.of(ignoredPlayerId));
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while adding ignore data...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a player from the ignored list of another player in the database.
     *
     * @param playerId        The UUID of the player who is removing another player from their ignored list.
     * @param ignoredPlayerId The UUID of the player being removed from the ignored list.
     */
    @Override
    public void removeIgnoredPlayer(UUID playerId, UUID ignoredPlayerId) {
        try (Connection connection = CreateConnection()) {
            String sql = String.format("DELETE FROM %s_ignores WHERE PlayerId=? AND IgnoredId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, ignoredPlayerId.toString());
                statement.executeUpdate();
            }

            Set<UUID> ignoredSet = _ignoredPlayerCache.getIfPresent(playerId);
            if (ignoredSet != null) {
                ignoredSet.remove(ignoredPlayerId);
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened during the deletion of ignore tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Checks if a player is ignored by another player.
     *
     * @param playerId        The UUID of the player who may be ignoring another player.
     * @param ignoredPlayerId The UUID of the player who may be ignored.
     * @return True if the ignoredPlayerId is in the ignored list of playerId, false otherwise.
     */
    @Override
    public boolean isPlayerIgnored(UUID playerId, UUID ignoredPlayerId) {
        var data = _ignoredPlayerCache.getIfPresent(playerId);
        if (data != null) {
            return data.contains(ignoredPlayerId);
        }

        data = new HashSet<>();
        try (Connection connection = CreateConnection()) {
            String sql = String.format("SELECT * FROM %s_ignores WHERE PlayerId=?;",
                    _config.storageTablePrefix);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data.add(UUID.fromString(result.getString("IgnoredId")));
                    }
                }
            }
        } catch (Exception ex) {
            _logger.Error(String.format("Unknown error happened while finding ignore data...\n%s", ex.getMessage()));
            return false;
        }

        _ignoredPlayerCache.put(playerId, data);
        return data.contains(ignoredPlayerId);
    }
}