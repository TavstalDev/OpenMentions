package io.github.tavstaldev.openMentions.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.utils.MentionUtils;
import io.github.tavstaldev.openMentions.utils.VanishUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener class for handling chat events in the OpenMentions plugin.
 * Detects mentions in chat messages and processes them according to the plugin's configuration.
 */
public class ChatListener implements Listener {
    // Logger instance for logging debug information related to the ChatListener.
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(ChatListener.class);

    // Regular expression pattern to match Minecraft usernames in chat messages.
    private final Pattern minecraftUsernamePattern = Pattern.compile("@([a-zA-Z0-9_]{3,16}$)\\b");

    /**
     * Constructor for the ChatListener class.
     * Registers the chat event listener with the Bukkit plugin manager.
     */
    public ChatListener() {
        _logger.Debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, OpenMentions.Instance);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Event handler for processing player chat messages.
     * Detects mentions in chat messages and applies mention effects if applicable.
     *
     * @param event The AsyncPlayerChatEvent triggered when a player sends a chat message.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;

        Player source = event.getPlayer();
        String rawMessage = event.getMessage();

        int mentionCount = 0;
        final int maxMentionCount = OpenMentions.Config().maxMentionsPerMessage;
        final boolean allowSelfMention = OpenMentions.Config().allowSelfMention;

        Matcher matcher = minecraftUsernamePattern.matcher(rawMessage);
        while (matcher.find() && mentionCount < maxMentionCount) {
            String mentionName = matcher.group(1);
            Player mentionedPlayer = Bukkit.getPlayerExact(mentionName);
            if (mentionedPlayer == null)
                continue;

            if (mentionedPlayer.getUniqueId() == source.getUniqueId() && !allowSelfMention)
                continue;

            if (mentionedPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR)
                continue;

            if (VanishUtil.isVanished(mentionedPlayer))
                continue;

            if (!MentionUtils.mentionPlayer(mentionedPlayer, source))
                continue;
            rawMessage = rawMessage.replaceFirst("@" + Pattern.quote(mentionName), "§e@" + mentionName + "§r");
            mentionCount++;
        }
        event.setMessage(rawMessage);
    }
}