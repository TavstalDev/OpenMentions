package io.github.tavstaldev.openMentions.utils;

import com.cryptomorin.xseries.XSound;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.managers.PlayerCacheManager;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for handling player mentions in the OpenMentions plugin.
 * Provides methods for formatting mentions and notifying players.
 */
public class MentionUtils {
    /** Logger instance for logging messages related to MentionUtils. */
    private static final PluginLogger _logger = OpenMentions.Logger().WithModule(MentionUtils.class);

    /**
     * Handles the mention of a player by another player.
     * Determines the player's mention preferences and sends the appropriate notification.
     *
     * @param player The player being mentioned.
     * @param mentioner The player who mentioned the target player.
     */
    public static boolean mentionPlayer(@NotNull Player player, Player mentioner) {
        var playerId = player.getUniqueId();
        var dataOpt = OpenMentions.Database.getData(playerId);
        if (dataOpt.isEmpty()) {
            _logger.Error("Player data not found for " + player.getName());
            return false;
        }

        var mentionerId = mentioner.getUniqueId();
        if (OpenMentions.Database.isPlayerIgnored(playerId, mentionerId))
            return true; // Player has ignored the mentioner, return true so the mentioner will not know that they are ignored

        if (PlayerCacheManager.isOnCooldown(mentionerId))
            return false; // Do not notify

        var data = dataOpt.get();
        switch (data.preference)
        {
            case ALWAYS: {
                sendMention(player, data.soundName, data.display, false, mentioner);
                break;
            }
            case SILENT_IN_COMBAT: {
                sendMention(player, data.soundName, data.display, OpenMentions.CombatManager.isPlayerInCombat(player), mentioner);
                break;
            }
            case NEVER_IN_COMBAT: {
                if (OpenMentions.CombatManager.isPlayerInCombat(player))
                    break; // Player is in combat, do not mention
                sendMention(player, data.soundName, data.display, false, mentioner);
                break;
            }
            case NEVER: {
                // Do nothing, player has disabled mentions
                break;
            }
        }

        var cooldownTime = OpenMentions.Config().mentionCooldown;
        if (cooldownTime < 1)
            return true;

        PlayerCacheManager.setCooldown(mentionerId, LocalDateTime.now().plusSeconds(cooldownTime));
        return true;
    }

    /**
     * Sends a mention notification to a player.
     * The notification can include chat messages, action bar messages, and sounds based on the player's preferences.
     *
     * @param player The player to notify.
     * @param soundKey The key of the sound to play.
     * @param display The display type for the mention notification.
     * @param isSilent Whether the notification should be silent (no sound).
     * @param mentioner The player who mentioned the target player.
     */
    private static void sendMention(Player player, String soundKey, EMentionDisplay display, boolean isSilent, Player mentioner) {
        String actionBarMessage = OpenMentions.Instance.getTranslator().Localize(player, "General.ActionBarMessage", Map.of("player", mentioner.getName()));
        float volume = (float)OpenMentions.Config().volume;
        float pitch = (float)OpenMentions.Config().pitch;
        XSound sound;
        Optional<XSound> soundResult = SoundUtils.getSound(soundKey);
        // Fallback sound if not found
        sound = soundResult.orElse(XSound.ENTITY_PLAYER_LEVELUP);

        switch (display) {
            case ALL: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
            case ONLY_CHAT: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                break;
            }
            case ONLY_SOUND: {
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
            case ONLY_ACTIONBAR: {
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                break;
            }
            case CHAT_AND_SOUND: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
            case CHAT_AND_ACTIONBAR: {
                OpenMentions.Instance.sendLocalizedMsg(player, "General.ChatMessage", Map.of("player", mentioner.getName()));
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                break;
            }
            case ACTIONBAR_AND_SOUND: {
                player.sendActionBar(ChatUtils.translateColors(actionBarMessage, true));
                if (!isSilent)
                    sound.play(player, volume, pitch);
                break;
            }
        }
    }
}