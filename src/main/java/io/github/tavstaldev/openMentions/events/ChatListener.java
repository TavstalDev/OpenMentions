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

// TODO: Documentation
public class ChatListener implements Listener {
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(ChatListener.class);
    private final Pattern minecraftUsernamePattern = Pattern.compile("@([a-zA-Z0-9_]{3,16}$)\\b");

    public ChatListener() {
        _logger.Debug("Registering chat event listener...");
        Bukkit.getPluginManager().registerEvents(this, OpenMentions.Instance);
        _logger.Debug("Event listener registered.");
    }

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
