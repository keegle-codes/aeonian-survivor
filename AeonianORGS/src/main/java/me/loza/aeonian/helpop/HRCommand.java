package me.loza.aeonian.helpop;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HRCommand implements CommandExecutor {
    private final PrefixHandler prefixHandler = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Correct Usage:");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/hr <number> <message>");
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That helpop number is invalid!");
            return true;
        }

        UUID ownerId = HelpopManager.getHelpopOwner(id);
        if (ownerId == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That helpop (#" + id + ") no longer exists!");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(ownerId);
        if (targetPlayer == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That player is not online!");
            return true;
        }

        StringBuilder helpop = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            helpop.append(args[i]).append(" ");
        }
        String raw = helpop.toString().trim();

        // Send reply to the original helpop sender
        targetPlayer.sendMessage(prefixHandler.getHelpopReplyPrefix()
                + ChatColor.DARK_GRAY + "[#" + id + "] "
                + ChatColor.RED + player.getName()
                + ChatColor.DARK_GRAY + " » "
                + ChatColor.GRAY + raw);

        // Same op logging behavior as your original
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + player.getName() + " » " + targetPlayer.getName() + " (#" + id + ") " + raw);
            }
        }

        // Optional: close the ticket after reply (keeps it simple)
        HelpopManager.removeHelpop(id);

        return true;
    }
}
