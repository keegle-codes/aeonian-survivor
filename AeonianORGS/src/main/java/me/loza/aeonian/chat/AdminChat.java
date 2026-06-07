package me.loza.aeonian.chat;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminChat implements CommandExecutor {

    PrefixHandler pref = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(pref.getErrorPrefix() + "Usage: /ac <message>");
            return true;
        }

        String message = String.join(" ", args);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(pref.getACPrefix() + ChatColor.RED + sender.getName() + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + message);
            }
        }

        return true;
    }
}
