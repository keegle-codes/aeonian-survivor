package me.loza.aeonian.essentials;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UtilityCommands implements CommandExecutor {

    PrefixHandler pref = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Only players can use this command!");
            return true;
        }

        Player commander = (Player) sender;

        if (args.length != 1) {
            applyAction(commander, command.getName());
            if (label.equalsIgnoreCase("heal")) {
                sender.sendMessage(pref.getSurvPrefix() + "You healed yourself");
            } else if (label.equalsIgnoreCase("feed")) {
                sender.sendMessage(pref.getSurvPrefix() + "You fed yourself");
            } else {
                sender.sendMessage(pref.getSurvPrefix() + "You cleared the inventory of yourself");
            }
            return true;
        }

        String target = args[0];

        if (target.equals("*")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                applyAction(p, command.getName());
            }

            if (label.equalsIgnoreCase("heal")) {
                sender.sendMessage(pref.getSurvPrefix() + "You healed all players");
            } else if (label.equalsIgnoreCase("feed")) {
                sender.sendMessage(pref.getSurvPrefix() + "You fed all players");
            } else {
                sender.sendMessage(pref.getSurvPrefix() + "You cleared all player's inventories");
            }
            return true;
        }

        Player player = Bukkit.getPlayerExact(target);

        if (player == null) {
            sender.sendMessage(pref.getErrorPrefix() + "Player not found.");
            return true;
        }

        applyAction(player, command.getName());
        if (label.equalsIgnoreCase("heal")) {
            sender.sendMessage(pref.getSurvPrefix() + "You healed " + player.getName());
        } else if (label.equalsIgnoreCase("feed")) {
            sender.sendMessage(pref.getSurvPrefix() + "You fed " + player.getName());
        } else {
            sender.sendMessage(pref.getSurvPrefix() + "You cleared the inventory of " + player.getName());
        }

        return true;
    }

    private void applyAction(Player player, String cmd) {

        switch (cmd.toLowerCase()) {

            case "heal":
                player.setHealth(player.getMaxHealth());
                break;

            case "feed":
                player.setFoodLevel(20);
                player.setSaturation(20f);
                break;

            case "ci":
                player.getInventory().clear();
                break;
        }
    }
}