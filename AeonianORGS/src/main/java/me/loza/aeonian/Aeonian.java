package me.loza.aeonian;

import me.loza.aeonian.chat.AdminChat;
import me.loza.aeonian.chat.ChatCommand;
import me.loza.aeonian.config.ConfigCommand;
import me.loza.aeonian.essentials.*;
import me.loza.aeonian.chat.ChatEvent;
import me.loza.aeonian.events.JoinEvent;
import me.loza.aeonian.events.ServerEvents;
import me.loza.aeonian.handlers.ChairHandler;
import me.loza.aeonian.helpop.HRCommand;
import me.loza.aeonian.helpop.HelpopCommand;
import me.loza.aeonian.misc.SeasonsCommand;
import me.loza.aeonian.spec.SpyCommand;
import me.loza.aeonian.spec.SpecChat;
import me.loza.aeonian.spec.SpecEvent;
import me.loza.aeonian.tribes.TribeCommand;
import me.loza.aeonian.tribes.VoteCommand;
import me.loza.aeonian.worldguard.GuardCommand;
import me.loza.aeonian.worldguard.GuardListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Aeonian extends JavaPlugin {

    public static boolean chat;

    public void registerCommands() {
        WarpCommand warpCommand = new WarpCommand(this);
        getCommand("warp").setExecutor(warpCommand);
        getCommand("warp").setTabCompleter(warpCommand);

        TribeCommand tribeCommand = new TribeCommand(this);
        getCommand("tribe").setExecutor(tribeCommand);
        getCommand("tribe").setTabCompleter(tribeCommand);

        TimerCommand timerCommand = new TimerCommand(this);
        getCommand("timer").setExecutor(timerCommand);
        getCommand("timer").setTabCompleter(timerCommand);

        VoteCommand voteCommand = new VoteCommand(this);
        getCommand("vote").setExecutor(voteCommand);
        getCommand("vote").setTabCompleter(voteCommand);

        getCommand("echest").setExecutor(new EnderCommand(this));
        getCommand("wg").setExecutor(new GuardCommand(this));
        getCommand("fullbright").setExecutor(new FullbrightCommand(this));
        getCommand("commandspy").setExecutor(new SpyCommand(this));
        getCommand("chat").setExecutor(new ChatCommand());
        getCommand("helpop").setExecutor(new HelpopCommand());
        getCommand("hr").setExecutor(new HRCommand());
        getCommand("skull").setExecutor(new SkullCommand());
        getCommand("enchant").setExecutor(new EnchantCommand());
        getCommand("sc").setExecutor(new SpecChat());
        getCommand("ac").setExecutor(new AdminChat());

        getCommand("heal").setExecutor(new UtilityCommands());
        getCommand("feed").setExecutor(new UtilityCommands());
        getCommand("ci").setExecutor(new UtilityCommands());

        getCommand("invsee").setExecutor(new InvseeCommand());
        getCommand("seasons").setExecutor(new SeasonsCommand());

        getCommand("config").setExecutor(new ConfigCommand(this));
        GamemodeCommand gmCmd = new GamemodeCommand(this);

        getCommand("gmc").setExecutor(gmCmd);
        getCommand("gms").setExecutor(gmCmd);
        getCommand("gma").setExecutor(gmCmd);
        getCommand("gmsp").setExecutor(gmCmd);

        getCommand("gmc").setTabCompleter(gmCmd);
        getCommand("gms").setTabCompleter(gmCmd);
        getCommand("gma").setTabCompleter(gmCmd);
        getCommand("gmsp").setTabCompleter(gmCmd);
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new GuardListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new SpecEvent(), this);
        getServer().getPluginManager().registerEvents(new ChatEvent(), this);
        getServer().getPluginManager().registerEvents(new ServerEvents(this), this);
        getServer().getPluginManager().registerEvents(new SeasonsCommand(), this);
        getServer().getPluginManager().registerEvents(new ChairHandler(), this);
    }

    @Override
    public void onEnable() {
        chat = true;
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        registerCommands();
        registerEvents();
        this.getLogger().info("Aeonian Plugin has started! Thank you Keegle :)");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Aeonian Plugin has stopped!");
    }
}
