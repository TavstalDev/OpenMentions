package io.github.tavstaldev.openMentions.utils;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Optional;

/**
 * Utility class for handling player vanish status.
 * Provides methods to check if a player is vanished.
 */
public class VanishUtil {

    /**
     * Checks if a player is vanished.
     *
     * @param player The player to check.
     * @return True if the player is vanished, false otherwise.
     */
    public static boolean isVanished(Player player) {
        Optional<MetadataValue> vanishedMeta = player.getMetadata("vanished").stream()
                .filter(MetadataValue::asBoolean)
                .findFirst();
        return vanishedMeta.isPresent();
    }
}
