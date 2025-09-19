package io.github.tavstaldev.openMentions.utils;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Optional;

public class VanishUtil {
    public static boolean isVanished(Player player) {
        Optional<MetadataValue> vanishedMeta = player.getMetadata("vanished").stream()
                .filter(MetadataValue::asBoolean)
                .findFirst();
        return vanishedMeta.isPresent();
    }
}
