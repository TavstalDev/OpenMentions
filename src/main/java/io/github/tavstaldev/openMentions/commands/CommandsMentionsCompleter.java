package io.github.tavstaldev.openMentions.commands;

import com.cryptomorin.xseries.XSound;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openMentions.OpenMentions;
import io.github.tavstaldev.openMentions.models.EMentionDisplay;
import io.github.tavstaldev.openMentions.models.EMentionPreference;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides tab completion for the "mentions" command.
 * Implements the TabCompleter interface to dynamically suggest command arguments.
 */
public class CommandsMentionsCompleter implements TabCompleter {
    // Logger instance for logging messages related to this class.
    private final PluginLogger _logger = OpenMentions.Logger().WithModule(CommandsMentionsCompleter.class);
    private final Collection<XSound> _sounds = XSound.getValues();

    /**
     * Handles tab completion for the "mentions" command.
     *
     * @param sender The command sender (e.g., player or console).
     * @param command The command being executed.
     * @param alias The alias used for the command.
     * @param args The arguments provided so far.
     * @return A list of possible completions for the current argument, or null if no completions are available.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        try {
            // Return null if the sender is the console, as tab completion is not supported.
            if (sender instanceof ConsoleCommandSender) {
                return new ArrayList<>();
            }
            Player player = (Player) sender;
            List<String> commandList = new ArrayList<>();

            // Handle tab completion based on the number of arguments provided.
            switch (args.length) {
                case 0:
                case 1: {
                    // Suggest subcommands based on the player's permissions.
                    for (var subCommand : CommandMentions.SubCommands) {
                        if (subCommand.permission == null || subCommand.permission.isEmpty()) {
                            commandList.add(subCommand.command);
                        }
                        else if (player.hasPermission(subCommand.permission)) {
                            commandList.add(subCommand.command);
                        }
                    }

                    // Filter suggestions based on the current input.
                    commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[0].toLowerCase()));
                    break;
                }
                case 2: {
                    // Provide specific suggestions for subcommands with additional arguments.
                    switch (args[0].toLowerCase()) {
                        case "sound": {
                            // Suggest available sound events.
                            try {
                                for (var sound : _sounds) {
                                    commandList.add(sound.name());
                                }
                            } catch (Exception ex) {
                                _logger.Error("Failed to retrieve sound events for tab completion.");
                                _logger.Error(ex);
                            }
                            break;
                        }
                        case "display": {
                            // Suggest available display options.
                            for (var value : EMentionDisplay.values()) {
                                commandList.add(value.name());
                            }
                            break;
                        }
                        case "preference": {
                            // Suggest available preference options.
                            for (var value : EMentionPreference.values()) {
                                commandList.add(value.name());
                            }
                            break;
                        }
                    }
                    // Filter suggestions based on the current input.
                    commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[1].toLowerCase()));
                    break;
                }
            }

            // Sort the suggestions alphabetically.
            Collections.sort(commandList);
            return commandList;
        }
        catch (Exception ex) {
            // Log any errors that occur during tab completion.
            _logger.Error("An error occurred while trying to tab complete the 'mentions' command.");
            _logger.Error(ex.getMessage());
            return new ArrayList<>();
        }
    }
}