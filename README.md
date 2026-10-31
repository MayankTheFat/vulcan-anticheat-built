# Vulcan AntiCheat - Built from Scratch

A custom-built Minecraft anti-cheat plugin created entirely from scratch. This is not a fork or copy of any existing Vulcan AntiCheat implementation - it's a completely original build with core detection features.

## Features

### Core Detection Algorithms
- **Fly Detection** - Detects illegal flying and excessive air time
- **Speed Detection** - Identifies unnatural movement speeds
- **KillAura Detection** - Detects 360-degree attacking patterns
- **Reach Detection** - Checks for extended attack distances
- **AutoClicker Detection** - Identifies unnaturally fast clicking
- **NoFall Detection** - Detects fall damage bypass

### Advanced Systems
- **Player Data Tracking** - Comprehensive player movement and combat analysis
- **Configurable Detection** - Adjustable thresholds for all checks
- **Smart Violation System** - Tracks violations with automatic reset
- **Performance Optimized** - Efficient algorithms with minimal server impact

### Alert & Punishment System
- **Real-time Alerts** - Notify staff when cheating is detected
- **Multi-channel Alerts** - Console, staff, and broadcast options
- **Graduated Punishments** - Warnings → Kicks → Bans
- **Setback System** - Teleports cheating players back
- **Bypass System** - Permission-based check bypassing

### Command System
- `/vulcan help` - Shows command help
- `/vulcan reload` - Reloads configuration
- `/vulcan alerts` - Toggle cheat alerts
- `/vulcan bypass` - Toggle bypass mode
- `/vulcan violations <player>` - Check player violations
- `/vulcan reset <player>` - Reset player violations
- `/vulcan info` - Shows plugin information

## Installation

### Requirements
- **Minecraft Server**: Paper/Spigot 1.21.4+
- **Java**: Version 21+
- **Build Tool**: Maven 3.9+

### Building from Source
```bash
# Clone the repository
git clone https://github.com/MayankPatil3/vulcan-anticheat-built.git
cd vulcan-anticheat-built

# Build the plugin
mvn clean package

# The compiled JAR will be in target/vulcan-anticheat-built-1.0.0.jar
```

### Server Installation
1. Build the plugin or download the latest release
2. Place the JAR file in your server's `plugins/` directory
3. Restart your server
4. Configure the plugin in `plugins/VulcanAntiCheatBuilt/config.yml`

## Configuration

The plugin is highly configurable. Key configuration areas:

### Detection Settings
```yaml
detection:
  enabled-checks: ["fly", "speed", "killaura", "reach", "autoclick", "nofall"]
  max-horizontal-speed: 5.0
  max-vertical-speed: 1.2
  max-attacks-per-second: 15
  max-reach-distance: 4.5
  violation-threshold: 5
```

### Alert Settings
```yaml
alerts:
  enabled: true
  broadcast: false
  console: true
  prefix: "&8[&cVulcan&8]"
  format: "{player} &7failed &c{check}&7: &f{details} &8(&c{vl}&8)"
```

### Punishment Settings
```yaml
punishments:
  enabled: true
  kick:
    enabled: true
    threshold: 10
  ban:
    enabled: false
    threshold: 20
```

## Permissions

- `vulcan.admin` - Full access to all commands
- `vulcan.alerts` - Receive cheat detection alerts
- `vulcan.reload` - Reload plugin configuration
- `vulcan.violations` - Check player violation counts
- `vulcan.reset` - Reset player violations
- `vulcan.debug` - Access debug commands
- `vulcan.bypass` - Bypass anti-cheat checks
- `vulcan.bypass.all` - Bypass all anti-cheat checks
- `vulcan.bypass.<check>` - Bypass specific checks (fly, speed, killaura, reach, autoclick, nofall)
- `vulcan.bypass.punishment` - Bypass punishments

## Project Structure

```
src/main/java/com/mayankpatil/anticheat/
├── VulcanAntiCheat.java          # Main plugin class
├── PlayerData.java               # Player tracking and data
├── DetectionManager.java         # Core detection algorithms
├── AlertManager.java             # Alert system
├── PunishmentManager.java        # Punishment system
└── VulcanCommand.java            # Command handler

src/main/resources/
├── plugin.yml                    # Plugin metadata
└── config.yml                    # Configuration file
```

## Technical Details

### Detection Algorithms
- **Movement Analysis**: Tracks player locations, speeds, and patterns
- **Combat Analysis**: Monitors attack angles, frequencies, and distances
- **Statistical Analysis**: Uses thresholds and patterns to identify anomalies
- **Real-time Processing**: Efficient algorithms with minimal performance impact

### Data Management
- **In-memory Tracking**: Fast access to player data
- **Automatic Cleanup**: Removes data for offline players
- **Violation Persistence**: Tracks violations with configurable reset intervals
- **Performance Monitoring**: Built-in performance optimization

## Development

### Building the Project
```bash
mvn clean compile        # Compile the project
mvn test                 # Run tests
mvn package              # Build the JAR file
```

### Code Style
- Follows Java coding conventions
- Comprehensive JavaDoc comments
- Proper error handling and logging
- Modular design with clear separation of concerns

### Extending the Plugin
The plugin is designed to be extensible. You can:
1. Add new detection checks by extending the detection system
2. Implement custom punishment methods
3. Add new alert channels (Discord, Webhooks, etc.)
4. Integrate with other plugins via the API

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built entirely from scratch by MayankPatil3
- Inspired by the need for transparent, customizable anti-cheat solutions
- Thanks to the Minecraft plugin development community

## Support

For issues, feature requests, or questions:
1. Check the [GitHub Issues](https://github.com/MayankPatil3/vulcan-anticheat-built/issues)
2. Review the configuration documentation
3. Ensure you're using compatible server software

---

**Note**: This is a custom-built anti-cheat solution. It may have different behavior compared to other Vulcan AntiCheat implementations as it was built from scratch based on research and understanding of anti-cheat principles.