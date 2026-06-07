package me.loza.aeonian.essentials;

import me.loza.aeonian.Aeonian;
import me.loza.aeonian.handlers.PrefixHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerCommand implements CommandExecutor, TabCompleter {

    private final Aeonian plugin;
    private final PrefixHandler prefixHandler = new PrefixHandler();

    private static final Pattern TIME_TOKEN_PATTERN = Pattern.compile("(?i)^(\\d+)([hms])$");

    private final List<BukkitRunnable> activeTimers = new ArrayList<>();
    private final List<BossBar> activeBossBars = new ArrayList<>();

    public TimerCommand(Aeonian plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length >= 1 && args[0].equalsIgnoreCase("cancel")) {
            cancelAllTimers(sender);
            return true;
        }

        if (args.length < 3) {
            sendUsage(sender);
            return true;
        }

        String mode = args[0].toLowerCase();
        if (!mode.equals("dragon") && !mode.equals("action")) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Mode must be 'dragon', 'action', or 'cancel'.");
            sendUsage(sender);
            return true;
        }

        ParsedTimer parsed = parseMessageAndTime(args);
        if (parsed == null) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Invalid time format.");
            sendUsage(sender);
            return true;
        }

        if (parsed.totalSeconds <= 0) {
            sender.sendMessage(prefixHandler.getErrorPrefix() + "Timer duration must be greater than zero.");
            return true;
        }

        if (parsed.message.isEmpty()) {
            parsed.message = "Timer";
        }

        if (mode.equals("dragon")) {
            startDragonTimer(sender, parsed.message, parsed.totalSeconds);
        } else {
            startActionBarTimer(sender, parsed.message, parsed.totalSeconds);
        }

        sender.sendMessage(prefixHandler.getSurvPrefix() + "Started " + mode + " timer: " +
                ChatColor.YELLOW + parsed.message + ChatColor.GRAY +
                " (" + formatTime(parsed.totalSeconds) + ")");

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Correct Usage:");
        sender.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/timer dragon <message> <time>");
        sender.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/timer action <message> <time>");
        sender.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "/timer cancel");
        sender.sendMessage(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Time example: 5h 2m 30s, 10m, 30s, 1h 30m");
    }

    private void startDragonTimer(CommandSender sender, String message, long totalSeconds) {
        BossBar bossBar = Bukkit.createBossBar(
                ChatColor.LIGHT_PURPLE + message + " (" + formatTime(totalSeconds) + ")",
                BarColor.PURPLE,
                BarStyle.SEGMENTED_20
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(online);
        }

        activeBossBars.add(bossBar);

        BukkitRunnable task = new BukkitRunnable() {
            long remaining = totalSeconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    bossBar.setProgress(0.0);
                    bossBar.setTitle(ChatColor.RED + message + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + "00s");
                    bossBar.removeAll();
                    activeBossBars.remove(bossBar);
                    activeTimers.remove(this);
                    cancel();
                    return;
                }

                double progress = (double) remaining / (double) totalSeconds;
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                bossBar.setTitle(ChatColor.RED + message + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + formatTime(remaining));

                remaining--;
            }
        };

        activeTimers.add(task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void startActionBarTimer(CommandSender sender, String message, long totalSeconds) {
        BukkitRunnable task = new BukkitRunnable() {
            long remaining = totalSeconds;

            @Override
            public void run() {
                if (remaining < 0) {
                    activeTimers.remove(this);
                    cancel();
                    return;
                }

                String text = ChatColor.RED + message + ChatColor.DARK_GRAY +
                        " » " + ChatColor.GRAY + formatTime(Math.max(0, remaining));

                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
                }

                if (remaining == 0) {
                    activeTimers.remove(this);
                    cancel();
                    return;
                }

                remaining--;
            }
        };

        activeTimers.add(task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelAllTimers(CommandSender sender) {
        for (BukkitRunnable runnable : new ArrayList<>(activeTimers)) {
            try {
                runnable.cancel();
            } catch (Exception ignored) { }
        }
        activeTimers.clear();

        for (BossBar bossBar : new ArrayList<>(activeBossBars)) {
            bossBar.removeAll();
        }
        activeBossBars.clear();

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }

        sender.sendMessage(prefixHandler.getSurvPrefix() + "All timers have been cancelled.");
    }

    private static class ParsedTimer {
        String message;
        long totalSeconds;
    }

    private ParsedTimer parseMessageAndTime(String[] args) {
        List<String> timeTokens = new ArrayList<>();
        int lastIndex = args.length - 1;

        int index = lastIndex;
        while (index >= 1) {
            Matcher matcher = TIME_TOKEN_PATTERN.matcher(args[index]);
            if (matcher.matches()) {
                timeTokens.add(0, args[index]);
                index--;
            } else {
                break;
            }
        }

        if (timeTokens.isEmpty()) {
            return null;
        }

        long totalSeconds = parseTimeTokens(timeTokens);
        if (totalSeconds <= 0) {
            return null;
        }

        StringBuilder msgBuilder = new StringBuilder();
        for (int i = 1; i <= index; i++) {
            msgBuilder.append(args[i]).append(" ");
        }

        ParsedTimer parsed = new ParsedTimer();
        parsed.message = msgBuilder.toString().trim();
        parsed.totalSeconds = totalSeconds;
        return parsed;
    }

    private long parseTimeTokens(List<String> tokens) {
        long seconds = 0;

        for (String token : tokens) {
            Matcher matcher = TIME_TOKEN_PATTERN.matcher(token);
            if (!matcher.matches()) {
                return -1;
            }

            long value = Long.parseLong(matcher.group(1));
            char unit = Character.toLowerCase(matcher.group(2).charAt(0));

            switch (unit) {
                case 'h':
                    seconds += value * 3600L;
                    break;
                case 'm':
                    seconds += value * 60L;
                    break;
                case 's':
                    seconds += value;
                    break;
                default:
                    return -1;
            }
        }

        return seconds;
    }

    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh%02dm%02ds", hours, minutes, seconds);
        } else {
            if (minutes > 0) {
                return String.format("%02dm%02ds", minutes, seconds);
            } else {
                return String.format("%02ds", seconds);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !command.getName().equalsIgnoreCase("timer")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            addIfStartsWith(completions, "dragon", partial);
            addIfStartsWith(completions, "action", partial);
            addIfStartsWith(completions, "cancel", partial);

            return completions;
        }

        return Collections.emptyList();
    }

    private void addIfStartsWith(List<String> list, String value, String partialLower) {
        if (value.toLowerCase().startsWith(partialLower)) {
            list.add(value);
        }
    }
}
