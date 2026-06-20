package com.diasmp.diaswords;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles /diaswords give <player> <sword>, /diaswords list, and /diaswords reload.
 */
public class DiaSwordsCommand implements CommandExecutor, TabCompleter {

    private final DiaSwords plugin;

    public DiaSwordsCommand(DiaSwords plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("diaswords.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /diaswords give <player> <sword>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + args[1] + "' is not online.", NamedTextColor.RED));
            return;
        }

        SwordType type = SwordType.fromName(args[2]);
        if (type == null) {
            sender.sendMessage(Component.text("Unknown sword type: " + args[2], NamedTextColor.RED));
            sender.sendMessage(Component.text("Available: dash, lightning, vampire, frost, vortex, explosive", NamedTextColor.GRAY));
            return;
        }

        target.getInventory().addItem(plugin.getSwordManager().createSword(type));
        sender.sendMessage(Component.text("Gave " + target.getName() + " a " + type.name().toLowerCase() + " sword.", NamedTextColor.GREEN));
        if (!sender.equals(target)) {
            target.sendMessage(Component.text("You received a custom " + type.name().toLowerCase() + " sword!", NamedTextColor.GREEN));
        }
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(Component.text("=== DiaSwords ===", NamedTextColor.GOLD));
        for (SwordType type : SwordType.values()) {
            sender.sendMessage(Component.text("- " + type.name().toLowerCase(), NamedTextColor.AQUA));
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(Component.text("DiaSwords config reloaded.", NamedTextColor.GREEN));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/diaswords give <player> <sword>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/diaswords list", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/diaswords reload", NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("diaswords.admin")) return List.of();

        if (args.length == 1) {
            return filter(Stream.of("give", "list", "reload"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(Stream.of(SwordType.values()).map(t -> t.name().toLowerCase()), args[2]);
        }
        return List.of();
    }

    private List<String> filter(Stream<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
