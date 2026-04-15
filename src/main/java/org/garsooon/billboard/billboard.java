package org.garsooon.billboard;

import org.bukkit.plugin.java.JavaPlugin;
import org.garsooon.billboard.Economy.Method;
import org.garsooon.billboard.Economy.Methods;
import org.garsooon.billboard.commands.AdCommands;
import org.garsooon.billboard.commands.BroadcastCommands;
import org.garsooon.billboard.data.AdManager;
import org.garsooon.billboard.data.AutoBroadcaster;
import org.garsooon.billboard.data.ConfigManager;
import org.garsooon.billboard.listeners.PlayerJoinListener;

import static org.bukkit.Bukkit.getLogger;

public class billboard extends JavaPlugin {

    private AdManager adManager;
    private ConfigManager configManager;
    private AutoBroadcaster autobroadcaster;
    private int broadcastTaskId;
    private int tickTaskId;
    private Method economy;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        getLogger().info("[Billboard] has been enabled!");

        boolean economyLoaded = Methods.setMethod(getServer().getPluginManager());
        if (!economyLoaded || Methods.getMethod() == null) {
            getLogger().severe("[Billboard] No compatible economy plugin found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.economy = Methods.getMethod();
        getLogger().info("[Billboard] Economy method loaded: " + economy.getName() + " v" + economy.getVersion());

        adManager = new AdManager(this, configManager, economy);
        adManager.loadData();

        AdCommands adCommands = new AdCommands(this, adManager, configManager);

        getCommand("buyad").setExecutor(adCommands);
        getCommand("reviewads").setExecutor(adCommands);
        getCommand("approvead").setExecutor(adCommands);
        getCommand("denyad").setExecutor(adCommands);
        getCommand("listads").setExecutor(adCommands);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Start broadcast task for ads
        long interval = configManager.getBroadcastInterval() * 20L;
        broadcastTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> adManager.broadcastRandomAd(getServer(), configManager), interval, interval);

        // Start tick task to remove expired ads (checks every 5 minutes)
        tickTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> adManager.removeExpiredAds(getLogger()), 6000L, 6000L);

        // Start broadcast task for server broadcasts
        autobroadcaster = new AutoBroadcaster(this, configManager);
        autobroadcaster.start();

        getCommand("broadcast").setExecutor(new BroadcastCommands(autobroadcaster));
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(broadcastTaskId);
        getServer().getScheduler().cancelTask(tickTaskId);

        if (autobroadcaster != null) {
            autobroadcaster.stop();
        }

        adManager.saveData();
        getLogger().info("[Billboard] has been disabled!");
    }

    public AdManager getAdManager() {
        return adManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AutoBroadcaster getAutobroadcaster() {
        return autobroadcaster;
    }

    public Method getEconomy() {return economy;}
}