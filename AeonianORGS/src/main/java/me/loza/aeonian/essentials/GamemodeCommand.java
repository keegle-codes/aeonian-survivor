package me.loza.aeonian.essentials;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private final Aeonian plugin;
    private final PrefixHandler prefixHandler = new PrefixHandler();

    public GamemodeCommand(Aeonian plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String cmd = command.getName().toLowerCase();
        GameMode mode;

        switch (cmd) {
            case "gmc":
                mode = GameMode.CREATIVE;
                break;
            case "gms":
                mode = GameMode.SURVIVAL;
                break;
            case "gma":
                mode = GameMode.ADVENTURE;
                break;
            case "gmsp":
                mode = GameMode.SPECTATOR;
                break;
            default:
                // Should never happen if only bound to these commands
                sender.sendMessage(prefixHandler.getErrorPrefix() + "Unknown gamemode command.");
                return true;
        }

        Player target;

        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(prefixHandler.getErrorPrefix() + "That player is not online.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefixHandler.getErrorPrefix() + "Console must specify a player.");
                return true;
            }
            target = (Player) sender;
        }

        target.setGameMode(mode);

        String modeName = mode.name().toLowerCase();

        if (sender.equals(target)) {
            sender.sendMessage(prefixHandler.getSurvPrefix() + "Your gamemode has been set to " + modeName);
        } else {
            sender.sendMessage(prefixHandler.getSurvPrefix() + "Set gamemode of " + target.getName() + " to " + modeName);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        if (!(cmd.equals("gmc") || cmd.equals("gms") || cmd.equals("gma") || cmd.equals("gmsp"))) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (Player online : Bukkit.getOnlinePlayers()) {
                String name = online.getName();
                if (name.toLowerCase().startsWith(partial)) {
                    completions.add(name);
                }
            }

            return completions;
        }

        return Collections.emptyList();
    }
}