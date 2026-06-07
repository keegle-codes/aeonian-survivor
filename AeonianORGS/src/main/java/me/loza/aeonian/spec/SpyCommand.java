package me.loza.aeonian.spec;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpyCommand implements CommandExecutor, Listener {

    private final PrefixHandler pref = new PrefixHandler();
    private final Aeonian plugin;
    private final Set<UUID> spyingPlayers = new HashSet<>();

    public SpyCommand(Aeonian plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1 || !(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
            player.sendMessage(pref.getErrorPrefix() + "Usage: /commandspy <on|off>");
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            spyingPlayers.add(player.getUniqueId());
            player.sendMessage(pref.getSurvPrefix() + "Command spy enabled.");
        } else {
            spyingPlayers.remove(player.getUniqueId());
            player.sendMessage(pref.getSurvPrefix() + "Command spy disabled.");
        }

        return true;
    }
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        for (UUID uuid : spyingPlayers) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null && !spy.equals(sender)) {
                spy.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "[Spy] " + sender.getName() + ": " + message);
            }
        }
    }
}
