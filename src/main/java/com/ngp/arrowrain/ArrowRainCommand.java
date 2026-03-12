package com.ngp.arrowrain;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;

public class ArrowRainCommand implements CommandExecutor {
    private final ArrowRainPlugin plugin;

    public ArrowRainCommand(ArrowRainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // /arrowrain list
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            if (plugin.getActiveRains().isEmpty()) {
                sender.sendMessage(plugin.getMessage("no-active-rains"));
            } else {
                sender.sendMessage(plugin.getMessage("active-rains")
                    .replace("{list}", String.join(", ", plugin.getActiveRains().keySet())));
            }
            return true;
        }

        // /arrowrain stop <player>
        if (args.length >= 2 && args[0].equalsIgnoreCase("stop")) {
            String targetName = args[1];
            ScheduledTask task = plugin.getActiveRains().remove(targetName);
            if (task == null) {
                sender.sendMessage(plugin.getMessage("no-rain-for-player")
                    .replace("{player}", targetName));
            } else {
                task.cancel();
                sender.sendMessage(plugin.getMessage("rain-stopped-sender")
                    .replace("{player}", targetName));
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) target.sendMessage(plugin.getMessage("rain-stopped-target"));
            }
            return true;
        }

        // /arrowrain <player> <seconds> [notify] [follow/static] [radius] [density]
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("usage"));
            sender.sendMessage(plugin.getMessage("usage-stop"));
            sender.sendMessage(plugin.getMessage("usage-list"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return true;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("seconds-not-number"));
            return true;
        }

        // Config defaults
        int maxDuration = plugin.getConfig().getInt("max-duration", 300);
        double configRadius = plugin.getConfig().getDouble("default-radius", 24);
        double height = plugin.getConfig().getDouble("default-height", 28);
        double damage = plugin.getConfig().getDouble("arrow-damage", 4.0);
        int configDensity = plugin.getConfig().getInt("default-density", 10);

        // Cap duration
        if (seconds > maxDuration) {
            sender.sendMessage(plugin.getMessage("rain-capped")
                .replace("{max}", String.valueOf(maxDuration)));
            seconds = maxDuration;
        }

        boolean notify = args.length >= 3 && args[2].equalsIgnoreCase("true");
        boolean staticMode = args.length >= 4 && args[3].equalsIgnoreCase("static");

        // Radius arg
        double radius = configRadius;
        if (args.length >= 5) {
            try {
                radius = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("radius-not-number"));
                return true;
            }
        }

        // Density arg
        int density = configDensity;
        if (args.length >= 6) {
            try {
                density = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("density-not-number"));
                return true;
            }
        }

        final double finalRadius = radius;
        final int finalDensity = density;
        final int finalSeconds = seconds;

        // Cancel existing rain on target if any
        ScheduledTask existing = plugin.getActiveRains().remove(target.getName());
        if (existing != null) existing.cancel();

        Location center = target.getLocation().clone();
        World world = target.getWorld();
        int totalTicks = finalSeconds * 20;
        int ticksPerArrow = Math.max(1, 20 / finalDensity);
        AtomicInteger ticksElapsed = new AtomicInteger(0);

        sender.sendMessage(plugin.getMessage("rain-started")
            .replace("{player}", target.getName())
            .replace("{seconds}", String.valueOf(finalSeconds)));
        if (notify) target.sendMessage(plugin.getMessage("rain-incoming"));

        ScheduledTask task = Bukkit.getRegionScheduler().runAtFixedRate(plugin, center, t -> {
            if (ticksElapsed.get() >= totalTicks) {
                t.cancel();
                plugin.getActiveRains().remove(target.getName());
                if (notify) target.sendMessage(plugin.getMessage("rain-ended"));
                return;
            }

            if (!target.isOnline()) {
                t.cancel();
                plugin.getActiveRains().remove(target.getName());
                return;
            }

            Location spawnOrigin = staticMode ? center : target.getLocation().clone();
            double x = (Math.random() - 0.5) * finalRadius * 2;
            double z = (Math.random() - 0.5) * finalRadius * 2;
            Location spawnLoc = spawnOrigin.clone().add(x, height, z);

            Arrow arrow = (Arrow) world.spawnEntity(spawnLoc, EntityType.ARROW);
            arrow.setFireTicks(32767);
            arrow.setVelocity(new Vector(
                (Math.random() - 0.5) * 0.3,
                -2.5,
                (Math.random() - 0.5) * 0.3
            ));
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            arrow.setDamage(damage);

            ticksElapsed.addAndGet(ticksPerArrow);
        }, 1L, ticksPerArrow);

        plugin.getActiveRains().put(target.getName(), task);
        return true;
    }
}
