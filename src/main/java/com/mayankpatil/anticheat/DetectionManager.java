package com.mayankpatil.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * DetectionManager - Implements core anti-cheat detection algorithms
 */
public class DetectionManager {
    
    private final VulcanAntiCheat plugin;
    private final Set<String> enabledChecks = new HashSet<>();
    
    // Configuration thresholds
    private double maxHorizontalSpeed = 5.0; // blocks per second
    private double maxVerticalSpeed = 1.2; // blocks per second
    private int maxAirTicks = 20; // ticks (1 second)
    private int maxAttacksPerSecond = 15;
    private double maxReachDistance = 4.5; // blocks
    private double maxAngleChange = 90.0; // degrees per second
    private int violationThreshold = 5;
    
    public DetectionManager(VulcanAntiCheat plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        // Load configuration values
        maxHorizontalSpeed = plugin.getPluginConfig().getDouble("detection.max-horizontal-speed", 5.0);
        maxVerticalSpeed = plugin.getPluginConfig().getDouble("detection.max-vertical-speed", 1.2);
        maxAirTicks = plugin.getPluginConfig().getInt("detection.max-air-ticks", 20);
        maxAttacksPerSecond = plugin.getPluginConfig().getInt("detection.max-attacks-per-second", 15);
        maxReachDistance = plugin.getPluginConfig().getDouble("detection.max-reach-distance", 4.5);
        maxAngleChange = plugin.getPluginConfig().getDouble("detection.max-angle-change", 90.0);
        violationThreshold = plugin.getPluginConfig().getInt("detection.violation-threshold", 5);
        
        // Load enabled checks
        for (String check : plugin.getPluginConfig().getStringList("detection.enabled-checks")) {
            enabledChecks.add(check.toLowerCase());
        }
        
        plugin.getLogger().info("Loaded " + enabledChecks.size() + " detection checks");
    }
    
    public void runChecks(Player player, PlayerData data) {
        // Run all enabled checks periodically
        if (enabledChecks.contains("fly")) {
            checkFly(player, data);
        }
        
        if (enabledChecks.contains("speed")) {
            checkSpeed(player, data);
        }
        
        // Reset violations if needed
        if (System.currentTimeMillis() - data.getLastViolationReset() > 60000) { // 1 minute
            data.resetViolations();
        }
    }
    
    public void checkMovement(Player player, PlayerData data, PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Fly detection
        if (enabledChecks.contains("fly")) {
            checkFly(player, data);
        }
        
        // Speed detection
        if (enabledChecks.contains("speed")) {
            checkSpeed(player, data);
        }
        
        // NoFall detection (simplified)
        if (enabledChecks.contains("nofall")) {
            checkNoFall(player, data, event);
        }
    }
    
    public void checkCombat(Player attacker, PlayerData data, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player victim = (Player) event.getEntity();
        
        // KillAura detection
        if (enabledChecks.contains("killaura")) {
            checkKillAura(attacker, data, victim);
        }
        
        // Reach detection
        if (enabledChecks.contains("reach")) {
            checkReach(attacker, data, victim);
        }
        
        // AutoClicker detection
        if (enabledChecks.contains("autoclick")) {
            checkAutoClicker(attacker, data);
        }
    }
    
    private void checkFly(Player player, PlayerData data) {
        if (player.hasPermission("vulcan.bypass.fly")) return;
        
        // Check if player is flying without permission
        boolean isFlying = player.isFlying();
        boolean hasFlyPermission = player.getAllowFlight();
        
        if (isFlying && !hasFlyPermission) {
            data.addViolation("fly");
            data.setFlyingIllegally(true);
            
            if (data.getViolations("fly") >= violationThreshold) {
                flag(player, "Fly", "Flying without permission", data.getViolations("fly"));
            }
        } else {
            data.setFlyingIllegally(false);
        }
        
        // Advanced fly detection based on air time
        if (data.getAirTicks() > maxAirTicks && !player.isFlying() && !player.isGliding()) {
            data.addViolation("fly");
            
            if (data.getViolations("fly") >= violationThreshold) {
                flag(player, "Fly", "Excessive air time (" + data.getAirTicks() + " ticks)", data.getViolations("fly"));
            }
        }
    }
    
    private void checkSpeed(Player player, PlayerData data) {
        if (player.hasPermission("vulcan.bypass.speed")) return;
        
        double speedX = Math.abs(data.getLastSpeedX());
        double speedZ = Math.abs(data.getLastSpeedZ());
        double horizontalSpeed = Math.sqrt(speedX * speedX + speedZ * speedZ);
        
        // Check horizontal speed
        if (horizontalSpeed > maxHorizontalSpeed) {
            data.addViolation("speed");
            data.setSpeedHacking(true);
            
            if (data.getViolations("speed") >= violationThreshold) {
                flag(player, "Speed", "Horizontal speed: " + String.format("%.2f", horizontalSpeed) + " b/s", data.getViolations("speed"));
            }
        } else {
            data.setSpeedHacking(false);
        }
    }
    
    private void checkNoFall(Player player, PlayerData data, PlayerMoveEvent event) {
        if (player.hasPermission("vulcan.bypass.nofall")) return;
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Simple NoFall detection based on fall distance
        double fallDistance = player.getFallDistance();
        double verticalChange = to.getY() - from.getY();
        
        // If player is falling but vertical change is positive (going up)
        if (fallDistance > 3.0 && verticalChange > 0) {
            data.addViolation("nofall");
            flag(player, "NoFall", "Fall distance: " + String.format("%.1f", fallDistance), data.getViolations("nofall"));
        }
    }
    
    private void checkKillAura(Player attacker, PlayerData data, Player victim) {
        if (attacker.hasPermission("vulcan.bypass.killaura")) return;
        
        // Check attack angle consistency
        double currentAngle = data.getLastAttackAngle();
        
        // Simple KillAura detection based on attack pattern
        if (data.getHitsInRow() > 3) {
            // If hitting multiple players in quick succession (simplified check)
            data.addViolation("killaura");
            data.setHasKillAura(true);
            
            if (data.getViolations("killaura") >= violationThreshold) {
                flag(attacker, "KillAura", "Multiple rapid attacks: " + data.getHitsInRow(), data.getViolations("killaura"));
            }
        } else {
            data.setHasKillAura(false);
        }
    }
    
    private void checkReach(Player attacker, PlayerData data, Player victim) {
        if (attacker.hasPermission("vulcan.bypass.reach")) return;
        
        Location attackerLoc = attacker.getEyeLocation();
        Location victimLoc = victim.getEyeLocation();
        
        double distance = attackerLoc.distance(victimLoc);
        
        if (distance > maxReachDistance) {
            data.addViolation("reach");
            data.setHasReachHack(true);
            
            if (data.getViolations("reach") >= violationThreshold) {
                flag(attacker, "Reach", "Distance: " + String.format("%.2f", distance) + " blocks", data.getViolations("reach"));
            }
        } else {
            data.setHasReachHack(false);
        }
    }
    
    private void checkAutoClicker(Player attacker, PlayerData data) {
        if (attacker.hasPermission("vulcan.bypass.autoclick")) return;
        
        int attacksPerSecond = data.getAttacksPerSecond();
        
        if (attacksPerSecond > maxAttacksPerSecond) {
            data.addViolation("autoclick");
            data.setHasAutoclicker(true);
            
            if (data.getViolations("autoclick") >= violationThreshold) {
                flag(attacker, "AutoClicker", "CPS: " + attacksPerSecond, data.getViolations("autoclick"));
            }
        } else {
            data.setHasAutoclicker(false);
        }
    }
    
    private void flag(Player player, String check, String details, int violations) {
        // Send alert
        plugin.getAlertManager().sendAlert(player, check, details, violations);
        
        // Apply punishment if needed
        if (violations >= violationThreshold * 2) { // Double threshold for punishment
            plugin.getPunishmentManager().applyPunishment(player, check, violations);
        }
    }
    
    // Getters for configuration
    public double getMaxHorizontalSpeed() { return maxHorizontalSpeed; }
    public double getMaxVerticalSpeed() { return maxVerticalSpeed; }
    public int getMaxAirTicks() { return maxAirTicks; }
    public int getMaxAttacksPerSecond() { return maxAttacksPerSecond; }
    public double getMaxReachDistance() { return maxReachDistance; }
    public double getMaxAngleChange() { return maxAngleChange; }
    public int getViolationThreshold() { return violationThreshold; }
    public Set<String> getEnabledChecks() { return new HashSet<>(enabledChecks); }
}