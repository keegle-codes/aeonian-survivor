package me.loza.aeonian.config;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfigCommand implements CommandExecutor, Listener {

    private final PrefixHandler pref = new PrefixHandler();
    private final Aeonian plugin;

    public ConfigCommand(Aeonian plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(pref.getErrorPrefix() + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(createConfigInventory());
        return true;
    }

    public ItemStack createItem(Material mat, String name) {
        ItemStack configItem = new ItemStack(mat);
        ItemMeta meta = configItem.getItemMeta();
        meta.setDisplayName(name);
        List<String> lores = new ArrayList<>();
        boolean currentStatus = plugin.getConfig().getBoolean("features." + convertString(name), false);
        if (currentStatus) {
            lores.add(ChatColor.GREEN + "Enabled");
        } else {
            lores.add(ChatColor.RED + "Disabled");
        }
        meta.setLore(lores);
        configItem.setItemMeta(meta);
        return configItem;
    }

    public String convertString(String config) {
        return ChatColor.stripColor(config.toLowerCase().replace(' ', '_'));
    }

    public Inventory createConfigInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, "Survivor Config");
        List<ItemStack> configItems = new ArrayList<>();

        configItems.add(createItem(Material.IRON_SWORD, ChatColor.GOLD + "PvP"));
        configItems.add(createItem(Material.GOLDEN_BOOTS, ChatColor.GOLD + "Fall Damage"));
        configItems.add(createItem(Material.COOKED_BEEF, ChatColor.GOLD + "Hunger Decay"));
        configItems.add(createItem(Material.ENDER_CHEST, ChatColor.GOLD + "Ender Chest"));
        configItems.add(createItem(Material.SHIELD, ChatColor.GOLD + "Resistance"));
        configItems.add(createItem(Material.WRITABLE_BOOK, ChatColor.GOLD + "Messaging"));
        configItems.add(createItem(Material.LEATHER_HELMET, ChatColor.GOLD + "Team Message"));

        int i = 0;
        for (ItemStack configItem : configItems) {
            inv.setItem(i, configItem);
            i++;
        }
        return inv;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null) return;

        if (!event.getView().getTitle().equals("Survivor Config")) return;

        if (event.getWhoClicked().isOp()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;
            if (!clickedItem.hasItemMeta()) return;
            ItemMeta clickedMeta = clickedItem.getItemMeta();

            String configString = convertString(clickedMeta.getDisplayName());
            boolean newStatus = !(plugin.getConfig().getBoolean("features." + configString, false));

            plugin.getConfig().set("features." + configString, newStatus);
            plugin.saveConfig();

            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(createConfigInventory());
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (plugin.getConfig().getBoolean("features.pvp", false)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        boolean resistanceEnabled = plugin.getConfig().getBoolean("features.resistance", false);
        boolean fallDamageEnabled = plugin.getConfig().getBoolean("features.fall_damage", true);

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && !fallDamageEnabled) {
            event.setCancelled(true);
            return;
        }

        if (resistanceEnabled) {
            event.setDamage(0.0);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (plugin.getConfig().getBoolean("features.hunger_decay", true)) {
            return;
        }

        if (event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
            player.setSaturation(20f);
        }
    }

    @EventHandler
    public void onBlockedCommands(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }

        String cmd = event.getMessage().split(" ", 2)[0].substring(1).toLowerCase();
        if (cmd.contains(":")) {
            cmd = cmd.split(":", 2)[1];
        }

        if (cmd.equals("me")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(pref.getErrorPrefix() + "This command is not allowed");
        }

        boolean messagingDisabled = plugin.getConfig().getBoolean("features.messaging", true);
        boolean teamDisabled = plugin.getConfig().getBoolean("features.team_message", true);

        if (!messagingDisabled && (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("whisper") || cmd.equals("w") || cmd.equals("r") || cmd.equals("reply"))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(pref.getErrorPrefix() + "Private messaging is disabled");
            return;
        }

        if (!teamDisabled && (cmd.equals("teammsg") || cmd.equals("tm"))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(pref.getErrorPrefix() + "Team messaging is disabled");
        }
    }

}
