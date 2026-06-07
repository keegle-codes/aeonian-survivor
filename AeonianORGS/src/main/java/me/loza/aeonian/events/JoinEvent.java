package me.loza.aeonian.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ChatColor color = ChatColor.GRAY;
        if (event.getPlayer().isOp()) {
            color = ChatColor.RED;
        }
        event.setJoinMessage(color + event.getPlayer().getName() + ChatColor.DARK_GRAY + " (" + ChatColor.GREEN + "+" + ChatColor.DARK_GRAY + ")");
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ChatColor color = ChatColor.GRAY;
        if (event.getPlayer().isOp()) {
            color = ChatColor.RED;
        }
        event.setQuitMessage(color + event.getPlayer().getName() + ChatColor.DARK_GRAY + " (" + ChatColor.RED + "-" + ChatColor.DARK_GRAY + ")");
    }
}
