package org.garsooon.billboard.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.garsooon.billboard.data.AutoBroadcaster;

import java.util.List;

@SuppressWarnings("SameReturnValue")
public class BroadcastCommands implements CommandExecutor {

    private final AutoBroadcaster autoBroadcaster;

    public BroadcastCommands(AutoBroadcaster autoBroadcaster) {
        this.autoBroadcaster = autoBroadcaster;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("billboard.broadcasts")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to manage broadcasts!");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                return handleList(sender);
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleList(CommandSender sender) {
        List<String> messages = autoBroadcaster.getMessages();
        if (messages.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No broadcasts configured.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Server Broadcasts ===");
        for (int i = 0; i < messages.size(); i++) {
            sender.sendMessage(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.WHITE + messages.get(i));
        }
        return true;
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /broadcast add <message>");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) sb.append(" ");
            sb.append(args[i]);
        }
        String message = sb.toString();

        autoBroadcaster.addMessage(message);
        sender.sendMessage(ChatColor.GREEN + "Broadcast added: " + ChatColor.WHITE + message);
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /broadcast remove <#>");
            return true;
        }

        try {
            int index = Integer.parseInt(args[1]) - 1;
            if (!autoBroadcaster.removeMessage(index)) {
                sender.sendMessage(ChatColor.RED + "Invalid broadcast number!");
                return true;
            }
            sender.sendMessage(ChatColor.GREEN + "Broadcast #" + (index + 1) + " removed.");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number!");
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        autoBroadcaster.reloadBroadcasts();
        sender.sendMessage(ChatColor.GREEN + "Broadcasts reloaded from file. " +
                autoBroadcaster.getMessageCount() + " messages loaded.");
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Broadcast Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/broadcast list " + ChatColor.WHITE + "- List all broadcasts");
        sender.sendMessage(ChatColor.YELLOW + "/broadcast add <message> " + ChatColor.WHITE + "- Add a broadcast");
        sender.sendMessage(ChatColor.YELLOW + "/broadcast remove <#> " + ChatColor.WHITE + "- Remove a broadcast");
        sender.sendMessage(ChatColor.YELLOW + "/broadcast reload " + ChatColor.WHITE + "- Reload broadcasts from file");
    }
}
