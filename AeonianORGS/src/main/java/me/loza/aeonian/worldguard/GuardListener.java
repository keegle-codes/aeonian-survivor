package me.loza.aeonian.worldguard;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class GuardListener implements Listener {
    private final Aeonian plugin;
    private final PrefixHandler pref = new PrefixHandler();

    public GuardListener(Aeonian plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();
        if (isInGuardedArea(blockLoc) && player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            player.sendMessage(pref.getErrorPrefix() + "You cannot place blocks in a guarded area!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();
        if (isInGuardedArea(blockLoc) && player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            player.sendMessage(pref.getErrorPrefix() + "You cannot break blocks in a guarded area!");
        }
    }

    private boolean isInGuardedArea(Location loc) {
        ConfigurationSection guardSection = plugin.getConfig().getConfigurationSection("guard");
        if (guardSection == null) return false;

        for (String name : guardSection.getKeys(false)) {
            Location guardLoc = plugin.getConfig().getLocation("guard." + name + ".location");
            int radius = plugin.getConfig().getInt("guard." + name + ".radius");

            if (guardLoc.getWorld().equals(loc.getWorld()) && guardLoc.distance(loc) <= radius) {
                return true;
            }
        }
        return false;
    }
}
