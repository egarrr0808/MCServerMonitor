package com.servermonitor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerMonitor extends JavaPlugin {
    
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private final Map<UUID, Long> lastPingCheck = new HashMap<>();
    private double[] tps = new double[3];
    private long lastTPSCheck = 0;
    
    @Override
    public void onEnable() {
        getLogger().info("ServerMonitor plugin has been enabled!");
        
        try {
            // Create default config if it doesn't exist
            saveDefaultConfig();
            
            // Start TPS monitoring
            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                long sec;
                long currentSec;
                int ticks;
                
                @Override
                public void run() {
                    sec = (System.currentTimeMillis() / 1000);
                    
                    if (currentSec == sec) {
                        ticks++;
                    } else {
                        currentSec = sec;
                        tps[0] = tps[0] * 0.8 + ticks * 0.2;
                        tps[1] = tps[1] * 0.9 + ticks * 0.1;
                        tps[2] = tps[2] * 0.95 + ticks * 0.05;
                        ticks = 1;
                    }
                }
            }, 0, 1);
            
            getLogger().info("TPS monitoring started successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ServerMonitor plugin has been disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("servermonitor")) {
            if (!sender.hasPermission("servermonitor.use")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
            
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                showHelp(sender);
                return true;
            }
            
            try {
                switch (args[0].toLowerCase()) {
                    case "cpu":
                        showCPUUsage(sender);
                        break;
                    case "memory":
                        showMemoryUsage(sender);
                        break;
                    case "tps":
                        showTPS(sender);
                        break;
                    case "ping":
                        showPing(sender);
                        break;
                    case "all":
                        showAllMetrics(sender);
                        break;
                    case "reload":
                        if (sender.hasPermission("servermonitor.reload")) {
                            reloadConfig();
                            sender.sendMessage(ChatColor.GREEN + "ServerMonitor configuration reloaded!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You don't have permission to reload the plugin!");
                        }
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /sm help for available commands.");
                        break;
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "An error occurred while executing the command: " + e.getMessage());
                getLogger().severe("Command execution error: " + e.getMessage());
                e.printStackTrace();
            }
            
            return true;
        }
        return false;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== ServerMonitor Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/sm cpu " + ChatColor.WHITE + "- Shows CPU usage");
        sender.sendMessage(ChatColor.YELLOW + "/sm memory " + ChatColor.WHITE + "- Shows memory usage");
        sender.sendMessage(ChatColor.YELLOW + "/sm tps " + ChatColor.WHITE + "- Shows server TPS");
        sender.sendMessage(ChatColor.YELLOW + "/sm ping " + ChatColor.WHITE + "- Shows player ping");
        sender.sendMessage(ChatColor.YELLOW + "/sm all " + ChatColor.WHITE + "- Shows all metrics");
        if (sender.hasPermission("servermonitor.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "/sm reload " + ChatColor.WHITE + "- Reloads the plugin configuration");
        }
        sender.sendMessage(ChatColor.YELLOW + "/sm help " + ChatColor.WHITE + "- Shows this help message");
    }
    
    private void showCPUUsage(CommandSender sender) {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();
            
            if (cpuLoad < 0) {
                // Some systems don't support this metric
                cpuLoad = getCPULoadManually();
            }
            
            double cpuPercentage = (cpuLoad / availableProcessors) * 100;
            
            sender.sendMessage(ChatColor.GREEN + "=== CPU Usage ===");
            sender.sendMessage(ChatColor.YELLOW + "System Load: " + ChatColor.WHITE + df.format(cpuLoad));
            sender.sendMessage(ChatColor.YELLOW + "Available Processors: " + ChatColor.WHITE + availableProcessors);
            sender.sendMessage(ChatColor.YELLOW + "CPU Usage: " + getColorForPercentage(cpuPercentage) + df.format(cpuPercentage) + "%");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to get CPU usage: " + e.getMessage());
            getLogger().warning("Error getting CPU usage: " + e.getMessage());
        }
    }
    
    private double getCPULoadManually() {
        try {
            // This is a fallback method if getSystemLoadAverage() returns -1
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("top -b -n 1");
            java.io.InputStream is = process.getInputStream();
            java.io.InputStreamReader isr = new java.io.InputStreamReader(is);
            java.io.BufferedReader br = new java.io.BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Cpu(s)")) {
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        if (part.contains("id")) {
                            double idlePercentage = Double.parseDouble(part.replaceAll("[^0-9.]", ""));
                            return 100.0 - idlePercentage;
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLogger().warning("Failed to get CPU load manually: " + e.getMessage());
        }
        return 0;
    }
    
    private void showMemoryUsage(CommandSender sender) {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedHeapMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxHeapMemory = memoryBean.getHeapMemoryUsage().getMax();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double usedMemoryPercentage = ((double) usedMemory / totalMemory) * 100;
            double usedHeapPercentage = ((double) usedHeapMemory / maxHeapMemory) * 100;
            
            sender.sendMessage(ChatColor.GREEN + "=== Memory Usage ===");
            sender.sendMessage(ChatColor.YELLOW + "Used Memory: " + ChatColor.WHITE + formatSize(usedMemory) + 
                            " / " + formatSize(totalMemory) + " (" + getColorForPercentage(usedMemoryPercentage) + 
                            df.format(usedMemoryPercentage) + "%" + ChatColor.WHITE + ")");
            sender.sendMessage(ChatColor.YELLOW + "Heap Memory: " + ChatColor.WHITE + formatSize(usedHeapMemory) + 
                            " / " + formatSize(maxHeapMemory) + " (" + getColorForPercentage(usedHeapPercentage) + 
                            df.format(usedHeapPercentage) + "%" + ChatColor.WHITE + ")");
            sender.sendMessage(ChatColor.YELLOW + "Free Memory: " + ChatColor.WHITE + formatSize(freeMemory));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to get memory usage: " + e.getMessage());
            getLogger().warning("Error getting memory usage: " + e.getMessage());
        }
    }
    
    private void showTPS(CommandSender sender) {
        try {
            sender.sendMessage(ChatColor.GREEN + "=== Server TPS ===");
            sender.sendMessage(ChatColor.YELLOW + "TPS (1m): " + getColorForTPS(tps[0]) + df.format(Math.min(tps[0], 20.0)));
            sender.sendMessage(ChatColor.YELLOW + "TPS (5m): " + getColorForTPS(tps[1]) + df.format(Math.min(tps[1], 20.0)));
            sender.sendMessage(ChatColor.YELLOW + "TPS (15m): " + getColorForTPS(tps[2]) + df.format(Math.min(tps[2], 20.0)));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to get TPS: " + e.getMessage());
            getLogger().warning("Error getting TPS: " + e.getMessage());
        }
    }
    
    private void showPing(CommandSender sender) {
        try {
            sender.sendMessage(ChatColor.GREEN + "=== Player Ping ===");
            
            boolean foundPlayers = false;
            for (Player player : Bukkit.getOnlinePlayers()) {
                int ping = player.getPing();
                sender.sendMessage(ChatColor.YELLOW + player.getName() + ": " + getColorForPing(ping) + ping + "ms");
                foundPlayers = true;
            }
            
            if (!foundPlayers) {
                sender.sendMessage(ChatColor.RED + "No players online!");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to get player ping: " + e.getMessage());
            getLogger().warning("Error getting player ping: " + e.getMessage());
        }
    }
    
    private void showAllMetrics(CommandSender sender) {
        try {
            showCPUUsage(sender);
            sender.sendMessage("");
            showMemoryUsage(sender);
            sender.sendMessage("");
            showTPS(sender);
            sender.sendMessage("");
            showPing(sender);
            sender.sendMessage("");
            showServerInfo(sender);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to show all metrics: " + e.getMessage());
            getLogger().warning("Error showing all metrics: " + e.getMessage());
        }
    }
    
    private void showServerInfo(CommandSender sender) {
        try {
            sender.sendMessage(ChatColor.GREEN + "=== Server Info ===");
            sender.sendMessage(ChatColor.YELLOW + "Server Version: " + ChatColor.WHITE + Bukkit.getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Bukkit Version: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
            sender.sendMessage(ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
            
            // Get uptime
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            long uptimeSeconds = uptime / 1000;
            long uptimeMinutes = uptimeSeconds / 60;
            long uptimeHours = uptimeMinutes / 60;
            long uptimeDays = uptimeHours / 24;
            
            uptimeHours = uptimeHours % 24;
            uptimeMinutes = uptimeMinutes % 60;
            uptimeSeconds = uptimeSeconds % 60;
            
            StringBuilder uptimeStr = new StringBuilder();
            if (uptimeDays > 0) {
                uptimeStr.append(uptimeDays).append("d ");
            }
            if (uptimeHours > 0 || uptimeDays > 0) {
                uptimeStr.append(uptimeHours).append("h ");
            }
            if (uptimeMinutes > 0 || uptimeHours > 0 || uptimeDays > 0) {
                uptimeStr.append(uptimeMinutes).append("m ");
            }
            uptimeStr.append(uptimeSeconds).append("s");
            
            sender.sendMessage(ChatColor.YELLOW + "Uptime: " + ChatColor.WHITE + uptimeStr.toString());
            
            // Get loaded chunks and entities
            int totalChunks = 0;
            int totalEntities = 0;
            
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                totalChunks += world.getLoadedChunks().length;
                totalEntities += world.getEntities().size();
            }
            
            sender.sendMessage(ChatColor.YELLOW + "Loaded Chunks: " + ChatColor.WHITE + totalChunks);
            sender.sendMessage(ChatColor.YELLOW + "Loaded Entities: " + ChatColor.WHITE + totalEntities);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to get server info: " + e.getMessage());
            getLogger().warning("Error getting server info: " + e.getMessage());
        }
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return df.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }
    
    private ChatColor getColorForPercentage(double percentage) {
        if (percentage < 60) {
            return ChatColor.GREEN;
        } else if (percentage < 85) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
    
    private ChatColor getColorForTPS(double tps) {
        if (tps >= 18.0) {
            return ChatColor.GREEN;
        } else if (tps >= 15.0) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
    
    private ChatColor getColorForPing(int ping) {
        if (ping < 100) {
            return ChatColor.GREEN;
        } else if (ping < 250) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
}
