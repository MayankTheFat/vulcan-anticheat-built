package com.mayankpatil.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PunishmentManager - Handles punishments for cheating violations
 */
public class PunishmentManager {
    
    private final VulcanAntiCheat plugin;
    private final Map<UUID, Integer> violationHistory = new HashMap<>();
    
    // Punishment configuration
    private boolean punishmentsEnabled = true;
    private boolean kickEnabled = true;
    private boolean banEnabled = false;
    private boolean warnEnabled = true;
    private boolean setbackEnabled = true;
    
    // Thresholds
    private int kickThreshold = 10;
    private int banThreshold = 20;
    private int warnThreshold = 5;
    
    // Messages
    private String kickMessage = "&cYou have been kicked for cheating.";
    private String banMessage = "&cYou have been banned for cheating.";
    private String warnMessage = "&cWarning: Cheating violations detected!";
    
    // Cooldowns
    private long lastKickTime = 0;
    private long lastBanTime = 0;
    private final long cooldownMillis = 60000; // 1 minute cooldown
    
    public PunishmentManager(VulcanAntiCheat plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        FileConfiguration config = plugin.getPluginConfig();
        
        punishmentsEnabled = config.getBoolean("punishments.enabled", true);
        kickEnabled = config.getBoolean("punishments.kick.enabled", true);
        banEnabled = config.getBoolean("punishments.ban.enabled", false);
        warnEnabled = config.getBoolean("punishments.warn.enabled", true);
        setbackEnabled = config.getBoolean("punishments.setback.enabled", true);
        
        kickThreshold = config.getInt("punishments.kick.threshold", 10);
        banThreshold = config.getInt("punishments.ban.threshold", 20);
        warnThreshold = config.getInt("punishments.warn.threshold", 5);
        
        kickMessage = config.getString("punishments.kick.message", "&cYou have been kicked for cheating.");
        banMessage = config.getString("punishments.ban.message", "&cYou have been banned for cheating.");
        warnMessage = config.getString("punishments.warn.message", "&cWarning: Cheating violations detected!");
    }
    
    public void applyPunishment(Player player, String check, int violations) {
        if (!punishmentsEnabled) return;
        
        // Check if player has bypass permission
        if (player.hasPermission("vulcan.bypass.punishment")) {
            plugin.getLogger().info("Player " + player.getName() + " bypassed punishment for " + check);
            return;
        }
        
        // Get total violations for player
        int totalViolations = getTotalViolations(player);
        
        // Apply punishments based on thresholds
        if (banEnabled && totalViolations >= banThreshold) {
            banPlayer(player, check, totalViolations);
        } else if (kickEnabled && totalViolations >= kickThreshold) {
            kickPlayer(player, check, totalViolations);
        } else if (warnEnabled && totalViolations >= warnThreshold) {
            warnPlayer(player, check, totalViolations);
        }
        
        // Apply setback if enabled
        if (setbackEnabled) {
            applySetback(player);
        }
        
        // Log punishment
        logPunishment(player, check, violations, totalViolations);
    }
    
    private int getTotalViolations(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerData(uuid);
        
        if (data == null) return 0;
        
        // Sum all violation types
        int total = data.getViolations("fly") +
                   data.getViolations("speed") +
                   data.getViolations("killaura") +
                   data.getViolations("reach") +
                   data.getViolations("autoclick");
        
        // Store in history
        violationHistory.put(uuid, total);
        
        return total;
    }
    
    private void banPlayer(Player player, String check, int violations) {
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBanTime < cooldownMillis) {
            return; // Cooldown active
        }
        
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', 
            banMessage + " &7(Check: " + check + ", Violations: " + violations + ")");
        
        // Broadcast ban
        Bukkit.broadcastMessage(ChatColor.RED + "[Vulcan] " + player.getName() + " has been banned for cheating.");
        
        // Ban player (simplified - in real implementation would use a ban system)
        player.kickPlayer(formattedMessage);
        
        lastBanTime = currentTime;
        plugin.getLogger().warning("Banned player " + player.getName() + " for " + check + " (violations: " + violations + ")");
    }
    
    private void kickPlayer(Player player, String check, int violations) {
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKickTime < cooldownMillis) {
            return; // Cooldown active
        }
        
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', 
            kickMessage + " &7(Check: " + check + ", Violations: " + violations + ")");
        
        // Broadcast kick
        Bukkit.broadcastMessage(ChatColor.YELLOW + "[Vulcan] " + player.getName() + " has been kicked for cheating.");
        
        // Kick player
        player.kickPlayer(formattedMessage);
        
        lastKickTime = currentTime;
        plugin.getLogger().warning("Kicked player " + player.getName() + " for " + check + " (violations: " + violations + ")");
    }
    
    private void warnPlayer(Player player, String check, int violations) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', 
            warnMessage + " &7(Check: " + check + ", Violations: " + violations + ")");
        
        // Send warning to player
        player.sendMessage(formattedMessage);
        
        // Send warning to staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("vulcan.alerts")) {
                staff.sendMessage(ChatColor.YELLOW + "[Vulcan] " + player.getName() + 
                    " received warning for " + check + " (violations: " + violations + ")");
            }
        }
        
        plugin.getLogger().info("Warned player " + player.getName() + " for " + check + " (violations: " + violations + ")");
    }
    
    private void applySetback(Player player) {
        // Simple setback - teleport player to their last safe location
        // In a real implementation, you would track safe locations
        
        if (player.isOnline()) {
            player.teleport(player.getLocation()); // Simple teleport as placeholder
            player.sendMessage(ChatColor.RED + "Setback applied for cheating.");
        }
    }
    
    private void logPunishment(Player player, String check, int checkViolations, int totalViolations) {
        // Log to console
        plugin.getLogger().info("Punishment Log:");
        plugin.getLogger().info("  Player: " + player.getName() + " (" + player.getUniqueId() + ")");
        plugin.getLogger().info("  Check: " + check);
        plugin.getLogger().info("  Check Violations: " + checkViolations);
        plugin.getLogger().info("  Total Violations: " + totalViolations);
        plugin.getLogger().info("  Time: " + System.currentTimeMillis());
        
        // TODO: Log to file or database in real implementation
    }
    
    public void resetViolations(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerData(uuid);
        
        if (data != null) {
            data.resetViolations();
        }
        
        violationHistory.remove(uuid);
        player.sendMessage(ChatColor.GREEN + "Your violation count has been reset.");
    }
    
    public int getPlayerViolations(Player player) {
        return violationHistory.getOrDefault(player.getUniqueId(), 0);
    }
    
    // Getters
    public boolean isPunishmentsEnabled() { return punishmentsEnabled; }
    public boolean isKickEnabled() { return kickEnabled; }
    public boolean isBanEnabled() { return banEnabled; }
    public boolean isWarnEnabled() { return warnEnabled; }
    public boolean isSetbackEnabled() { return setbackEnabled; }
    public int getKickThreshold() { return kickThreshold; }
    public int getBanThreshold() { return banThreshold; }
    public int getWarnThreshold() { return warnThreshold; }
    
    // Setters
    public void setPunishmentsEnabled(boolean enabled) { 
        punishmentsEnabled = enabled; 
        plugin.getPluginConfig().set("punishments.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setKickEnabled(boolean enabled) { 
        kickEnabled = enabled; 
        plugin.getPluginConfig().set("punishments.kick.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setBanEnabled(boolean enabled) { 
        banEnabled = enabled; 
        plugin.getPluginConfig().set("punishments.ban.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setWarnEnabled(boolean enabled) { 
        warnEnabled = enabled; 
        plugin.getPluginConfig().set("punishments.warn.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setSetbackEnabled(boolean enabled) { 
        setbackEnabled = enabled; 
        plugin.getPluginConfig().set("punishments.setback.enabled", enabled);
        plugin.saveConfig();
    }
}