package io.github.tavstaldev.openMentions.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import io.github.tavstaldev.openMentions.models.EMentionPreference;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener class for handling player-related events in the OpenMentions plugin.
 * Includes player join, quit, and chat events.
 */
public class PlayerListener implements Listener {
    /** Logger instance for logging messages related to EventListener. */
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(PlayerListener.class);

    /**
     * Initializes and registers the event listener with the Bukkit plugin manager.
     */
    public PlayerListener() {
        _logger.Debug("Registering player event listener...");
        Bukkit.getPluginManager().registerEvents(this, OpenMentions.Instance);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Handles the PlayerJoinEvent.
     * Loads or creates player data and adds it to the PlayerCacheManager.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var config = OpenMentions.Config();
        Player player = event.getPlayer();
        var playerId = player.getUniqueId();
        var playerOptData = OpenMentions.Database.getData(playerId);
        if (playerOptData.isEmpty()) {
            var defaultSoundKey = config.defaultSound;
            var defaultDisplay = EMentionDisplay.valueOf(config.defaultDisplay);
            var defaultPreference = EMentionPreference.valueOf(config.defaultPreference);
            OpenMentions.Database.addData(playerId, defaultSoundKey, defaultDisplay, defaultPreference);
        }
    }
}