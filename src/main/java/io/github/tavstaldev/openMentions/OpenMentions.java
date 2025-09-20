package io.github.tavstaldev.openMentions;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.openMentions.commands.CommandMentions;
import io.github.tavstaldev.openMentions.commands.CommandsMentionsCompleter;
import io.github.tavstaldev.openMentions.events.ChatListener;
import io.github.tavstaldev.openMentions.events.PlayerListener;
import io.github.tavstaldev.openMentions.managers.CombatLogManager;
import io.github.tavstaldev.openMentions.managers.CombatManager;
import io.github.tavstaldev.openMentions.managers.MySqlManager;
import io.github.tavstaldev.openMentions.managers.SqlLiteManager;
import io.github.tavstaldev.openMentions.models.ICombatManager;
import io.github.tavstaldev.openMentions.models.IDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Main class for the OpenMentions plugin.
 * Handles initialization, configuration, and management of the plugin's features.
 */
public final class OpenMentions extends PluginBase {
    /** Singleton instance of the OpenMentions plugin. */
    public static OpenMentions Instance;

    /**
     * Retrieves the custom logger for the plugin.
     *
     * @return The plugin's custom logger.
     */
    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }

    /**
     * Retrieves the translator for the plugin.
     *
     * @return The plugin's translator.
     */
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }

    /**
     * Retrieves the plugin's configuration file.
     *
     * @return The plugin's configuration file.
     */
    public static OMConfig Config() {
        return (OMConfig) Instance.getConfig();
    }

    /** Database manager for handling player data storage. */
    public static IDatabase Database;

    /** Combat manager for handling combat-related features. */
    public static ICombatManager CombatManager;

    /**
     * Constructor for the OpenMentions plugin.
     * Initializes the plugin with its name, version, author, download URL, and supported languages.
     */
    public OpenMentions() {super("https://github.com/TavstalDev/OpenMentions/releases/latest");
    }

    /**
     * Called when the plugin is enabled.
     * Handles initialization of components, configuration, and dependencies.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _config = new OMConfig();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.Info(String.format("Loading %s...", getProjectName()));

        if (VersionUtils.isLegacy()) {
            _logger.Error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize Combat Manager
        Plugin combatLogPlugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (combatLogPlugin != null && combatLogPlugin.isEnabled()) {
            CombatManager = new CombatLogManager();
            getLogger().info("Successfully hooked into CombatLogX!");
        } else {
            CombatManager = new CombatManager();
            _logger.Warn("CombatLogX plugin not found or not enabled. Combat management features will be disabled.");
        }

        // Register Events
        new PlayerListener();
        new ChatListener();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!_translator.Load()) {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Create Database
        String databaseType = Config().storageType;
        if (databaseType == null)
            databaseType = "sqlite";
        switch (databaseType.toLowerCase()) {
            case "mysql": {
                Database = new MySqlManager();
                break;
            }
            case "sqlite":
            default: {
                Database = new SqlLiteManager();
                break;
            }
        }
        Database.load();
        Database.checkSchema();

        // Register Commands
        _logger.Debug("Registering commands...");
        var command = getCommand("mentions");
        if (command != null) {
            command.setExecutor(new CommandMentions());
            command.setTabCompleter(new CommandsMentionsCompleter());
        }

        _logger.Ok(String.format("%s has been successfully loaded.", getProjectName()));
        if (Config().checkForUpdates) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.Ok("Plugin is up to date!");
                } else {
                    _logger.Warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.Error("Failed to determine update status: " + e.getMessage());
                return null;
            });
        }
    }

    /**
     * Called when the plugin is disabled.
     * Handles cleanup and resource release.
     */
    @Override
    public void onDisable() {
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Reloads the plugin's configuration and localizations.
     */
    public void reload() {
        _logger.Info("Reloading OpenMentions...");
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this._config.load();
        _logger.Debug("Configuration reloaded.");
    }
}