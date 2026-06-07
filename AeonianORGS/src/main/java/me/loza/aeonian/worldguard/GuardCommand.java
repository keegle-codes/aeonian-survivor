package me.loza.aeonian.worldguard;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class GuardCommand implements CommandExecutor {
    PrefixHandler pref = new PrefixHandler();
    private final Aeonian plugin;

    public GuardCommand(Aeonian plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            sendCorrectUsage(player, pref);
            return true;
        }
        if ((args[0].equalsIgnoreCase("add")) && (args.length >= 3)) {
            if (plugin.getConfig().getLocation("guard." + args[1] + ".location") == null) {
                String locPath = "guard." + args[1] + ".location";
                String radPath = "guard." + args[1] + ".radius";
                Location newLoc = player.getLocation();
                plugin.getConfig().set(locPath, newLoc);
                plugin.getConfig().set(radPath, Integer.valueOf(args[2]));
                player.sendMessage(pref.getSurvPrefix() + "New guarded area " + ChatColor.RED + args[1] + ChatColor.GRAY + " added with radius " + ChatColor.RED + args[2]);
                plugin.saveConfig();
            } else {
                player.sendMessage(pref.getErrorPrefix() + "A region with that name already exists!");
            }
        } else if ((args[0].equalsIgnoreCase("remove"))  && (args.length >= 2)) {
            if (plugin.getConfig().getLocation("guard." + args[1] + ".location") != null) {
                String locPath = "guard." + args[1] + ".location";
                String radPath = "guard." + args[1] + ".radius";
                plugin.getConfig().set(locPath, null);
                plugin.getConfig().set(radPath, null);
                plugin.getConfig().set("guard." + args[1], null);
                player.sendMessage(pref.getSurvPrefix() + "Guarded area " + ChatColor.RED + args[1] + ChatColor.GRAY + " removed!");
                plugin.saveConfig();
            } else {
                player.sendMessage(pref.getErrorPrefix() + "A region with that name doesn't exist!");
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            sendGuards(player, pref);
        } else {
            sendCorrectUsage(player, pref);
        }
        return true;
    }

    public void sendGuards(Player player, PrefixHandler pref) {
        Set<String> guardNames;
        if (plugin.getConfig().getConfigurationSection("guard") != null) {
            guardNames = plugin.getConfig().getConfigurationSection("guard").getKeys(false);
        } else {
            player.sendMessage(pref.getErrorPrefix() + "There are no guard names set");
            return;
        }
        StringBuilder guardList = new StringBuilder();
        guardList.append(pref.getSurvPrefix() + "Guard Names: ");
        for (String warpName : guardNames) {
            guardList.append(ChatColor.RED + warpName + ChatColor.GRAY + ", ");
        }
        player.sendMessage(guardList.toString());
    }

    public void sendCorrectUsage(Player player, PrefixHandler pref) {
        player.sendMessage(pref.getErrorPrefix() + "Correct Usage: ");
        player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/wg [add/remove] <name> <radius>");
        player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/wg list");
    }
}
