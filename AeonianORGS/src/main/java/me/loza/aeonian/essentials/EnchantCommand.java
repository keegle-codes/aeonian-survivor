package me.loza.aeonian.essentials;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantCommand implements CommandExecutor {
    PrefixHandler pref = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /enchant <efficiency> <amt>
        if (!(sender instanceof Player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(pref.getErrorPrefix() + "Correct Usage: /enchant <enchantment> <level>");
            return true;
        }

        int level = -1;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(pref.getErrorPrefix() + "Not a valid number!");
            return true;
        }

        if (level < 1) {
            player.sendMessage(pref.getErrorPrefix() + "Enchantment level must be at least 1!");
            return true;
        }

        Enchantment enchant = Enchantment.getByName(args[0].toLowerCase());
        if (enchant == null) {
            player.sendMessage(pref.getErrorPrefix() + "Invalid enchantment name.");
            return true;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if ((mainHand != null && mainHand.getType() != Material.AIR)) {
            player.getInventory().getItemInMainHand().addUnsafeEnchantment(enchant, level);
            player.sendMessage(pref.getSurvPrefix() + "Added enchantment " + enchant.getName() + " " + level + " to your item.");
        } else if ((offHand != null && offHand.getType() != Material.AIR)) {
            player.getInventory().getItemInOffHand().addUnsafeEnchantment(enchant, level);
            player.sendMessage(pref.getSurvPrefix() + "Added enchantment " + enchant.getName() + " " + level + " to your item.");
        } else {
            player.sendMessage(pref.getErrorPrefix() + "You must be holding something to enchant!");
        }
        return true;
    }
}
