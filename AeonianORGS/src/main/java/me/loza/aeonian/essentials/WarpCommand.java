package me.loza.aeonian.essentials;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private static final String WARPS_PATH = "warps.";

    private final Aeonian plugin;

    public WarpCommand(Aeonian plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PrefixHandler textHandler = new PrefixHandler();

        if (!(sender instanceof Player) || !sender.isOp()) {
            sender.sendMessage(textHandler.getErrorPrefix() + "You don't have permission");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 2 && !args[1].equalsIgnoreCase("list")) {
            handleTwoArgs(player, textHandler, args);
        } else if (args.length == 1) {
            handleSingleArg(player, textHandler, args[0]);
        } else {
            sendUsage(player);
            sendListOfWarps(player, textHandler);
        }

        return true;
    }

    private void handleTwoArgs(Player player, PrefixHandler textHandler, String[] args) {
        String subCommand = args[0];
        String arg = args[1];

        if (subCommand.equalsIgnoreCase("add")) {
            Location existingWarp = plugin.getConfig().getLocation(WARPS_PATH + arg);
            if (existingWarp == null) {
                Location location = player.getLocation();
                plugin.getConfig().set(WARPS_PATH + arg, location);
                plugin.saveConfig();
                player.sendMessage(textHandler.getSurvPrefix() + "New warp created: " + ChatColor.RED + arg);
            } else {
                player.sendMessage(textHandler.getErrorPrefix() + "Warp with that name already exists");
            }
        } else if (subCommand.equalsIgnoreCase("remove")) {
            Location existingWarp = plugin.getConfig().getLocation(WARPS_PATH + arg);
            if (existingWarp != null) {
                plugin.getConfig().set(WARPS_PATH + arg, null);
                plugin.saveConfig();
                player.sendMessage(textHandler.getSurvPrefix() + "Warp removed: " + ChatColor.RED + arg);
            } else {
                player.sendMessage(textHandler.getErrorPrefix() + "Warp does not exist");
            }
        } else {
            Location warpLoc = plugin.getConfig().getLocation(WARPS_PATH + subCommand);
            if (warpLoc == null) {
                player.sendMessage(textHandler.getErrorPrefix() + "Warp does not exist");
                return;
            }

            Player targetPlayer = Bukkit.getPlayerExact(arg);
            if (targetPlayer == null) {
                player.sendMessage(textHandler.getErrorPrefix() + "That player is not online");
                return;
            }

            targetPlayer.teleport(warpLoc);
            player.sendMessage(textHandler.getSurvPrefix() + "Warped " + arg + " to " + subCommand);
            targetPlayer.sendMessage(textHandler.getSurvPrefix() + "Warped to: " + ChatColor.RED + subCommand);
        }
    }

    private void handleSingleArg(Player player, PrefixHandler textHandler, String arg) {
        if (!arg.equalsIgnoreCase("list")) {
            Location location = plugin.getConfig().getLocation(WARPS_PATH + arg);
            if (location != null) {
                player.teleport(location);
                player.sendMessage(textHandler.getSurvPrefix() + "Warped to: " + ChatColor.RED + arg);
            } else {
                player.sendMessage(textHandler.getErrorPrefix() + "Warp does not exist");
            }
        } else {
            sendListOfWarps(player, textHandler);
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Correct Usage: ");
        player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/warp [add/remove] <warp_name>");
        player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/warp list");
        player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/warp <warp_name> <player>");
    }

    public void sendListOfWarps(Player player, PrefixHandler textHandler) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("warps");
        if (section == null) {
            player.sendMessage(textHandler.getErrorPrefix() + "There are no warps set");
            return;
        }

        Set<String> warpNames = section.getKeys(false);
        StringBuilder warpList = new StringBuilder();
        warpList.append(textHandler.getSurvPrefix()).append("Warps: ");

        for (String warpName : warpNames) {
            warpList.append(ChatColor.RED).append(warpName).append(ChatColor.GRAY).append(", ");
        }

        player.sendMessage(warpList.toString());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !command.getName().equalsIgnoreCase("warp")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();

            addIfStartsWith(completions, "add", partial);
            addIfStartsWith(completions, "remove", partial);
            addIfStartsWith(completions, "list", partial);

            for (String warp : getWarpNames()) {
                if (warp.toLowerCase().startsWith(partial)) {
                    completions.add(warp);
                }
            }

        } else if (args.length == 2) {
            String firstArg = args[0];
            String partial = args[1].toLowerCase();

            if (firstArg.equalsIgnoreCase("remove")) {
                for (String warp : getWarpNames()) {
                    if (warp.toLowerCase().startsWith(partial)) {
                        completions.add(warp);
                    }
                }
            } else if (!firstArg.equalsIgnoreCase("add") && !firstArg.equalsIgnoreCase("list")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    String name = online.getName();
                    if (name.toLowerCase().startsWith(partial)) {
                        completions.add(name);
                    }
                }
            }
        }

        return completions;
    }

    private List<String> getWarpNames() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("warps");
        if (section == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    private void addIfStartsWith(List<String> list, String value, String partialLower) {
        if (value.toLowerCase().startsWith(partialLower)) {
            list.add(value);
        }
    }
}
