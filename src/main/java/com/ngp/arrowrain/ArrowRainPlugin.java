package com.ngp.arrowrain;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ArrowRainPlugin extends JavaPlugin {
    private final Map<String, ScheduledTask> activeRains = new HashMap<>();
    private static final int BSTATS_ID = 30064;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        new Metrics(this, BSTATS_ID);

        UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.check();
        getServer().getPluginManager().registerEvents(updateChecker, this);

        getCommand("arrowrain").setExecutor(new ArrowRainCommand(this));
        getCommand("arrowrain").setTabCompleter(new ArrowRainTabComplete(this));
        getLogger().info("ArrowRain loaded!");
    }

    public Map<String, ScheduledTask> getActiveRains() {
        return activeRains;
    }

    public String getMessage(String key) {
        String lang = getConfig().getString("language", "en");
        String path = "messages." + lang + "." + key;
        String fallback = "messages.en." + key;
        String raw = getConfig().getString(path, getConfig().getString(fallback, "&cMissing message: " + key));
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
