package me.loza.aeonian.essentials;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullCommand implements CommandExecutor {
    PrefixHandler pref = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Must be a player to use this command!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(pref.getErrorPrefix() + "Usage: /skull <player>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta == null) {
            player.sendMessage(pref.getErrorPrefix() + "Failed to create player head.");
            return true;
        }

        meta.setOwningPlayer(target);

        skull.setItemMeta(meta);

        player.getInventory().addItem(skull);

        player.sendMessage(pref.getSurvPrefix() + "Given the skull of " + ChatColor.YELLOW + targetName);
        return true;
    }
}
