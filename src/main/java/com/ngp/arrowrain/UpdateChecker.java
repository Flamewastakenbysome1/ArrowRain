package com.ngp.arrowrain;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker implements Listener {
    private final ArrowRainPlugin plugin;
    private static final String MODRINTH_PROJECT_ID = "J34a3MvD";
    private String latestVersion = null;

    public UpdateChecker(ArrowRainPlugin plugin) {
        this.plugin = plugin;
    }

    public void check() {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID + "/version");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "ArrowRain/" + plugin.getDescription().getVersion());
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                String json = response.toString();
                // Grab the first version_number from the response
                int idx = json.indexOf("\"version_number\":\"");
                if (idx != -1) {
                    int start = idx + 18;
                    int end = json.indexOf("\"", start);
                    latestVersion = json.substring(start, end);

                    String current = plugin.getDescription().getVersion();
                    if (!latestVersion.equals(current)) {
                        plugin.getLogger().info("A new update is available: " + latestVersion);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not check for updates: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp() || latestVersion == null) return;
        String current = plugin.getDescription().getVersion();
        if (!latestVersion.equals(current)) {
            player.sendMessage(plugin.getMessage("update-available")
                .replace("{version}", latestVersion));
        }
    }
}
