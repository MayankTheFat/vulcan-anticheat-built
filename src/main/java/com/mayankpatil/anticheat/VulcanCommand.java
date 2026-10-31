package com.mayankpatil.anticheat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * VulcanCommand - Handles plugin commands
 */
public class VulcanCommand implements CommandExecutor, TabCompleter {
    
    private final VulcanAntiCheat plugin;
    
    public VulcanCommand(VulcanAntiCheat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;
                
            case "reload":
                reloadCommand(sender);
                break;
                
            case "alerts":
                alertsCommand(sender, args);
                break;
                
            case "bypass":
                bypassCommand(sender, args);
                break;
                
            case "violations":
                violationsCommand(sender, args);
                break;
                
            case "reset":
                resetCommand(sender, args);
                break;
                
            case "info":
                infoCommand(sender);
                break;
                
            case "debug":
                debugCommand(sender, args);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /vulcan help for help.");
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== Vulcan AntiCheat Help =====");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan help" + ChatColor.GRAY + " - Shows this help message");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan reload" + ChatColor.GRAY + " - Reloads the configuration");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan alerts" + ChatColor.GRAY + " - Toggle cheat alerts");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan bypass" + ChatColor.GRAY + " - Toggle bypass mode");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan violations <player>" + ChatColor.GRAY + " - Check player violations");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan reset <player>" + ChatColor.GRAY + " - Reset player violations");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan info" + ChatColor.GRAY + " - Shows plugin information");
        sender.sendMessage(ChatColor.YELLOW + "/vulcan debug" + ChatColor.GRAY + " - Toggle debug mode");
        
        if (sender.hasPermission("vulcan.admin")) {
            sender.sendMessage(ChatColor.GOLD + "\nAdmin Commands:");
            sender.sendMessage(ChatColor.YELLOW + "/vulcan debug <player>" + ChatColor.GRAY + " - Debug specific player");
        }
    }
    
    private void reloadCommand(CommandSender sender) {
        if (!sender.hasPermission("vulcan.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload the configuration.");
            return;
        }
        
        plugin.reloadConfig();
        plugin.getDetectionManager().loadConfig();
        plugin.getAlertManager().loadConfig();
        plugin.getPunishmentManager().loadConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        plugin.getLogger().info("Configuration reloaded by " + sender.getName());
    }
    
    private void alertsCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("vulcan.alerts")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to receive alerts.");
            return;
        }
        
        plugin.getAlertManager().toggleAlerts(player);
    }
    
    private void bypassCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("vulcan.bypass")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to bypass checks.");
            return;
        }
        
        plugin.getAlertManager().toggleBypass(player);
    }
    
    private void violationsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vulcan.violations")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to check violations.");
            return;
        }
        
        Player target = null;
        
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Please specify a player: /vulcan violations <player>");
            return;
        }
        
        PlayerData data = plugin.getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(ChatColor.RED + "No data found for player: " + target.getName());
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "===== Violations for " + target.getName() + " =====");
        sender.sendMessage(ChatColor.YELLOW + "Fly: " + ChatColor.WHITE + data.getViolations("fly"));
        sender.sendMessage(ChatColor.YELLOW + "Speed: " + ChatColor.WHITE + data.getViolations("speed"));
        sender.sendMessage(ChatColor.YELLOW + "KillAura: " + ChatColor.WHITE + data.getViolations("killaura"));
        sender.sendMessage(ChatColor.YELLOW + "Reach: " + ChatColor.WHITE + data.getViolations("reach"));
        sender.sendMessage(ChatColor.YELLOW + "AutoClick: " + ChatColor.WHITE + data.getViolations("autoclick"));
        
        int total = data.getViolations("fly") + data.getViolations("speed") + 
                   data.getViolations("killaura") + data.getViolations("reach") + 
                   data.getViolations("autoclick");
        sender.sendMessage(ChatColor.GOLD + "Total Violations: " + ChatColor.WHITE + total);
    }
    
    private void resetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vulcan.reset")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to reset violations.");
            return;
        }
        
        Player target = null;
        
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Please specify a player: /vulcan reset <player>");
            return;
        }
        
        plugin.getPunishmentManager().resetViolations(target);
        sender.sendMessage(ChatColor.GREEN + "Violations reset for " + target.getName());
    }
    
    private void infoCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== Vulcan AntiCheat Info =====");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + "1.0.0");
        sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.WHITE + "MayankPatil3");
        sender.sendMessage(ChatColor.YELLOW + "Built From: " + ChatColor.WHITE + "Scratch");
        sender.sendMessage(ChatColor.YELLOW + "Enabled Checks: " + ChatColor.WHITE + 
            plugin.getDetectionManager().getEnabledChecks().size());
        sender.sendMessage(ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE + 
            Bukkit.getOnlinePlayers().size());
        sender.sendMessage(ChatColor.YELLOW + "Alerts Enabled: " + ChatColor.WHITE + 
            plugin.getAlertManager().isAlertsEnabled());
        sender.sendMessage(ChatColor.YELLOW + "Punishments Enabled: " + ChatColor.WHITE + 
            plugin.getPunishmentManager().isPunishmentsEnabled());
    }
    
    private void debugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vulcan.debug")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use debug mode.");
            return;
        }
        
        // TODO: Implement debug mode
        sender.sendMessage(ChatColor.YELLOW + "Debug mode is not yet implemented.");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main command completions
            List<String> commands = Arrays.asList("help", "reload", "alerts", "bypass", "violations", "reset", "info", "debug");
            for (String cmd : commands) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            // Player name completions for violations and reset
            if (args[0].equalsIgnoreCase("violations") || args[0].equalsIgnoreCase("reset")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}