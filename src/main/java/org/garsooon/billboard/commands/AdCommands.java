package org.garsooon.billboard.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.garsooon.billboard.billboard;
import org.garsooon.billboard.data.ActiveAd;
import org.garsooon.billboard.data.AdManager;
import org.garsooon.billboard.data.AdRequest;
import org.garsooon.billboard.data.ConfigManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SameReturnValue")
public class AdCommands implements CommandExecutor {

    private final billboard plugin;
    private final AdManager adManager;
    private final ConfigManager configManager;

    public AdCommands(billboard plugin, AdManager adManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.adManager = adManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("buyad")) {
            return handleBuyAd(sender, args);
        } else if (cmd.equals("reviewads")) {
            return handleReviewAds(sender);
        } else if (cmd.equals("approvead")) {
            return handleApproveAd(sender, args);
        } else if (cmd.equals("denyad")) {
            return handleDenyAd(sender, args);
        } else if (cmd.equals("listads")) {
            return handleListAds(sender);
        }

        return false;
    }

    private boolean handleBuyAd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can buy ads!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /buyad <duration_days> <message>");
            return true;
        }

        int duration;
        try {
            duration = Integer.parseInt(args[0]);
            int maxDuration = configManager.getMaxDurationDays();
            if (duration <= 0 || duration > maxDuration) {
                player.sendMessage(ChatColor.RED + "Duration must be between 1 and " + maxDuration + " days!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid duration! Must be a number.");
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        int maxLength = configManager.getMaxMessageLength();
        if (message.length() > maxLength) {
            player.sendMessage(ChatColor.RED + "Message too long! Max " + maxLength + " characters.");
            return true;
        }

        for (AdRequest existingRequest : adManager.getPendingAds()) {
            if (existingRequest.getPlayerName().equals(player.getName())) {
                player.sendMessage(ChatColor.RED + "You already have a pending ad! Wait for approval or denial.");
                return true;
            }
        }

        AdRequest request = new AdRequest(player.getName(), message, duration);
        adManager.addPendingAd(request);

        player.sendMessage(ChatColor.GREEN + "Ad submitted for approval!");
        player.sendMessage(ChatColor.YELLOW + "Message: " + ChatColor.WHITE +
                translateColorCodes(message));
        player.sendMessage(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + duration + " days");

        adManager.notifyModerators(plugin.getServer());

        return true;
    }

    private boolean handleReviewAds(CommandSender sender) {
        if (!sender.hasPermission("billboard.moderate")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to review ads!");
            return true;
        }

        if (adManager.getPendingAds().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No pending ads to review.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Pending Ads ===");
        int index = 1;
        for (AdRequest ad : adManager.getPendingAds()) {
            sender.sendMessage(ChatColor.YELLOW + "#" + index + " - " + ChatColor.WHITE + ad.getPlayerName());
            sender.sendMessage(ChatColor.GRAY + "  Message: " +
                    translateColorCodes(ad.getMessage()));
            sender.sendMessage(ChatColor.GRAY + "  Duration: " + ChatColor.WHITE + ad.getDurationDays() + " days");
            index++;
        }
        sender.sendMessage(ChatColor.GREEN + "Use /approvead <#> or /denyad <#>");

        return true;
    }

    private boolean handleApproveAd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("billboard.moderate")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to approve ads!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /approvead <#>");
            return true;
        }

        try {
            int index = Integer.parseInt(args[0]) - 1;
            List<AdRequest> adList = new ArrayList<>(adManager.getPendingAds());

            if (index < 0 || index >= adList.size()) {
                sender.sendMessage(ChatColor.RED + "Invalid ad number!");
                return true;
            }

            AdRequest ad = adList.get(index);
            adManager.approveAd(ad);

            sender.sendMessage(ChatColor.GREEN + "Ad approved and activated!");

            Player player = plugin.getServer().getPlayer(ad.getPlayerName());
            if (player != null) {
                player.sendMessage(ChatColor.GREEN + "Your ad has been approved and is now active!");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number!");
        }

        return true;
    }

    private boolean handleDenyAd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("billboard.moderate")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to deny ads!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /denyad <#>");
            return true;
        }

        try {
            int index = Integer.parseInt(args[0]) - 1;
            List<AdRequest> adList = new ArrayList<>(adManager.getPendingAds());

            if (index < 0 || index >= adList.size()) {
                sender.sendMessage(ChatColor.RED + "Invalid ad number!");
                return true;
            }

            AdRequest ad = adList.get(index);
            adManager.denyAd(ad);

            sender.sendMessage(ChatColor.GREEN + "Ad denied and removed.");

            Player player = plugin.getServer().getPlayer(ad.getPlayerName());
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Your ad was denied by a moderator.");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number!");
        }

        return true;
    }

    private boolean handleListAds(CommandSender sender) {
        if (!sender.hasPermission("billboard.moderate")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list ads!");
            return true;
        }

        if (adManager.getActiveAds().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No active ads.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Active Ads ===");
        for (ActiveAd ad : adManager.getActiveAds()) {
            long remainingDays = ad.getRemainingDays();
            long remainingHours = ad.getRemainingHours();

            String timeLeft = remainingDays > 0 ?
                    remainingDays + " days" : remainingHours + " hours";

            sender.sendMessage(ChatColor.YELLOW + ad.getPlayerName() + ChatColor.GRAY + " - " +
                    ChatColor.WHITE + timeLeft + " left");
            sender.sendMessage(ChatColor.GRAY + "  " +
                    translateColorCodes(ad.getMessage()));
        }

        return true;
    }

    private String translateColorCodes(String text) {
        return text.replace("&0", ChatColor.BLACK.toString())
                .replace("&1", ChatColor.DARK_BLUE.toString())
                .replace("&2", ChatColor.DARK_GREEN.toString())
                .replace("&3", ChatColor.DARK_AQUA.toString())
                .replace("&4", ChatColor.DARK_RED.toString())
                .replace("&5", ChatColor.DARK_PURPLE.toString())
                .replace("&6", ChatColor.GOLD.toString())
                .replace("&7", ChatColor.GRAY.toString())
                .replace("&8", ChatColor.DARK_GRAY.toString())
                .replace("&9", ChatColor.BLUE.toString())
                .replace("&a", ChatColor.GREEN.toString())
                .replace("&b", ChatColor.AQUA.toString())
                .replace("&c", ChatColor.RED.toString())
                .replace("&d", ChatColor.LIGHT_PURPLE.toString())
                .replace("&e", ChatColor.YELLOW.toString())
                .replace("&f", ChatColor.WHITE.toString());
    }
}