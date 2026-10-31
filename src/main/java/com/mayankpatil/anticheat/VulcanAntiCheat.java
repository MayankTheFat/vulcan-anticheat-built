package com.mayankpatil.anticheat;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * VulcanAntiCheat - A custom-built Minecraft anti-cheat plugin
 * Built from scratch with core detection features
 */
public class VulcanAntiCheat extends JavaPlugin implements Listener {
    
    private FileConfiguration config;
    private DetectionManager detectionManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        config = getConfig();
        
        // Initialize managers
        detectionManager = new DetectionManager(this);
        alertManager = new AlertManager(this);
        punishmentManager = new PunishmentManager(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register commands
        getCommand("vulcan").setExecutor(new VulcanCommand(this));
        
        // Start detection task
        startDetectionTask();
        
        getLogger().info("VulcanAntiCheat v1.0.0 has been enabled!");
        getLogger().info("Built from scratch by MayankPatil3");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("VulcanAntiCheat has been disabled!");
    }
    
    private void startDetectionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Run periodic detection checks
                for (Player player : getServer().getOnlinePlayers()) {
                    PlayerData data = getPlayerData(player.getUniqueId());
                    if (data != null) {
                        detectionManager.runChecks(player, data);
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L); // Run every second
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Initialize player data
        PlayerData data = new PlayerData(player);
        playerDataMap.put(uuid, data);
        
        getLogger().info("Tracking player: " + player.getName());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataMap.remove(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData data = getPlayerData(player.getUniqueId());
        
        if (data != null) {
            data.updateMovement(event);
            detectionManager.checkMovement(player, data, event);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            
            PlayerData data = getPlayerData(attacker.getUniqueId());
            if (data != null) {
                data.updateCombat(event);
                detectionManager.checkCombat(attacker, data, event);
            }
        }
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }
    
    public DetectionManager getDetectionManager() {
        return detectionManager;
    }
    
    public AlertManager getAlertManager() {
        return alertManager;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public FileConfiguration getPluginConfig() {
        return config;
    }
}