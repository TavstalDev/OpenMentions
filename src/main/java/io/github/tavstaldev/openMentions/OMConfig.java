package io.github.tavstaldev.openMentions;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class OMConfig extends ConfigurationBase {

    public OMConfig() {
        super(OpenMentions.Instance, "config.yml", null);
    }

    // General
    public boolean checkForUpdates, debug;

    // Storage
    public String storageType, storageFilename, storageHost, storageDatabase, storageUsername, storagePassword, storageTablePrefix;
    public int storagePort;

    // Settings
    public String defaultDisplay, defaultPreference, defaultSound;
    public double volume, pitch;
    public int mentionCooldown, maxMentionsPerMessage;
    public boolean allowSelfMention;

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "eng");
        resolve("usePlayerLocale", true);
        checkForUpdates = resolveGet("updateChecker", true);
        debug = resolveGet("debug", false);
        resolve("prefix", "&3Open&bMentions &8»");

        // Dates
        resolve("dates.daily-refresh", LocalDate.now().plusDays(1).atStartOfDay().toString());
        resolve("dates.weekly-refresh", LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay().toString());

        // Storage
        storageType = resolveGet("storage.type", "sqlite");
        storageFilename = resolveGet("storage.filename", "database");
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "openmentions");

        // Settings
        defaultDisplay = resolveGet("settings.defaultDisplay", "ALL");
        defaultPreference = resolveGet("settings.defaultPreference", "ALWAYS");
        defaultSound = resolveGet("settings.defaultSound", "ENTITY_PLAYER_LEVELUP");
        volume = resolveGet("settings.volume", 1.0);
        pitch = resolveGet("settings.pitch", 1.0);
        mentionCooldown = resolveGet("settings.mentionCooldown", 3);
        maxMentionsPerMessage = resolveGet("settings.maxMentionsPerMessage", 3);
        allowSelfMention = resolveGet("settings.allowSelfMention", true);
    }
}
