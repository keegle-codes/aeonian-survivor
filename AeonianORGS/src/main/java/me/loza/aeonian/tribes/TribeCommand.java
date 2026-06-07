package me.loza.aeonian.tribes;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TribeCommand implements CommandExecutor, TabCompleter, Listener {

    private final PrefixHandler prefixHandler = new PrefixHandler();
    private final Aeonian plugin;

    public TribeCommand(Aeonian plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private enum TribeOptionType {
        COLLISION_RULE,
        FRIENDLY_FIRE,
        SEE_FRIENDLY_INVISIBLES,
        DEATH_MESSAGE_VISIBILITY
    }

    private static class TribeOptionsHolder implements InventoryHolder {
        private final String tribeName;

        public TribeOptionsHolder(String tribeName) {
            this.tribeName = tribeName;
        }

        public String getTribeName() {
            return tribeName;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private static class TribeOptionValueHolder implements InventoryHolder {
        private final String tribeName;
        private final TribeOptionType optionType;

        public TribeOptionValueHolder(String tribeName, TribeOptionType optionType) {
            this.tribeName = tribeName;
            this.optionType = optionType;
        }

        public String getTribeName() {
            return tribeName;
        }

        public TribeOptionType getOptionType() {
            return optionType;
        }

        @Override
        public Inventory getInventory() {
            return null; // not used
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.isOp()) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "You don't have permission");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Correct Usage:");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.YELLOW + "Can use color codes for pref/suf (leave blank to reset)");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe add <tribe>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe remove <tribe>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe join <tribe> <player>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe leave <player>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe color <tribe> <color>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe prefix <tribe> <prefix>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe suffix <tribe> <prefix>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe tp <tribe/*> <player/warp>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe options <tribe>");
            player.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/tribe list");
            return true;
        }

        String subcmd = args[0];

        if (subcmd.equalsIgnoreCase("add")) {
            addTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("remove")) {
            removeTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("list")) {
            listTribes(player);
        } else if (subcmd.equalsIgnoreCase("join")) {
            joinTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("leave")) {
            leaveTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("color")) {
            colorTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("prefix")) {
            prefixTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("suffix")) {
            suffixTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("tp")) {
            tpTribe(args, player);
        } else if (subcmd.equalsIgnoreCase("options")) {
            optionsTribe(args, player);
        }

        return true;
    }

    public void listTribes(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getTeams().size() < 1) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "No tribes currently made!");
            return;
        }

        for (Team t : scoreboard.getTeams()) {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Team " + t.getName() + ":");
            if (t.getEntries().size() > 0) {
                player.sendMessage(ChatColor.DARK_GRAY + "» " + t.getColor() + t.getEntries());
            } else {
                player.sendMessage(ChatColor.DARK_GRAY + "» " + t.getColor() + "Empty");
            }
        }
    }

    public void addTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Specify a name for the tribe!");
            return;
        }

        String name = args[1];
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team existing = scoreboard.getTeam(name);
        if (existing != null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "A tribe with that name already exists!");
            return;
        }

        scoreboard.registerNewTeam(name);
        player.sendMessage(prefixHandler.getSurvPrefix() + "Created new tribe: " + name);
    }

    public void removeTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Specify a tribe to remove!");
            return;
        }

        String name = args[1];

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team existing = scoreboard.getTeam(name);
        if (existing == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "No tribe with that name exists!");
            return;
        }

        existing.unregister();

        player.sendMessage(prefixHandler.getSurvPrefix() + "Removed tribe: " + name);
    }

    public void joinTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a tribe to join!");
            return;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String name = args[1];
        Team existing = scoreboard.getTeam(name);
        if (existing == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe doesn't exist!");
            return;
        }

        if (args.length < 3) {
            existing.addEntry(player.getName());
            player.sendMessage(prefixHandler.getSurvPrefix() + "You have joined tribe " + name);
        } else {
            existing.addEntry(args[2]);
            player.sendMessage(prefixHandler.getSurvPrefix() + "You added " + args[2] + " to tribe " + name);
        }
    }

    public void leaveTribe(String[] args, Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String targetName;
        if (args.length >= 2) {
            targetName = args[1];
        } else {
            targetName = player.getName();
        }

        boolean removed = false;

        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(targetName)) {
                team.removeEntry(targetName);
                removed = true;

                player.sendMessage(prefixHandler.getSurvPrefix() + "Removed " + targetName + " from tribe " + team.getName());
            }
        }
        if (!removed) {
            player.sendMessage(prefixHandler.getErrorPrefix() + targetName + " is not on any tribe!");
        }
    }

    public void colorTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a tribe to color!");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a color for the tribe!");
            return;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String name = args[1];
        Team existing = scoreboard.getTeam(name);
        if (existing == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe doesn't exist!");
            return;
        }

        String color_name = args[2].toUpperCase();
        ChatColor color;
        try {
            color = ChatColor.valueOf(color_name);
        } catch (IllegalArgumentException e) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Invalid color, example of valid colors: RED, BLUE, AQUA, YELLOW, etc.");
            return;
        }

        if (!color.isColor()) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Extra formatting lost support in 1.9+.");
            return;
        }
        existing.setColor(color);
        player.sendMessage(prefixHandler.getSurvPrefix() + "Set color of tribe " + name + " to " + color + color.name());
    }

    public void prefixTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a tribe to prefix!");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a prefix for the tribe (or 'clear' to clear it)!");
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String name = args[1];
        Team existing = scoreboard.getTeam(name);
        if (existing == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe doesn't exist!");
            return;
        }

        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            prefixBuilder.append(args[i]).append(" ");
        }
        String rawPrefix = prefixBuilder.toString();

        if (rawPrefix.isEmpty() || rawPrefix.equalsIgnoreCase("clear")) {
            existing.setPrefix("");
            player.sendMessage(prefixHandler.getSurvPrefix() + "Cleared prefix for tribe " + name);
            return;
        }

        String coloredPrefix = ChatColor.translateAlternateColorCodes('&', rawPrefix);
        existing.setPrefix(coloredPrefix);

        player.sendMessage(prefixHandler.getSurvPrefix() + "Set prefix of tribe " + name + " to " + coloredPrefix);
    }

    public void suffixTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a tribe to suffix!");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a suffix for the tribe (or 'clear' to clear it)!");
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String name = args[1];
        Team existing = scoreboard.getTeam(name);
        if (existing == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe doesn't exist!");
            return;
        }

        StringBuilder suffixBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            suffixBuilder.append(args[i]).append(" ");
        }
        String rawSuffix = suffixBuilder.toString();

        if (rawSuffix.isEmpty() || rawSuffix.equalsIgnoreCase("clear")) {
            existing.setSuffix("");
            player.sendMessage(prefixHandler.getSurvPrefix() + "Cleared suffix for tribe " + name);
            return;
        }

        String coloredSuffix = ChatColor.translateAlternateColorCodes('&', rawSuffix);
        existing.setSuffix(coloredSuffix);

        player.sendMessage(prefixHandler.getSurvPrefix() + "Set suffix of tribe " + name + " to " + coloredSuffix);
    }

    public void tpTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Usage: /tribe tp <tribe/*> <player/warp>");
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String tribeArg = args[1];

        Location destination;

        if (args.length >= 3) {
            String targetArg = args[2];

            Player targetPlayer = Bukkit.getPlayerExact(targetArg);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                destination = targetPlayer.getLocation();
            } else {
                Location warpLoc = plugin.getConfig().getLocation("warps." + targetArg);
                if (warpLoc == null) {
                    player.sendMessage(prefixHandler.getErrorPrefix() + "No online player or warp named '" + targetArg + "' found.");
                    return;
                }
                destination = warpLoc;
            }
        } else {
            destination = player.getLocation();
        }

        int teleportedCount = 0;

        if (tribeArg.equals("*")) {
            for (Team team : scoreboard.getTeams()) {
                for (String entry : team.getEntries()) {
                    Player tribePlayer = Bukkit.getPlayerExact(entry);
                    if (tribePlayer != null && tribePlayer.isOnline()) {
                        tribePlayer.teleport(destination);
                        teleportedCount++;
                    }
                }
            }

            if (teleportedCount == 0) {
                player.sendMessage(prefixHandler.getErrorPrefix() + "No online players in any tribe to teleport.");
            } else {
                player.sendMessage(prefixHandler.getSurvPrefix() + "Teleported " + teleportedCount + " tribe player(s).");
            }
            return;
        }

        Team tribe = scoreboard.getTeam(tribeArg);
        if (tribe == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe doesn't exist!");
            return;
        }

        for (String entry : tribe.getEntries()) {
            Player tribePlayer = Bukkit.getPlayerExact(entry);
            if (tribePlayer != null && tribePlayer.isOnline()) {
                tribePlayer.teleport(destination);
                teleportedCount++;
            }
        }

        if (teleportedCount == 0) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "No online players in tribe " + tribeArg + " to teleport.");
        } else {
            player.sendMessage(prefixHandler.getSurvPrefix() + "Teleported " + teleportedCount + " player(s) in tribe " + tribeArg + ".");
        }
    }

    public void optionsTribe(String[] args, Player player) {
        if (args.length < 2) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "Specify a tribe to open options for!");
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String name = args[1];
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe doesn't exist!");
            return;
        }

        openOptionsMenu(player, team);
    }

    private void openOptionsMenu(Player player, Team team) {
        String tribeName = team.getName();
        Inventory inv = Bukkit.createInventory(
                new TribeOptionsHolder(tribeName),
                9,
                ChatColor.RED + "Tribe Options: " + tribeName
        );

        String currentCollision = collisionRuleToString(team.getOption(Team.Option.COLLISION_RULE));
        inv.setItem(1, createOptionItem(
                Material.BARRIER,
                ChatColor.GOLD + "collisionRule",
                "Current: " + currentCollision
        ));

        inv.setItem(3, createOptionItem(
                Material.FLINT_AND_STEEL,
                ChatColor.GOLD + "friendlyFire",
                "Current: " + team.allowFriendlyFire()
        ));

        inv.setItem(5, createOptionItem(
                Material.GOLDEN_CARROT,
                ChatColor.GOLD + "seeFriendlyInvisibles",
                "Current: " + team.canSeeFriendlyInvisibles()
        ));

        String currentDeathVis = deathVisibilityToString(team.getOption(Team.Option.DEATH_MESSAGE_VISIBILITY));
        inv.setItem(7, createOptionItem(
                Material.PAPER,
                ChatColor.GOLD + "deathMessageVisibility",
                "Current: " + currentDeathVis
        ));

        player.openInventory(inv);
    }

    private void openOptionValueMenu(Player player, String tribeName, TribeOptionType optionType) {
        String titleOptionName;
        switch (optionType) {
            case COLLISION_RULE:
                titleOptionName = "collisionRule";
                break;
            case FRIENDLY_FIRE:
                titleOptionName = "friendlyFire";
                break;
            case SEE_FRIENDLY_INVISIBLES:
                titleOptionName = "seeFriendlyInvisibles";
                break;
            case DEATH_MESSAGE_VISIBILITY:
                titleOptionName = "deathMessageVisibility";
                break;
            default:
                titleOptionName = "Option";
        }

        Inventory inv = Bukkit.createInventory(
                new TribeOptionValueHolder(tribeName, optionType),
                9,
                ChatColor.RED + titleOptionName + ": " + tribeName
        );

        switch (optionType) {
            case COLLISION_RULE:
                inv.setItem(1, simpleValueItem(Material.PAPER, "always"));
                inv.setItem(3, simpleValueItem(Material.BARRIER, "never"));
                inv.setItem(5, simpleValueItem(Material.BOOK, "pushOtherTeams"));
                inv.setItem(7, simpleValueItem(Material.BOOKSHELF, "pushOwnTeam"));
                break;

            case FRIENDLY_FIRE:
            case SEE_FRIENDLY_INVISIBLES:
                inv.setItem(3, simpleValueItem(Material.LIME_WOOL, "true"));
                inv.setItem(5, simpleValueItem(Material.RED_WOOL, "false"));
                break;

            case DEATH_MESSAGE_VISIBILITY:
                inv.setItem(1, simpleValueItem(Material.PAPER, "always"));
                inv.setItem(3, simpleValueItem(Material.BARRIER, "never"));
                inv.setItem(5, simpleValueItem(Material.BOOK, "hideForOtherTeams"));
                inv.setItem(7, simpleValueItem(Material.BOOKSHELF, "hideForOwnTeam"));
                break;
        }

        player.openInventory(inv);
    }

    private ItemStack createOptionItem(Material material, String name, String line1) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + line1);
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack simpleValueItem(Material material, String plainName) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + plainName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Set to " + plainName);
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String collisionRuleToString(Team.OptionStatus status) {
        if (status == null) status = Team.OptionStatus.ALWAYS;
        switch (status) {
            case ALWAYS:
                return "always";
            case NEVER:
                return "never";
            case FOR_OTHER_TEAMS:
                return "pushOtherTeams";
            case FOR_OWN_TEAM:
                return "pushOwnTeam";
            default:
                return "always";
        }
    }

    private String deathVisibilityToString(Team.OptionStatus status) {
        if (status == null) status = Team.OptionStatus.ALWAYS;
        switch (status) {
            case ALWAYS:
                return "always";
            case NEVER:
                return "never";
            case FOR_OTHER_TEAMS:
                return "hideForOtherTeams";
            case FOR_OWN_TEAM:
                return "hideForOwnTeam";
            default:
                return "always";
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof TribeOptionsHolder) && !(holder instanceof TribeOptionValueHolder)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().getDisplayName() == null) {
            return;
        }

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (holder instanceof TribeOptionsHolder) {
            TribeOptionsHolder toh = (TribeOptionsHolder) holder;
            Team team = scoreboard.getTeam(toh.getTribeName());
            if (team == null) {
                player.closeInventory();
                player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe no longer exists!");
                return;
            }

            if (displayName.contains("collisionrule")) {
                openOptionValueMenu(player, team.getName(), TribeOptionType.COLLISION_RULE);
            } else if (displayName.contains("friendlyfire")) {
                openOptionValueMenu(player, team.getName(), TribeOptionType.FRIENDLY_FIRE);
            } else if (displayName.contains("seefriendlyinvisibles")) {
                openOptionValueMenu(player, team.getName(), TribeOptionType.SEE_FRIENDLY_INVISIBLES);
            } else if (displayName.contains("deathmessagevisibility")) {
                openOptionValueMenu(player, team.getName(), TribeOptionType.DEATH_MESSAGE_VISIBILITY);
            }

        } else if (holder instanceof TribeOptionValueHolder) {
            TribeOptionValueHolder tvh = (TribeOptionValueHolder) holder;
            Team team = scoreboard.getTeam(tvh.getTribeName());
            if (team == null) {
                player.closeInventory();
                player.sendMessage(prefixHandler.getErrorPrefix() + "That tribe no longer exists!");
                return;
            }

            String valueKey = displayName;

            switch (tvh.getOptionType()) {
                case COLLISION_RULE:
                    applyCollisionRule(team, valueKey);
                    player.sendMessage(prefixHandler.getSurvPrefix() + "Set collisionRule of " + team.getName() + " to " + valueKey);
                    break;

                case FRIENDLY_FIRE:
                    boolean ff = valueKey.equalsIgnoreCase("true");
                    team.setAllowFriendlyFire(ff);
                    player.sendMessage(prefixHandler.getSurvPrefix() + "Set friendlyFire of " + team.getName() + " to " + ff);
                    break;

                case SEE_FRIENDLY_INVISIBLES:
                    boolean sfi = valueKey.equalsIgnoreCase("true");
                    team.setCanSeeFriendlyInvisibles(sfi);
                    player.sendMessage(prefixHandler.getSurvPrefix() + "Set seeFriendlyInvisibles of " + team.getName() + " to " + sfi);
                    break;

                case DEATH_MESSAGE_VISIBILITY:
                    applyDeathVisibility(team, valueKey);
                    player.sendMessage(prefixHandler.getSurvPrefix() + "Set deathMessageVisibility of " + team.getName() + " to " + valueKey);
                    break;
            }

            openOptionsMenu(player, team);
        }
    }

    private void applyCollisionRule(Team team, String valueKey) {
        String key = valueKey.replace(" ", "").toLowerCase();
        Team.OptionStatus status;
        if (key.equals("always")) {
            status = Team.OptionStatus.ALWAYS;
        } else if (key.equals("never")) {
            status = Team.OptionStatus.NEVER;
        } else if (key.equals("pushotherteams")) {
            status = Team.OptionStatus.FOR_OTHER_TEAMS;
        } else if (key.equals("pushownteam")) {
            status = Team.OptionStatus.FOR_OWN_TEAM;
        } else {
            status = Team.OptionStatus.ALWAYS;
        }
        team.setOption(Team.Option.COLLISION_RULE, status);
    }

    private void applyDeathVisibility(Team team, String valueKey) {
        String key = valueKey.replace(" ", "").toLowerCase();
        Team.OptionStatus status;
        if (key.equals("always")) {
            status = Team.OptionStatus.ALWAYS;
        } else if (key.equals("never")) {
            status = Team.OptionStatus.NEVER;
        } else if (key.equals("hideforotherteams")) {
            status = Team.OptionStatus.FOR_OTHER_TEAMS;
        } else if (key.equals("hideforownteam")) {
            status = Team.OptionStatus.FOR_OWN_TEAM;
        } else {
            status = Team.OptionStatus.ALWAYS;
        }
        team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, status);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !command.getName().equalsIgnoreCase("tribe")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();

            addIfStartsWith(completions, "add", partial);
            addIfStartsWith(completions, "remove", partial);
            addIfStartsWith(completions, "join", partial);
            addIfStartsWith(completions, "leave", partial);
            addIfStartsWith(completions, "color", partial);
            addIfStartsWith(completions, "prefix", partial);
            addIfStartsWith(completions, "suffix", partial);
            addIfStartsWith(completions, "tp", partial);
            addIfStartsWith(completions, "options", partial);
            addIfStartsWith(completions, "list", partial);

            return completions;
        }

        if (args.length == 2) {
            String subcmd = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            if (subcmd.equals("remove") || subcmd.equals("join") || subcmd.equals("color") || subcmd.equals("prefix") || subcmd.equals("suffix") || subcmd.equals("options")) {
                for (String tribe : getTribeNames()) {
                    if (tribe.toLowerCase().startsWith(partial)) {
                        completions.add(tribe);
                    }
                }
            } else if (subcmd.equals("tp")) {
                if ("*".startsWith(partial)) {
                    completions.add("*");
                }
                for (String tribe : getTribeNames()) {
                    if (tribe.toLowerCase().startsWith(partial)) {
                        completions.add(tribe);
                    }
                }
            } else if (subcmd.equals("leave")) {
                for (String name : getOnlinePlayerNames()) {
                    if (name.toLowerCase().startsWith(partial)) {
                        completions.add(name);
                    }
                }
            } else if (subcmd.equals("list")) {
                return Collections.emptyList();
            }

            return completions;
        }

        if (args.length == 3) {
            String subcmd = args[0].toLowerCase();
            String partial = args[2].toLowerCase();

            if (subcmd.equals("join")) {
                for (String name : getOnlinePlayerNames()) {
                    if (name.toLowerCase().startsWith(partial)) {
                        completions.add(name);
                    }
                }
            } else if (subcmd.equals("color")) {
                for (ChatColor color : ChatColor.values()) {
                    if (color.isColor()) {
                        String colorName = color.name().toLowerCase();
                        if (colorName.startsWith(partial)) {
                            completions.add(colorName);
                        }
                    }
                }
            } else if (subcmd.equals("tp")) {
                for (String name : getOnlinePlayerNames()) {
                    if (name.toLowerCase().startsWith(partial)) {
                        completions.add(name);
                    }
                }
                for (String warp : getWarpNames()) {
                    if (warp.toLowerCase().startsWith(partial)) {
                        completions.add(warp);
                    }
                }
            }

            return completions;
        }

        return Collections.emptyList();
    }

    private List<String> getTribeNames() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        List<String> names = new ArrayList<>();
        for (Team team : scoreboard.getTeams()) {
            names.add(team.getName());
        }
        return names;
    }

    private List<String> getWarpNames() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("warps");
        if (section == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            names.add(online.getName());
        }
        return names;
    }

    private void addIfStartsWith(List<String> list, String value, String partialLower) {
        if (value.toLowerCase().startsWith(partialLower)) {
            list.add(value);
        }
    }
}
