package me.loza.aeonian.helpop;

import me.loza.aeonian.handlers.PrefixHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpopCommand implements CommandExecutor {
    private final PrefixHandler prefixHandler = new PrefixHandler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Correct Usage:");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/helpop <message>");
            return true;
        }

        StringBuilder helpop = new StringBuilder();
        for (String arg : args) {
            helpop.append(arg).append(" ");
        }
        String raw = helpop.toString().trim();

        int id = HelpopManager.createHelpop(player.getUniqueId());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                TextComponent helpopMessage = new TextComponent(prefixHandler.getHelpopPrefix()
                        + ChatColor.DARK_GRAY + "[#" + id + "] "
                        + ChatColor.RED + player.getName()
                        + ChatColor.DARK_GRAY + " » "
                        + ChatColor.GRAY + raw);

                helpopMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/hr " + id + " "));
                helpopMessage.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.DARK_RED + "Click to auto-fill reply command").create()
                ));

                p.spigot().sendMessage(helpopMessage);
            }
        }

        player.sendMessage(prefixHandler.getHelpopPrefix() + ChatColor.GRAY + "Helpop sent! " + ChatColor.DARK_GRAY + "(#" + id + ")");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " » " + raw);

        return true;
    }
}
