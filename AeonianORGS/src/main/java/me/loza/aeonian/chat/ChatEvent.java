package me.loza.aeonian.chat;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;


public class ChatEvent implements Listener {

    PrefixHandler prefixHandler = new PrefixHandler();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Team playerTeam = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(event.getPlayer().getName());

        ChatColor playerColor = ChatColor.WHITE;
        String prefix = "";
        String suffix = "";

        if (playerTeam != null) {

            if (playerTeam.getColor() != null) {
                playerColor = playerTeam.getColor();
            }

            if (playerTeam.getPrefix() != null) {
                prefix = playerTeam.getPrefix();
            }

            if (playerTeam.getSuffix() != null) {
                suffix = playerTeam.getSuffix();
            }
        }

        String formattedName = prefix + playerColor + event.getPlayer().getName() + suffix;
        if (event.getPlayer().isOp()) {
            event.setFormat(ChatColor.DARK_GRAY + "(" + ChatColor.RED + "OP" + ChatColor.DARK_GRAY + ") " + formattedName + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + event.getMessage());
            return;
        }
        if (Aeonian.chat) {
            event.setFormat(formattedName + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + event.getMessage());
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(prefixHandler.getErrorPrefix() + "Chat is currently off!");
        }
    }
}

