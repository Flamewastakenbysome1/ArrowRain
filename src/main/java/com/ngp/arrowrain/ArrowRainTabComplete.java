package com.ngp.arrowrain;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArrowRainTabComplete implements TabCompleter {
    private final ArrowRainPlugin plugin;

    public ArrowRainTabComplete(ArrowRainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String typed = args[0].toLowerCase();
            List<String> options = new ArrayList<>();
            options.add("stop");
            options.add("list");
            Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .forEach(options::add);
            return options.stream()
                .filter(s -> s.toLowerCase().startsWith(typed))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String typed = args[1].toLowerCase();
            if (args[0].equalsIgnoreCase("stop")) {
                return plugin.getActiveRains().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(typed))
                    .collect(Collectors.toList());
            }
            return Arrays.asList("10", "30", "60", "120", "300").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
        }

        if (args.length == 3) {
            String typed = args[2].toLowerCase();
            return Arrays.asList("true", "false").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
        }

        if (args.length == 4) {
            String typed = args[3].toLowerCase();
            return Arrays.asList("follow", "static").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
        }

        if (args.length == 5) {
            String typed = args[4];
            return Arrays.asList("5", "10", "15", "20", "30").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
        }

        if (args.length == 6) {
            String typed = args[5];
            return Arrays.asList("5", "10", "20", "30", "50").stream()
                .filter(s -> s.startsWith(typed))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
