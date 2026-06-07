package me.loza.aeonian.essentials;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EnderCommand implements CommandExecutor {
    PrefixHandler pref = new PrefixHandler();
    private final Aeonian plugin;

    public EnderCommand(Aeonian plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Only players can use this comamnd!");
            return true;
        }
        Player player = (Player) sender;
        if (!(plugin.getConfig().getBoolean("features.ender_chest", true)) && !(player.isOp())) {
            player.sendMessage(pref.getErrorPrefix() + "Ender chests are currently disabled");
            return true;
        }

        if (args.length < 1) {
            Inventory echest = player.getEnderChest();
            player.openInventory(echest);
        } else {
            if (player.isOp()) {
                if (Bukkit.getPlayer(args[0]) != null) {
                    Inventory echest = Bukkit.getPlayer(args[0]).getEnderChest();
                    player.openInventory(echest);
                    player.sendMessage(pref.getSurvPrefix() + "Opened " + args[0] + " ender chest!");
                } else {
                    player.sendMessage(pref.getErrorPrefix() + "That player is not online!");
                }
            } else {
                player.sendMessage(pref.getErrorPrefix() + "Only staff can open other player's ender chests");
            }
        }
        return true;
    }
}