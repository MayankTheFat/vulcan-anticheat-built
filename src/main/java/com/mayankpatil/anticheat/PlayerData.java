package com.mayankpatil.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * PlayerData - Tracks player information for detection purposes
 */
public class PlayerData {
    
    private final Player player;
    private final UUID uuid;
    
    // Movement tracking
    private List<Location> recentLocations = new ArrayList<>();
    private double lastSpeedX = 0;
    private double lastSpeedZ = 0;
    private long lastMoveTime = 0;
    private int airTicks = 0;
    private int groundTicks = 0;
    
    // Combat tracking
    private long lastAttackTime = 0;
    private int attacksPerSecond = 0;
    private double lastAttackAngle = 0;
    private int hitsInRow = 0;
    
    // Violation tracking
    private int flyViolations = 0;
    private int speedViolations = 0;
    private int killAuraViolations = 0;
    private int reachViolations = 0;
    private int autoclickViolations = 0;
    
    // Detection flags
    private boolean isFlyingIllegally = false;
    private boolean isSpeedHacking = false;
    private boolean hasKillAura = false;
    private boolean hasReachHack = false;
    private boolean hasAutoclicker = false;
    
    // Timestamps
    private long lastViolationReset = System.currentTimeMillis();
    private long joinTime = System.currentTimeMillis();
    
    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.lastMoveTime = System.currentTimeMillis();
    }
    
    public void updateMovement(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Calculate speed
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        double speedX = deltaX * 20; // Convert to blocks per second
        double speedZ = deltaZ * 20;
        
        // Store recent locations (max 20)
        recentLocations.add(to.clone());
        if (recentLocations.size() > 20) {
            recentLocations.remove(0);
        }
        
        // Update tracking data
        lastSpeedX = speedX;
        lastSpeedZ = speedZ;
        lastMoveTime = System.currentTimeMillis();
        
        // Update air/ground ticks
        if (player.isOnGround()) {
            groundTicks++;
            airTicks = 0;
        } else {
            airTicks++;
            groundTicks = 0;
        }
    }
    
    public void updateCombat(EntityDamageByEntityEvent event) {
        long currentTime = System.currentTimeMillis();
        
        // Calculate attacks per second
        if (currentTime - lastAttackTime < 1000) {
            attacksPerSecond++;
        } else {
            attacksPerSecond = 1;
        }
        
        lastAttackTime = currentTime;
        
        // Calculate attack angle if player is looking at entity
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            Location eyeLocation = player.getEyeLocation();
            Location targetLocation = target.getEyeLocation();
            
            double dx = targetLocation.getX() - eyeLocation.getX();
            double dy = targetLocation.getY() - eyeLocation.getY();
            double dz = targetLocation.getZ() - eyeLocation.getZ();
            
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double angle = Math.toDegrees(Math.acos(dy / distance));
            lastAttackAngle = angle;
        }
        
        // Track hits in a row
        hitsInRow++;
        if (currentTime - lastAttackTime > 2000) { // Reset if 2 seconds without attack
            hitsInRow = 0;
        }
    }
    
    public void addViolation(String checkType) {
        switch (checkType.toLowerCase()) {
            case "fly":
                flyViolations++;
                break;
            case "speed":
                speedViolations++;
                break;
            case "killaura":
                killAuraViolations++;
                break;
            case "reach":
                reachViolations++;
                break;
            case "autoclick":
                autoclickViolations++;
                break;
        }
    }
    
    public int getViolations(String checkType) {
        switch (checkType.toLowerCase()) {
            case "fly": return flyViolations;
            case "speed": return speedViolations;
            case "killaura": return killAuraViolations;
            case "reach": return reachViolations;
            case "autoclick": return autoclickViolations;
            default: return 0;
        }
    }
    
    public void resetViolations() {
        flyViolations = 0;
        speedViolations = 0;
        killAuraViolations = 0;
        reachViolations = 0;
        autoclickViolations = 0;
        lastViolationReset = System.currentTimeMillis();
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public UUID getUuid() { return uuid; }
    public List<Location> getRecentLocations() { return new ArrayList<>(recentLocations); }
    public double getLastSpeedX() { return lastSpeedX; }
    public double getLastSpeedZ() { return lastSpeedZ; }
    public long getLastMoveTime() { return lastMoveTime; }
    public int getAirTicks() { return airTicks; }
    public int getGroundTicks() { return groundTicks; }
    public long getLastAttackTime() { return lastAttackTime; }
    public int getAttacksPerSecond() { return attacksPerSecond; }
    public double getLastAttackAngle() { return lastAttackAngle; }
    public int getHitsInRow() { return hitsInRow; }
    public boolean isFlyingIllegally() { return isFlyingIllegally; }
    public boolean isSpeedHacking() { return isSpeedHacking; }
    public boolean hasKillAura() { return hasKillAura; }
    public boolean hasReachHack() { return hasReachHack; }
    public boolean hasAutoclicker() { return hasAutoclicker; }
    public long getJoinTime() { return joinTime; }
    
    // Setters for flags
    public void setFlyingIllegally(boolean flyingIllegally) { isFlyingIllegally = flyingIllegally; }
    public void setSpeedHacking(boolean speedHacking) { isSpeedHacking = speedHacking; }
    public void setHasKillAura(boolean hasKillAura) { this.hasKillAura = hasKillAura; }
    public void setHasReachHack(boolean hasReachHack) { this.hasReachHack = hasReachHack; }
    public void setHasAutoclicker(boolean hasAutoclicker) { this.hasAutoclicker = hasAutoclicker; }
}