package io.github.tavstaldev.openMentions.commands;

import com.cryptomorin.xseries.XSound;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import io.github.tavstaldev.openMentions.models.EMentionPreference;
import io.github.tavstaldev.openMentions.utils.SoundUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CommandMentions class implements the CommandExecutor interface to handle the
 * "/mentions" command in the OpenMentions plugin. This command provides various
 * subcommands for managing plugin settings and preferences.
 */
public class CommandMentions implements CommandExecutor {
    /** Logger instance for logging messages related to CommandMentions. */
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(CommandMentions.class);

    /**
     * Handles the execution of the "/mentions" command.
     *
     * @param sender  The sender of the command (can be a player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.Info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("openmentions.commands.mentions")) {
            OpenMentions.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    if (!player.hasPermission("OpenMentions.commands.version")) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Version.Current", Map.of("version", OpenMentions.Instance.getVersion()));

                    OpenMentions.Instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate) {
                            OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Version.UpToDate");
                        } else {
                            OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Version.Outdated", Map.of("link", OpenMentions.Instance.getDownloadUrl()));
                        }
                    }).exceptionally(e -> {
                        _logger.Error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("OpenMentions.commands.reload")) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    OpenMentions.Instance.reload();
                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Reload.Done");
                    return true;
                }
                case "sound": {
                    if (args.length < 2) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Sound.Usage");
                        return true;
                    }

                    Optional<XSound> sound = SoundUtils.getSound(args[1]);
                    if (sound.isEmpty()) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Sound.Invalid", Map.of("value", args[1]));
                        return true;
                    }

                    var playerId = player.getUniqueId();
                    String soundName = sound.get().name();
                    OpenMentions.Database.updateSound(playerId, soundName);
                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Sound.Set", Map.of(
                            "value", soundName
                    ));
                    return true;
                }
                case "display": {
                    if (args.length < 2) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Display.Usage");
                        return true;
                    }

                    EMentionDisplay display;
                    try {
                        display = EMentionDisplay.valueOf(args[1]);
                    } catch (Exception ignored) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Display.Invalid", Map.of("value", args[1]));
                        return true;
                    }

                    var playerId = player.getUniqueId();
                    OpenMentions.Database.updateDisplay(playerId, display);
                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Display.Set", Map.of(
                            "value", display.toString()
                    ));
                    return true;
                }
                case "preference": {
                    if (args.length < 2) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Preference.Usage");
                        return true;
                    }

                    EMentionPreference preference;
                    try {
                        preference = EMentionPreference.valueOf(args[1]);
                    } catch (Exception ignored) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Preference.Invalid", Map.of("value", args[1]));
                        return true;
                    }

                    var playerId = player.getUniqueId();
                    OpenMentions.Database.updatePreference(playerId, preference);
                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Preference.Set", Map.of(
                            "value", preference.toString()
                    ));
                    return true;
                }
                case "ignore": {
                    if (args.length < 2) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Ignore.Usage");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || !target.hasPlayedBefore()) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", Map.of("player", args[1]));
                        return true;
                    }

                    var playerId = player.getUniqueId();
                    var targetId = target.getUniqueId();

                    if (playerId == targetId)
                    {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Ignore.Self");
                        return true;
                    }

                    if (OpenMentions.Database.isPlayerIgnored(playerId, targetId))
                    {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Ignore.AlreadyEnabled", Map.of("player", args[1]));
                        return true;
                    }

                    OpenMentions.Database.addIgnoredPlayer(playerId, targetId);
                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Ignore.Enabled", Map.of("player", args[1]));
                    return true;
                }
                case "unignore": {
                    if (args.length < 2) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Unignore.Usage");
                        return true;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    if (target == null || !target.hasPlayedBefore()) {
                        OpenMentions.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", Map.of("player", args[1]));
                        return true;
                    }

                    var playerId = player.getUniqueId();
                    var targetId = target.getUniqueId();

                    if (playerId == targetId)
                    {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Unignore.Self");
                        return true;
                    }

                    if (!OpenMentions.Database.isPlayerIgnored(playerId, targetId))
                    {
                        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Unignore.AlreadyDisabled", Map.of("player", args[1]));
                        return true;
                    }

                    OpenMentions.Database.removeIgnoredPlayer(playerId, targetId);
                    OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Unignore.Disabled", Map.of("player", args[1]));
                    return true;
                }
            }

            help(player, 1);
            return true;
        }
        help(player, 1);
        return true;
    }

    /**
     * List of subcommands available for the "/mentions" command.
     */
    public static final List<SubCommandData> SubCommands = List.of(
            // HELP
            new SubCommandData("help", "", Map.of(
                    "syntax", "",
                    "description", "Commands.Help.Desc"
            )),
            // VERSION
            new SubCommandData("version", "openmentions.commands.version", Map.of(
                    "syntax", "",
                    "description", "Commands.Version.Desc"
            )),
            // RELOAD
            new SubCommandData("reload", "openmentions.commands.reload", Map.of(
                    "syntax", "",
                    "description", "Commands.Reload.Desc"
            )),
            // SET SOUND
            new SubCommandData("sound", "", Map.of(
                    "syntax", "Commands.Sound.Syntax",
                    "description", "Commands.Sound.Desc"
            )),
            // SET DISPLAY
            new SubCommandData("display", "", Map.of(
                    "syntax", "Commands.Display.Syntax",
                    "description", "Commands.Display.Desc"
            )),
            // SET PREFERENCE
            new SubCommandData("preference", "", Map.of(
                    "syntax", "Commands.Preference.Syntax",
                    "description", "Commands.Preference.Desc"
            )),
            // IGNORE
            new SubCommandData("ignore", "", Map.of(
                    "syntax", "Commands.Ignore.Syntax",
                    "description", "Commands.Ignore.Desc"
            )),
            // UNIGNORE
            new SubCommandData("unignore", "", Map.of(
                    "syntax", "Commands.Unignore.Syntax",
                    "description", "Commands.Unignore.Desc"
            ))
    );

    /**
     * Displays the help menu for the "/mentions" command.
     *
     * @param player The player requesting the help menu.
     * @param page   The page number of the help menu to display.
     */
    private void help(Player player, int page) {
        int maxPage = 1 + (SubCommands.size() / 15);

        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Help.Title", new HashMap<>() {{
            put("current_page", finalPage);
            put("max_page", maxPage);
        }});
        OpenMentions.Instance.sendLocalizedMsg(player, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= SubCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = SubCommands.get(index);
            if (!subCommand.hasPermission(player)) {
                i--;
                continue;
            }

            subCommand.send(OpenMentions.Instance, player);
        }

        // Bottom message
        String previousBtn = OpenMentions.Instance.Localize(player, "Commands.Help.PrevBtn");
        String nextBtn = OpenMentions.Instance.Localize(player, "Commands.Help.NextBtn");
        String bottomMsg = OpenMentions.Instance.Localize(player, "Commands.Help.Bottom")
                .replace("%current_page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage));

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true).clickEvent(ClickEvent.runCommand("/mentions help " + (page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true).clickEvent(ClickEvent.runCommand("/mentions help " + (page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        player.sendMessage(bottomComp);
    }
}