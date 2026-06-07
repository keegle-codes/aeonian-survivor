package me.loza.aeonian.spec;

import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class SpecEvent implements Listener {

    private final PrefixHandler pref = new PrefixHandler();

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;

        Player viewer = event.getPlayer();
        Player target = (Player) event.getRightClicked();

        if (viewer.getGameMode() == GameMode.SPECTATOR) {
            viewer.openInventory(target.getInventory());
        }
    }

}
