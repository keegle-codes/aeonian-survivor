package me.loza.aeonian.tribes;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class VoteCommand implements CommandExecutor, Listener, TabCompleter {
    private final PrefixHandler prefixHandler = new PrefixHandler();
    private final Aeonian plugin;
    private final Scoreboard scoreboard;

    private final String VOTE_GUI_TITLE = ChatColor.DARK_GRAY + "Vote: Choose a Player";
    private final String LIST_GUI_TITLE = ChatColor.DARK_GRAY + "Votes List";
    private final String DETAILS_GUI_PREFIX = ChatColor.DARK_GRAY + "Vote Details #";

    private final Set<String> allowVoteGuiClose = new HashSet<>();

    public VoteCommand(Aeonian plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1) {
            if (player.isOp()) {
                player.sendMessage(prefixHandler.getSurvPrefix() + "Correct Usage:");
                player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/vote start <gui/custom> <tribe>");
                player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/vote list <reset>");
            } else {
                player.sendMessage(prefixHandler.getErrorPrefix() + "Correct Usage: /vote <name>");
            }
            return true;
        }

        String subCmd = args[0].toLowerCase();
        if (subCmd.equalsIgnoreCase("start")) {
            startVote(args, player);
        } else if (subCmd.equalsIgnoreCase("list")) {
            listVote(args, player);
        } else {
            castVote(args, player);
        }
        return true;
    }

    public void startVote(String[] args, Player player) {
        if (!player.isOp()) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You do not have permission to do that.");
            return;
        }
        if (args.length < 2 || !(args[1].equalsIgnoreCase("gui") || args[1].equalsIgnoreCase("custom"))) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specifiy a means of voting: gui, custom).");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specifiy a tribe for starting the vote.");
            return;
        }

        String tribe_name = args[2];
        String method = args[1].toLowerCase();

        Team tribe = scoreboard.getTeam(tribe_name);
        if (tribe == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe does not exist");
            return;
        }

        Set<Integer> voteNums = getVoteNumbers();
        int newVoteNum = voteNums.isEmpty() ? 1 : (voteNums.stream().max(Integer::compareTo).orElse(0) + 1);

        plugin.getConfig().set("votes." + newVoteNum + ".host", player.getName());
        plugin.getConfig().set("votes." + newVoteNum + ".team", tribe_name);
        plugin.getConfig().set("votes." + newVoteNum + ".method", method);
        plugin.getConfig().createSection("votes." + newVoteNum + ".votes_cast");
        plugin.saveConfig();

        player.sendMessage(prefixHandler.getSurvPrefix() + "You have started a vote for tribe " + tribe_name);

        ChatColor teamColor = ChatColor.WHITE;
        if (scoreboard.getTeam(tribe_name).getColor() != null) {
            teamColor = scoreboard.getTeam(tribe_name).getColor();
        }

        for (String s : tribe.getEntries()) {
            Player tribe_player = Bukkit.getPlayer(s);
            if (tribe_player != null) {
                if (method.equalsIgnoreCase("gui")) {
                    tribe_player.sendMessage(prefixHandler.getSurvPrefix() + "The vote has started!");
                    player.sendTitle(ChatColor.WHITE + "Vote Started!", teamColor + "Click to vote", 10, 60, 10);
                    openTeamVoteGui(tribe_player);
                } else {
                    tribe_player.sendMessage(prefixHandler.getSurvPrefix() + "The vote has started: /vote <name>");
                    player.sendTitle(ChatColor.WHITE + "Vote Started!", teamColor + "/vote <player>", 10, 60, 10);
                }
            }
        }
    }

    public void listVote(String[] args, Player player) {
        if (!player.isOp()) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You do not have permission to do that.");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("reset")) {
            plugin.getConfig().set("votes", null);
            plugin.saveConfig();
            player.sendMessage(prefixHandler.getSurvPrefix() + "Vote list reset.");
            return;
        }

        openVoteListGui(player);
    }

    public void castVote(String[] args, Player player) {
        Team playerTeam = scoreboard.getEntryTeam(player.getName());
        if (playerTeam == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You must be on a team to vote!");
            return;
        }

        Integer latestTeamVote = getLatestVoteForTeam(playerTeam.getName());
        if (latestTeamVote == -1) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You cannot vote right now");
            return;
        }

        String method = plugin.getConfig().getString("votes." + latestTeamVote + ".method", "custom");

        if (method.equalsIgnoreCase("gui")) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "This vote is using the GUI method.");
            return;
        }

        String voteChoice = String.join(" ", args).trim();
        if (voteChoice.isEmpty()) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a name to vote");
            return;
        }

        castVoteChoice(player, latestTeamVote, voteChoice, playerTeam.getName());
    }

    public void castVoteChoice(Player player, int voteId, String voteChoice, String teamName) {
        String voterName = player.getName();
        String path = "votes." + voteId + ".votes_cast." + voterName;

        if (plugin.getConfig().contains(path)) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You already voted!");
            return;
        }

        plugin.getConfig().set(path, voteChoice);
        plugin.saveConfig();

        player.sendMessage(prefixHandler.getSurvPrefix() + "Vote cast for: " + ChatColor.YELLOW + voteChoice);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp()) {
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + player.getName() + " casted vote for " + voteChoice);
            }
        }

        giveHostVotePaper(voteId, voteChoice, teamName);
    }

    public void giveHostVotePaper(int voteId, String voteChoice, String teamName) {
        String hostName = plugin.getConfig().getString("votes." + voteId + ".host");
        if (hostName == null) return;

        Player host = Bukkit.getPlayerExact(hostName);
        if (host == null) return;

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + voteChoice);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to reveal");
            lore.add(ChatColor.GRAY + teamName);
            meta.setLore(lore);
            paper.setItemMeta(meta);
        }

        host.getInventory().addItem(paper);
        host.sendMessage(prefixHandler.getSurvPrefix() + "You received a vote reveal paper.");
    }

    public void openTeamVoteGui(Player player) {
        Team playerTeam = scoreboard.getEntryTeam(player.getName());
        if (playerTeam == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You must be on a team to vote!");
            return;
        }

        Integer latestTeamVote = getLatestVoteForTeam(playerTeam.getName());
        if (latestTeamVote == -1) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You cannot vote right now");
            return;
        }

        String method = plugin.getConfig().getString("votes." + latestTeamVote + ".method", "custom");
        if (!method.equalsIgnoreCase("gui")) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "This vote is not using the GUI method.");
            return;
        }

        String voterName = player.getName();
        String alreadyVotedPath = "votes." + latestTeamVote + ".votes_cast." + voterName;
        if (plugin.getConfig().contains(alreadyVotedPath)) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "You already voted!");
            return;
        }

        List<Player> teamPlayers = new ArrayList<>();
        for (String entry : playerTeam.getEntries()) {
            Player p = Bukkit.getPlayer(entry);
            if (p != null) teamPlayers.add(p);
        }

        Inventory inv = Bukkit.createInventory(null, 45, VOTE_GUI_TITLE);

        int slot = 0;
        for (Player p : teamPlayers) {
            if (p.getName().equalsIgnoreCase(player.getName())) continue;
            if (slot >= inv.getSize()) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName(ChatColor.YELLOW + p.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_GRAY + "vote");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot, head);
            slot++;
        }

        player.openInventory(inv);
    }

    public void openVoteListGui(Player player) {
        ConfigurationSection votes = plugin.getConfig().getConfigurationSection("votes");
        if (votes == null || votes.getKeys(false).isEmpty()) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "There are no votes to list.");
            return;
        }

        List<Integer> voteIds = new ArrayList<>();
        for (String key : votes.getKeys(false)) {
            try {
                voteIds.add(Integer.parseInt(key));
            } catch (Exception ignored) {
            }
        }
        voteIds.sort(Comparator.reverseOrder());

        Inventory inv = Bukkit.createInventory(null, 54, LIST_GUI_TITLE);

        int slot = 0;
        for (Integer voteId : voteIds) {
            if (slot >= inv.getSize()) break;

            String base = "votes." + voteId;
            String host = plugin.getConfig().getString(base + ".host", "unknown");
            String team = plugin.getConfig().getString(base + ".team", "unknown");
            String method = plugin.getConfig().getString(base + ".method", "unknown");

            ConfigurationSection cast = plugin.getConfig().getConfigurationSection(base + ".votes_cast");
            int count = cast == null ? 0 : cast.getKeys(false).size();

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Vote #" + voteId);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_GRAY + "vote_list");
                lore.add(ChatColor.DARK_GRAY + "id:" + voteId);
                lore.add(ChatColor.GRAY + "Host: " + ChatColor.YELLOW + host);
                lore.add(ChatColor.GRAY + "Team: " + ChatColor.YELLOW + team);
                lore.add(ChatColor.GRAY + "Method: " + ChatColor.YELLOW + method);
                lore.add(ChatColor.GRAY + "Votes cast: " + ChatColor.YELLOW + count);
                lore.add(ChatColor.GRAY + "Click to view details");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(slot, item);
            slot++;
        }

        player.openInventory(inv);
    }

    public void openVoteDetailsGui(Player player, int voteId) {
        String base = "votes." + voteId;
        ConfigurationSection cast = plugin.getConfig().getConfigurationSection(base + ".votes_cast");
        if (cast == null || cast.getKeys(false).isEmpty()) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "No votes were cast for this vote.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, DETAILS_GUI_PREFIX + voteId);

        int slot = 0;
        for (String voterName : cast.getKeys(false)) {
            if (slot >= inv.getSize()) break;

            String voteChoice = cast.getString(voterName, "unknown");

            Player voter = Bukkit.getPlayerExact(voterName);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                if (voter != null) {
                    meta.setOwningPlayer(voter);
                }
                meta.setDisplayName(ChatColor.YELLOW + voterName);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Voted: " + ChatColor.YELLOW + voteChoice);
                meta.setLore(lore);
                head.setItemMeta(meta);
            }

            inv.setItem(slot, head);
            slot++;
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equalsIgnoreCase(VOTE_GUI_TITLE)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;
            if (!item.hasItemMeta()) return;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            List<String> lore = meta.getLore();
            if (lore == null || lore.stream().noneMatch(s -> ChatColor.stripColor(s).toLowerCase().contains("vote"))) return;

            String choice = ChatColor.stripColor(meta.getDisplayName());
            if (choice == null || choice.isEmpty()) return;

            Team t = scoreboard.getEntryTeam(player.getName());
            if (t == null) return;

            Integer latestTeamVote = getLatestVoteForTeam(t.getName());
            if (latestTeamVote == -1) return;

            allowVoteGuiClose.add(player.getName());
            castVoteChoice(player, latestTeamVote, choice, t.getName());
            player.closeInventory();
            return;
        }

        if (event.getView().getTitle().equalsIgnoreCase(LIST_GUI_TITLE)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;
            if (!item.hasItemMeta()) return;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;

            List<String> lore = meta.getLore();
            if (lore == null) return;

            Integer voteId = null;
            for (String s : lore) {
                String stripped = ChatColor.stripColor(s).toLowerCase();
                if (stripped.startsWith("id:")) {
                    try {
                        voteId = Integer.parseInt(stripped.replace("id:", "").trim());
                    } catch (Exception ignored) {
                    }
                }
            }

            if (voteId == null) return;

            openVoteDetailsGui(player, voteId);
            return;
        }

        if (event.getView().getTitle().startsWith(DETAILS_GUI_PREFIX)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!event.getView().getTitle().equalsIgnoreCase(VOTE_GUI_TITLE)) {
            return;
        }

        if (allowVoteGuiClose.remove(player.getName())) {
            return;
        }

        if (event.getInventory().isEmpty()) {
            return;
        }

        Team team = scoreboard.getEntryTeam(player.getName());
        if (team == null) {
            return;
        }

        int voteId = getLatestVoteForTeam(team.getName());
        if (voteId == -1) {
            return;
        }

        String method = plugin.getConfig().getString("votes." + voteId + ".method", "custom");
        if (!method.equalsIgnoreCase("gui")) {
            return;
        }

        String voterName = player.getName();
        String alreadyVotedPath = "votes." + voteId + ".votes_cast." + voterName;
        if (plugin.getConfig().contains(alreadyVotedPath)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            openTeamVoteGui(player);
        });
    }

    @EventHandler
    public void onVotePaperUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PAPER) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) return;

        String revealLine = ChatColor.stripColor(lore.get(0));
        if (!revealLine.contains("Right-click to reveal")) return;

        String teamName = ChatColor.stripColor(lore.get(1));
        Team voteTeam = scoreboard.getTeam(teamName);
        ChatColor teamColor = ChatColor.WHITE;
        if ((voteTeam != null) && (voteTeam.getColor() != null)) {
            teamColor = voteTeam.getColor();
        }

        if (!player.isOp()) {
            return;
        }

        String choice = ChatColor.stripColor(meta.getDisplayName());
        if (choice == null || choice.isEmpty()) return;

        for (Entity e : player.getNearbyEntities(50, 50, 50)) {
            if (e instanceof Player) {
                Player p = (Player) e;
                p.sendTitle(ChatColor.WHITE + "Vote cast for...", teamColor + choice, 10, 60, 10);
            }
        }
        player.sendTitle(ChatColor.WHITE + "Vote cast for...", teamColor + choice, 10, 60, 10);

        player.sendMessage(prefixHandler.getSurvPrefix() + "Revealed vote: " + ChatColor.YELLOW + choice);
    }

    public Integer getLatestVoteForTeam(String teamName) {
        ConfigurationSection votes = plugin.getConfig().getConfigurationSection("votes");
        if (votes == null) return -1;

        int latest = -1;

        for (String key : votes.getKeys(false)) {
            int voteNum;
            try {
                voteNum = Integer.parseInt(key);
            } catch (Exception ignored) {
                continue;
            }

            String team = votes.getString(key + ".team");
            if (team != null && teamName.equalsIgnoreCase(team) && voteNum > latest) {
                latest = voteNum;
            }
        }

        return latest;
    }

    public Set<Integer> getVoteNumbers() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("votes");
        if (section == null) return Collections.emptySet();

        return section.getKeys(false).stream().map(k -> {
            try {
                return Integer.parseInt(k);
            } catch (Exception ignored) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("vote")) return Collections.emptyList();

        boolean isPlayer = sender instanceof Player;
        Player player = isPlayer ? (Player) sender : null;
        boolean isOp = sender.isOp();

        if (args.length == 1) {
            String partial = args[0];

            List<String> base = new ArrayList<>();
            if (isOp) {
                base.add("start");
                base.add("list");
            }

            return filterStartsWith(base, partial);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            if (!isOp) return Collections.emptyList();
            return filterStartsWith(Arrays.asList("gui", "custom"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            if (!isOp) return Collections.emptyList();
            if (!(args[1].equalsIgnoreCase("gui") || args[1].equalsIgnoreCase("custom"))) return Collections.emptyList();

            List<String> teamNames = scoreboard.getTeams().stream().map(Team::getName).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());

            return filterStartsWith(teamNames, args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            if (!isOp) return Collections.emptyList();
            return filterStartsWith(Collections.singletonList("reset"), args[1]);
        }

        if (!args[0].equalsIgnoreCase("start") && !args[0].equalsIgnoreCase("list")) {
            String last = args[args.length - 1];
            return filterStartsWith(getSuggestedVotablePlayers(player), last);
        }

        return Collections.emptyList();
    }

    private List<String> getSuggestedVotablePlayers(Player player) {
        if (player != null) {
            Team t = scoreboard.getEntryTeam(player.getName());
            if (t != null) {
                return t.getEntries().stream().filter(name -> !name.equalsIgnoreCase(player.getName())).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
            }
        }

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }

    private List<String> filterStartsWith(Collection<String> options, String partialRaw) {
        String partial = partialRaw == null ? "" : partialRaw.toLowerCase(Locale.ROOT);

        LinkedHashSet<String> unique = new LinkedHashSet<>(options);

        return unique.stream().filter(Objects::nonNull).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(partial)).collect(Collectors.toList());
    }
}
