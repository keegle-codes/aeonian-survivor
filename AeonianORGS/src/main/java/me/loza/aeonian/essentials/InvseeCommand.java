package me.loza.aeonian.essentials;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class InvseeCommand implements CommandExecutor, Listener {

    private final PrefixHandler prefixHandler = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Only players can use this command!");
            return true;
        }

        Player viewer = (Player) sender;

        if (!viewer.isOp()) {
            viewer.sendMessage(prefixHandler.getErrorPrefix() + "No permission!");
            return true;
        }

        if (args.length != 1) {
            viewer.sendMessage(prefixHandler.getSurvPrefix() + "Correct Usage:");
            viewer.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/invsee <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            viewer.sendMessage(prefixHandler.getErrorPrefix() + "That player is not online!");
            return true;
        }

        viewer.openInventory(target.getInventory());

        viewer.sendMessage(ChatColor.GRAY + "Viewing " + ChatColor.RED + target.getName() + ChatColor.GRAY + "'s inventory.");
        return true;
    }

}