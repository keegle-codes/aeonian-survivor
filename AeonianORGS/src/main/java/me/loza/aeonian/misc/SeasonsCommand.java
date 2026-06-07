package me.loza.aeonian.misc;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SeasonsCommand implements CommandExecutor, Listener {

    private final PrefixHandler prefixHandler = new PrefixHandler();
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Seasons";

    private static class Season {
        final int number;
        final String theme;
        final int castSize;
        final String hosts;
        final String date;
        final String winner;
        final Material icon;

        Season(int number, String theme, int castSize, String hosts, String date, String winner, Material icon) {
            this.number = number;
            this.theme = theme;
            this.castSize = castSize;
            this.hosts = hosts;
            this.date = date;
            this.winner = winner;
            this.icon = icon;
        }
    }

    private static final List<Season> SEASONS = Arrays.asList(
            new Season(1, "The Jungle", 16, "Ryfri", "30 Dec. 2017", "Spongey", Material.JUNGLE_LEAVES),
            new Season(2, "Four Seas", 16, "Keegle & Spongey", "13 Jan. 2018", "MajorWoof", Material.WATER_BUCKET),
            new Season(3, "Cold Shoulder", 18, "Austin & Keegle", "17 Feb. 2018", "Jared", Material.PACKED_ICE),
            new Season(4, "Desert Oasis", 21, "Spongey", "10 Mar. 2018", "dontbow", Material.RED_SAND),
            new Season(5, "Exile Island", 18, "CHANGA & Dahii", "7 Apr. 2018", "Merc", Material.OAK_BOAT),
            new Season(6, "HvV", 18, "Ryfri & Phoraxe", "2 Jun. 2018", "Spongey", Material.DIAMOND_SWORD),
            new Season(7, "The Tropics", 21, "Keegle & Swishduck", "30 Jun. 2018", "Peridot", Material.MELON_SLICE),
            new Season(8, "Inferior", 15, "Jared", "10 Aug. 2018", "Zevulpes", Material.MUD),
            new Season(9, "Seasons Change", 20, "Spongey & Dahii", "15 Sep. 2018", "Bjr", Material.CLOCK),
            new Season(10, "Space Legacy", 18, "Keegle & Ryfri", "10 Nov. 2018", "Lucity", Material.ENDER_EYE),
            new Season(11, "Winter is Coming", 20, "Theb & CHANGA", "27 Dec. 2018", "KOKE", Material.SNOWBALL),
            new Season(12, "David vs Goliath", 20, "Zevulpes & Lucity", "26 Jan. 2019", "Andy", Material.ANVIL),
            new Season(13, "Avatar", 20, "Keegle & Spongey", "9 Mar. 2019", "Purpdan", Material.ELYTRA),
            new Season(14, "Apocalypse", 19, "Garrett & Keegle", "27 Apr. 2019", "Tuxpeng", Material.NETHERRACK),
            new Season(15, "Olympians", 20, "Spongey & Andy", "1 Jun. 2019", "Keegle", Material.GOLDEN_HELMET),
            new Season(16, "Reunion", 18, "Swishduck & Keegle", "14 Jul. 2019", "zCent", Material.FIREWORK_ROCKET),
            new Season(17, "Rodeo", 21, "Peridot & Pie", "24 Aug. 2019", "Spongey", Material.SADDLE),
            new Season(18, "Atlantis", 18, "zCent", "21 Sep. 2019", "Bizzlebub", Material.TRIDENT),
            new Season(19, "The Mines", 16, "Keegle", "9 Nov. 2019", "PotaTomas", Material.IRON_PICKAXE),
            new Season(20, "Ghost Island", 21, "Spongey", "21 Dec. 2019", "Flameh", Material.SOUL_LANTERN),
            new Season(21, "Winning is Everything", 21, "Theb", "18 Jan. 2020", "Dahii", Material.NETHER_STAR),
            new Season(22, "Love is Blind", 21, "Garrett & Keegle", "8 Feb. 2020", "Dahii", Material.BOW),
            new Season(23, "World War", 20, "Swishduck & zCent", "21 Mar. 2020", "Bliv", Material.TNT),
            new Season(24, "Arcade", 20, "Spongey & Keegle", "9 May. 2020", "zCent", Material.REDSTONE),
            new Season(25, "Heaven", 18, "xRoss & Bizzlebub", "20 Jun. 2020", "Tuxpeng", Material.QUARTZ),
            new Season(26, "Alchemy", 18, "Keegle", "14 Aug. 2020", "Spongey", Material.BREWING_STAND),
            new Season(27, "The Election", 18, "Theb & Spongey", "26 Sep. 2020", "Purpdan", Material.WRITABLE_BOOK),
            new Season(28, "Halloween", 18, "zCent", "24 Oct. 2020", "CHOCK", Material.JACK_O_LANTERN),
            new Season(29, "Avatar 2", 25, "Spongey & Keegle", "26 Dec. 2020", "CHANGA", Material.HEART_OF_THE_SEA),
            new Season(30, "Legacy", 18, "Keegle", "26 Jun. 2021", "Swishduck", Material.ENCHANTED_BOOK),
            new Season(31, "Around the World", 20, "zCent", "7 Aug. 2021", "Bobby", Material.MAP),
            new Season(32, "Eden", 18, "Spongey", "26 Feb. 2022", "SimplySam", Material.CHERRY_SAPLING),
            new Season(33, "HvV 2", 18, "Dahii", "14 May. 2022", "Zeka", Material.SHIELD),
            new Season(34, "Pirates Cove", 18, "Ryfri & Colin", "1 Apr. 2023", "zCent", Material.SPYGLASS),
            new Season(35, "Arcade 2", 20, "Keegle & Spongey", "24 Jun. 2023", "Miles", Material.REDSTONE_BLOCK),
            new Season(36, "Spellbound", 18, "zCent & Swishduck", "12 Aug. 2023", "Bacan", Material.ENCHANTING_TABLE),
            new Season(37, "Golden Coconut", 20, "zCent & Swishduck", "16 Mar. 2024", "Type", Material.GOLDEN_APPLE),
            new Season(38, "Brains Brawn Beauty", 21, "zCent", "22 Jun. 2024", "Bobby", Material.BOOK),
            new Season(39, "The Bunker", 18, "Keegle", "7 Sep. 2024", "Ryfri", Material.IRON_DOOR),
            new Season(40, "Calamity", 24, "Keegle, zCent, and Spongey", "7 Dec. 2024", "Decodin", Material.CRYING_OBSIDIAN),
            new Season(41, "Alone Together", 20, "Keegle, zCent, and Bub", "3 May. 2025", "Arod", Material.LEAD)
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);

        for (Season s : SEASONS) {
            ItemStack item = new ItemStack(s.icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Season " + s.number + ChatColor.DARK_GRAY + " — " + ChatColor.YELLOW + s.theme);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_GRAY + " ");
                lore.add(ChatColor.GRAY + "Theme: " + ChatColor.WHITE + s.theme);
                lore.add(ChatColor.GRAY + "Cast Size: " + ChatColor.WHITE + s.castSize);
                lore.add(ChatColor.GRAY + "Host(s): " + ChatColor.WHITE + s.hosts);
                lore.add(ChatColor.GRAY + "Date: " + ChatColor.WHITE + s.date);
                lore.add(ChatColor.GRAY + "Winner: " + ChatColor.WHITE + s.winner);

                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            int slot = Math.max(0, Math.min(inv.getSize() - 1, s.number - 1));
            inv.setItem(slot, item);
        }

        player.openInventory(inv);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null) return;

        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);
    }
}