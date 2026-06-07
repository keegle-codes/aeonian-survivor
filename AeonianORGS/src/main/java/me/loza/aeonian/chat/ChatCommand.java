package me.loza.aeonian.chat;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PrefixHandler pref = new PrefixHandler();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("on")) {
                    Aeonian.chat = true;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(pref.getSurvPrefix() + "Chat has been turned on!");
                    }
                } else if (args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("false") || args[0].equalsIgnoreCase("off")) {
                    Aeonian.chat = false;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(pref.getSurvPrefix() + "Chat has been turned off!");
                    }
                }
            } else {
                player.sendMessage(ChatColor.GOLD + "Correct Usage: ");
                player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/chat <on/off>");
            }
            return true;
        }
        return false;
    }
}