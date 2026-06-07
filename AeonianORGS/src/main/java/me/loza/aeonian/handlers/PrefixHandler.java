package me.loza.aeonian.handlers;

import org.bukkit.ChatColor;

public class PrefixHandler {
    public String getSurvPrefix() {
        return ChatColor.RED + "Aeonian" + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY;
    }

    public String getSpecPrefix() {
        return ChatColor.GRAY + "(" + ChatColor.YELLOW + "Spec" + ChatColor.GRAY + ") ";
    }

    public String getErrorPrefix() {
        return ChatColor.RED + "Error: ";
    }

    public String getHelpopPrefix() {
        return ChatColor.DARK_GRAY + "(" + ChatColor.DARK_RED + "Helpop" + ChatColor.DARK_GRAY + ") ";
    }

    public String getACPrefix() {
        return ChatColor.DARK_GRAY + "(" + ChatColor.DARK_RED + "Admin Chat" + ChatColor.DARK_GRAY + ") ";
    }

    public String getHelpopReplyPrefix() {
        return ChatColor.DARK_GRAY + "(" + ChatColor.DARK_RED + "Reply" + ChatColor.DARK_GRAY + ") ";
    }
}