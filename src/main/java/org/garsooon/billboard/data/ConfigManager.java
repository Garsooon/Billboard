package org.garsooon.billboard.data;

import org.garsooon.billboard.billboard;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final billboard plugin;
    private File configFile;
    private Map<String, Object> config;

    // Default values
    private int broadcastInterval = 300;
    private String broadcastHeader = "&6[AD]";
    private int maxDurationDays = 30;
    private int maxMessageLength = 100;

    public ConfigManager(billboard plugin) {
        this.plugin = plugin;
        this.config = new HashMap<>();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
            return;
        }

        config = loadYamlFile(configFile);

        if (config == null || config.isEmpty()) {
            config = new HashMap<>();
            createDefaultConfig();
            return;
        }

        broadcastInterval = getInt("broadcast-interval", broadcastInterval);
        broadcastHeader = getString(broadcastHeader);
        maxDurationDays = getInt("max-duration-days", maxDurationDays);
        maxMessageLength = getInt("max-message-length", maxMessageLength);
        plugin.getServer().getLogger().info("[Billboard] Configuration loaded successfully");
    }

    private void createDefaultConfig() {
        config.put("broadcast-interval", broadcastInterval);
        config.put("broadcast-header", broadcastHeader);
        config.put("max-duration-days", maxDurationDays);
        config.put("max-message-length", maxMessageLength);

        saveYamlFile(configFile, config);
        plugin.getServer().getLogger().info("[Billboard] Created default config.yml");
    }


    private String getString(String defaultValue) {
        if (config.containsKey("broadcast-header")) {
            Object val = config.get("broadcast-header");
            if (val instanceof String) {
                return (String) val;
            }
        }
        return defaultValue;
    }

    private int getInt(String key, int defaultValue) {
        if (config.containsKey(key)) {
            Object val = config.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
        }
        return defaultValue;
    }

    public int getBroadcastInterval() {
        return broadcastInterval;
    }

    public String getBroadcastHeader() {
        return broadcastHeader;
    }

    public int getMaxDurationDays() {
        return maxDurationDays;
    }

    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    ///util
    public Map<String, Object> loadYamlFile(File file) {
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }

        try {
            Yaml yaml = new Yaml();
            FileInputStream fis = new FileInputStream(file);
            Object loadedObj = yaml.load(fis);
            fis.close();

            if (loadedObj instanceof Map) {
                return (Map<String, Object>) loadedObj;
            }

            return new HashMap<>();

        } catch (IOException e) {
            plugin.getServer().getLogger().severe("[Billboard] Could not load " + file.getName() + "!");
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void saveYamlFile(File file, Map<String, Object> data) {
        try {
            Yaml yaml = new Yaml();
            FileWriter writer = new FileWriter(file);
            yaml.dump(data, writer);
            writer.close();
        } catch (IOException e) {
            plugin.getServer().getLogger().severe("[Billboard] Could not save " + file.getName() + "!");
            e.printStackTrace();
        }
    }
}