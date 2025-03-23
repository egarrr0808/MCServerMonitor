# ServerMonitor

ServerMonitor is a performance monitoring plugin for Minecraft Paper servers. It provides a convenient interface for tracking key server metrics through in-game commands.

## Features

- **CPU Monitoring**: Display processor usage and available cores
- **Memory Monitoring**: Display used, free, and total memory
- **TPS Monitoring**: Display ticks per second over different time periods
- **Ping Monitoring**: Display ping for all players on the server
- **Server Information**: Display version, uptime, loaded chunks, and entities

## Requirements

- Minecraft Paper server 1.21.4 or higher
- Java 17 or higher

## Installation

1. Download the latest version of the plugin from the [Releases](https://github.com/egarrr0808/MCServerMonitor/releases/tag/plugin) section
2. Place the JAR file in the `plugins` folder of your Minecraft server
3. Restart the server or use the `/reload confirm` command
4. Done! Use the plugin commands to monitor your server

## Commands

- `/servermonitor` or `/sm` - Base plugin command
- `/sm help` - Shows a list of available commands
- `/sm cpu` - Shows CPU usage information
- `/sm memory` - Shows memory usage information
- `/sm tps` - Shows current TPS (ticks per second) of the server
- `/sm ping` - Shows ping for all players on the server
- `/sm all` - Shows all metrics at once
- `/sm reload` - Reloads the plugin configuration (requires servermonitor.reload permission)

## Permissions

- `servermonitor.use` - Allows using the basic plugin commands (default for operators)
- `servermonitor.reload` - Allows reloading the plugin configuration (default for operators)

## Configuration

The `config.yml` file is created automatically on first launch. You can configure:

```yaml
# Display settings
settings:
  # Color scheme: default, dark, light
  color_scheme: default
  # Show plugin prefix in messages
  show_prefix: true
  # Debug mode (enables additional logging)
  debug: false

# Performance thresholds
thresholds:
  # CPU usage thresholds (percentage)
  cpu:
    warning: 60
    critical: 85
  # Memory usage thresholds (percentage)
  memory:
    warning: 60
    critical: 85
  # TPS thresholds
  tps:
    warning: 15
    critical: 10
  # Ping thresholds (milliseconds)
  ping:
    warning: 100
    critical: 250

# Monitoring settings
monitoring:
  # Enable automatic monitoring alerts
  enable_alerts: false
  # Alert interval in seconds (minimum 60)
  alert_interval: 300
  # Who should receive alerts (console, ops, all)
  alert_recipients: ops
```

## Project Structure

```
ServerMonitor/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── servermonitor/
│       │           └── ServerMonitor.java
│       └── resources/
│           ├── config.yml
│           └── plugin.yml
├── pom.xml
├── LICENSE
├── README.md
└── .gitignore
```

## Building from Source

1. Clone the repository:
```
git clone https://github.com/yourusername/ServerMonitor.git
```

2. Build the project with Maven:
```
cd ServerMonitor
mvn clean package
```

3. The compiled JAR file will be in the `target/` folder

## License

This project is distributed under the MIT License. See the [LICENSE](LICENSE) file for details.
