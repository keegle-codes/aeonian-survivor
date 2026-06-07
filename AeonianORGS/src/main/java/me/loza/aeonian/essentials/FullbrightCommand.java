package me.loza.aeonian.essentials;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FullbrightCommand implements CommandExecutor {
    private final Aeonian plugin;
    PrefixHandler pref = new PrefixHandler();

    public FullbrightCommand(Aeonian plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(pref.getSurvPrefix() + "Night vision has been disabled.");
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
            player.sendMessage(pref.getSurvPrefix() + "Night vision has been enabled.");
        }

        return true;
    }
}
