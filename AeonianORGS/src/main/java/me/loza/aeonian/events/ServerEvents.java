package me.loza.aeonian.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class ServerEvents implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final List<String> motdMessages = List.of(
            "When is Avatar III?",
            "I miss villain Spongey...",
            "Another zCent season? Ugh.",
            "Keegle coding bug? No one was surprised.",
            "Mom help, I got lost in the Arcade.",
            "Leave Bones in the Arcade and never turn back.",
            "The Golden Coconut was the friends we made.",
            "If only CHONGA could spell...",
            "The Rodeo glaze is crazy!",
            "Ryfri teleporting all entities moment.",
            "The Candy crazies are going around.",
            "S.A.M. is taking our jobs!",
            "Microus made merge? Yeah, okay bud.",
            "So, is zCent still evil?",
            "Rob narration was 10/10.",
            "The whole crew misses Avatar Purp.",
            "But seasons don't even change in Minecraft?",
            "There is no Season 8 in Ba Sing Se",
            "Zoob island 4th edition when?",
            "Get CHOCK an idol man..."
    );

    public ServerEvents(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String randomPick = motdMessages.get(random.nextInt(motdMessages.size()));
        String motd = colorize("&8» &c&lAeonian ORGS: &7Currently in 1.21.10 &8«\n&e&o" + randomPick );

        event.setMotd(motd);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateTabFor(event.getPlayer());
            updateTabForAll();
        }, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, this::updateTabForAll, 5L);
    }

    private void updateTabFor(Player player) {
        String header = colorize("&8» &c&lAeonian ORGS &8«");

        String footer = colorize("&7Online: &f" + Bukkit.getOnlinePlayers().size() + " &7/ &f" + Bukkit.getMaxPlayers() + "\n" +
                        "&f/echest&7, &f/fullbright&7, &f/helpop&7, &f/seasons");

        player.setPlayerListHeaderFooter(header, footer);
    }

    private void updateTabForAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateTabFor(p);
        }
    }

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
