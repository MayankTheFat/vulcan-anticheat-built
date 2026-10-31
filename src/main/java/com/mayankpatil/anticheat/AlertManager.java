package com.mayankpatil.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * AlertManager - Handles sending alerts for detected violations
 */
public class AlertManager {
    
    private final VulcanAntiCheat plugin;
    private final Set<UUID> alertReceivers = new HashSet<>();
    private final Set<UUID> bypassingPlayers = new HashSet<>();
    
    // Alert configuration
    private String alertPrefix = "&8[&cVulcan&8]";
    private boolean alertsEnabled = true;
    private boolean broadcastAlerts = false;
    private boolean consoleAlerts = true;
    private String alertFormat = "{player} &7failed &c{check}&7: &f{details} &8(&c{vl}&8)";
    
    public AlertManager(VulcanAntiCheat plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        FileConfiguration config = plugin.getPluginConfig();
        
        alertPrefix = config.getString("alerts.prefix", "&8[&cVulcan&8]");
        alertsEnabled = config.getBoolean("alerts.enabled", true);
        broadcastAlerts = config.getBoolean("alerts.broadcast", false);
        consoleAlerts = config.getBoolean("alerts.console", true);
        alertFormat = config.getString("alerts.format", "{player} &7failed &c{check}&7: &f{details} &8(&c{vl}&8)");
        
        // Load staff with alert permissions
        for (String uuidStr : config.getStringList("alerts.receivers")) {
            try {
                alertReceivers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in alert receivers: " + uuidStr);
            }
        }
    }
    
    public void sendAlert(Player player, String check, String details, int violations) {
        if (!alertsEnabled) return;
        
        String formattedAlert = formatAlert(player, check, details, violations);
        
        // Send to console if enabled
        if (consoleAlerts) {
            plugin.getLogger().warning(ChatColor.stripColor(formattedAlert));
        }
        
        // Send to staff with alert permissions
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (shouldReceiveAlerts(onlinePlayer)) {
                onlinePlayer.sendMessage(formattedAlert);
            }
        }
        
        // Broadcast if enabled
        if (broadcastAlerts) {
            Bukkit.broadcastMessage(formattedAlert);
        }
    }
    
    private String formatAlert(Player player, String check, String details, int violations) {
        String formatted = alertFormat
            .replace("{player}", player.getName())
            .replace("{check}", check)
            .replace("{details}", details)
            .replace("{vl}", String.valueOf(violations));
        
        return ChatColor.translateAlternateColorCodes('&', alertPrefix + " " + formatted);
    }
    
    private boolean shouldReceiveAlerts(Player player) {
        // Check if player has alert permission
        if (player.hasPermission("vulcan.alerts")) {
            return true;
        }
        
        // Check if player is in receivers list
        if (alertReceivers.contains(player.getUniqueId())) {
            return true;
        }
        
        // Check if player is OP (optional)
        if (player.isOp() && plugin.getPluginConfig().getBoolean("alerts.ops-receive", true)) {
            return true;
        }
        
        return false;
    }
    
    public void addAlertReceiver(Player player) {
        alertReceivers.add(player.getUniqueId());
        saveReceivers();
    }
    
    public void removeAlertReceiver(Player player) {
        alertReceivers.remove(player.getUniqueId());
        saveReceivers();
    }
    
    public void toggleAlerts(Player player) {
        if (alertReceivers.contains(player.getUniqueId())) {
            removeAlertReceiver(player);
            player.sendMessage(ChatColor.RED + "Alerts disabled.");
        } else {
            addAlertReceiver(player);
            player.sendMessage(ChatColor.GREEN + "Alerts enabled.");
        }
    }
    
    private void saveReceivers() {
        // Convert UUIDs to strings
        java.util.List<String> uuidStrings = new java.util.ArrayList<>();
        for (UUID uuid : alertReceivers) {
            uuidStrings.add(uuid.toString());
        }
        
        plugin.getPluginConfig().set("alerts.receivers", uuidStrings);
        plugin.saveConfig();
    }
    
    public void toggleBypass(Player player) {
        if (bypassingPlayers.contains(player.getUniqueId())) {
            bypassingPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Bypass disabled.");
        } else {
            bypassingPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Bypass enabled.");
        }
    }
    
    public boolean isBypassing(Player player) {
        return bypassingPlayers.contains(player.getUniqueId()) || 
               player.hasPermission("vulcan.bypass.all");
    }
    
    // Getters
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public boolean isBroadcastAlerts() { return broadcastAlerts; }
    public boolean isConsoleAlerts() { return consoleAlerts; }
    public Set<UUID> getAlertReceivers() { return new HashSet<>(alertReceivers); }
    
    // Setters
    public void setAlertsEnabled(boolean enabled) { 
        alertsEnabled = enabled; 
        plugin.getPluginConfig().set("alerts.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setBroadcastAlerts(boolean broadcast) { 
        broadcastAlerts = broadcast; 
        plugin.getPluginConfig().set("alerts.broadcast", broadcast);
        plugin.saveConfig();
    }
    
    public void setConsoleAlerts(boolean console) { 
        consoleAlerts = console; 
        plugin.getPluginConfig().set("alerts.console", console);
        plugin.saveConfig();
    }
}