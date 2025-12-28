package org.garsooon.billboard.data;

import org.bukkit.ChatColor;
import org.garsooon.billboard.billboard;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "CallToPrintStackTrace"})
public class AutoBroadcaster {

    private final billboard plugin;
    private final ConfigManager configManager;
    private final File broadcastFile;
    private List<String> messages;
    private int currentMessageIndex = 0;
    private int taskId = -1;

    public AutoBroadcaster(billboard plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messages = new ArrayList<>();
        this.broadcastFile = new File(plugin.getDataFolder(), "broadcasts.yml");

        initializeBroadcastsFile();
        loadBroadcasts();
    }

    private void initializeBroadcastsFile() {
        if (!broadcastFile.exists()) {
            try {
                broadcastFile.createNewFile();

                Map<String, Object> data = new HashMap<>();
                data.put("interval", 300);

                List<String> defaultMessages = new ArrayList<>();
                defaultMessages.add("&eWelcome to the server! Type /help for commands.");
                defaultMessages.add("&aVisit our website at example.com");
                defaultMessages.add("&6Use /buyad to advertise your shop or service!");
                defaultMessages.add("&dJoin our Discord community!");

                data.put("messages", defaultMessages);

                saveYamlFile(broadcastFile, data);

                plugin.getServer().getLogger().info("[Billboard] Created default broadcasts.yml file");
            } catch (IOException e) {
                plugin.getServer().getLogger().severe("[Billboard] Could not create broadcasts.yml!");
                e.printStackTrace();
            }
        }
    }

    public void loadBroadcasts() {
        if (!broadcastFile.exists()) {
            plugin.getServer().getLogger().warning("[Billboard] broadcasts.yml not found!");
            return;
        }

        try {
            Yaml yaml = new Yaml();
            FileInputStream fis = new FileInputStream(broadcastFile);
            Object loadedObj = yaml.load(fis);
            fis.close();

            if (loadedObj instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) loadedObj;

                if (data.containsKey("messages") && data.get("messages") instanceof List) {
                    messages.clear();
                    List<?> messageList = (List<?>) data.get("messages");
                    for (Object msg : messageList) {
                        if (msg instanceof String) {
                            messages.add((String) msg);
                        }
                    }
                }
                plugin.getServer().getLogger().info("[Billboard] Loaded " + messages.size() + " broadcast messages");
            }
        } catch (IOException e) {
            plugin.getServer().getLogger().severe("[Billboard] Could not load broadcasts.yml!");
            e.printStackTrace();
        }
    }

    public void reloadBroadcasts() {
        stop();
        loadBroadcasts();
        currentMessageIndex = 0;
        start();
        plugin.getServer().getLogger().info("[Billboard] Broadcasts reloaded!");
    }

    public void start() {
        if (messages.isEmpty()) {
            plugin.getServer().getLogger().warning("[Billboard] No broadcast messages configured in broadcasts.yml!");
            return;
        }

        int interval = getIntervalFromFile();
        long intervalTicks = interval * 20L; // Convert seconds to ticks

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                plugin,
                this::broadcastNextMessage,
                intervalTicks,
                intervalTicks
        );

        plugin.getServer().getLogger().info("[Billboard] AutoBroadcaster started with " + messages.size() + " messages (interval: " + interval + "s)");
    }

    public void stop() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
            plugin.getServer().getLogger().info("[Billboard] AutoBroadcaster stopped");
        }
    }

    public void restart() {
        stop();
        currentMessageIndex = 0;
        start();
    }

    private void broadcastNextMessage() {
        if (messages.isEmpty()) {
            return;
        }

        String message = messages.get(currentMessageIndex);
        String coloredMessage = translateColorCodes(message);

        plugin.getServer().broadcastMessage(coloredMessage);

        currentMessageIndex = (currentMessageIndex + 1) % messages.size();
    }

    public boolean isRunning() {
        return taskId != -1;
    }

    public int getCurrentMessageIndex() {
        return currentMessageIndex;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public int getMessageCount() {
        return messages.size();
    }

    private int getIntervalFromFile() {
        try {
            Yaml yaml = new Yaml();
            FileInputStream fis = new FileInputStream(broadcastFile);
            Object loadedObj = yaml.load(fis);
            fis.close();

            if (loadedObj instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) loadedObj;
                if (data.containsKey("interval")) {
                    Object intervalObj = data.get("interval");
                    if (intervalObj instanceof Number) {
                        return ((Number) intervalObj).intValue();
                    }
                }
            }
        } catch (IOException e) {
            plugin.getServer().getLogger().warning("[Billboard] Could not read interval from broadcasts.yml, using default 300s");
        }

        return 300;
    }

    private void saveYamlFile(File file, Map<String, Object> data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            if (data.containsKey("interval")) {
                writer.write("interval: " + data.get("interval"));
                writer.newLine();
            }

            if (data.containsKey("messages") && data.get("messages") instanceof List) {
                writer.write("messages:");
                writer.newLine();

                List<?> messageList = (List<?>) data.get("messages");
                for (Object msg : messageList) {
                    if (msg instanceof String) {
                        writer.write("  - '" + ((String) msg).replace("'", "''") + "'");
                        writer.newLine();
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
            plugin.getServer().getLogger().severe("[Billboard] Could not save " + file.getName() + "!");
            e.printStackTrace();
        }
    }

    //TODO put all my util classes into my plugin template
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