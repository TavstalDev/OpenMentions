# OpenMentions

![Release (latest by date)](https://img.shields.io/github/v/release/TavstalDev/OpenMentions?style=plastic-square)
![Workflow Status](https://img.shields.io/github/actions/workflow/status/TavstalDev/OpenMentions/ghrelease.yml?branch=stable&label=build&style=plastic-square)
![License](https://img.shields.io/github/license/TavstalDev/OpenMentions?style=plastic-square)
![Downloads](https://img.shields.io/github/downloads/TavstalDev/OpenMentions/total?style=plastic-square)
![Issues](https://img.shields.io/github/issues/TavstalDev/OpenMentions?style=plastic-square)

## Description

**OpenMentions** is a Paper plugin that notifies players when they are mentioned in chat. 
It supports customizable notification formats, sounds, and preferences, making it easy for players to manage how they receive mention alerts.

## Features

- Mention players in chat using symbols (e.g. `@player`)
- Customizable notification display: chat, action bar, sound, or combinations
- Per-player mention preferences and sound settings
- Permission-based formatting for mentions
- Configurable cooldowns and mention limits
- SQLite and MySQL database support
- Locale and language support

## Installation

1. Download the latest release from [GitHub Releases](https://github.com/TavstalDev/OpenMentions/releases).
2. Place the `OpenMentions.jar` file into your server's `plugins` directory.
3. Start your server.

## Usage

- Main command: `/mentions`
    - `/mentions help` — Show help menu
    - `/mentions version` — Show plugin version
    - `/mentions reload` — Reload configuration
    - `/mentions sound <sound>` — Set your mention sound
    - `/mentions display <type>` — Set your mention display type
    - `/mentions preference <type>` — Set your mention preference
    - `/mentions igonre <player>` — Ignore mentions from a player
    - `/mentions unignore <player>` — Unignore mentions from a player

Tab completion is supported for all subcommands and options.

## Permissions

| Permission                        | Description                                 | Default |
|------------------------------------|---------------------------------------------|---------|
| openmentions.commands.mentions     | Use the `/mentions` command                 | true    |
| openmentions.commands.version      | View plugin version                         | op      |
| openmentions.commands.reload       | Reload the plugin                           | op      |

## CombatLogX Compatibility

OpenMentions has a soft dependency on [CombatLogX](https://www.spigotmc.org/resources/combatlogx.31689/).  
The plugin will work without CombatLogX, but it will **not detect combat states** unless CombatLogX is installed.  
For full mention preference functionality (e.g., "NEVER_IN_COMBAT", "SILENT_IN_COMBAT"), make sure CombatLogX is present on your server.

## License

This project is licensed under the **GNU General Public License v3.0**. You can find the full license text in the `LICENSE` file within this repository.

## Contact

For any questions, bug reports, or feature requests, please use the [GitHub issue tracker](https://github.com/TavstalDev/OpenMentions/issues).