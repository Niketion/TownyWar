package io.github.niketion.townywar.commands;

import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.TownGrace;
import io.github.niketion.townywar.structure.TownWar;
import io.github.niketion.townywar.structure.War;
import io.github.niketion.townywar.structure.handlers.WarHandler;
import io.github.niketion.townywar.utils.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class WarAdminCommand implements CommandExecutor {
    private final TownyWarPlugin plugin;

    public WarAdminCommand(TownyWarPlugin plugin) {
        plugin.getCommand("waradmin").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(usage());
            return true;
        }

        WarHandler handler = plugin.getWarHandler();

        switch (args[0].toLowerCase()) {
            case "stopall": {
                plugin.getWarHandler().getWars().forEach(((s, war) -> {
                    war.sendAllMessage(plugin.getConfigValues().getAdminStopAll().replace("%name%", sender.getName()));
                    war.end();
                }));

                return true;
            }
            case "forceend": {
                if (handler.getWars().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "There are no wars in progress.");
                    return true;
                }

                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.GOLD + "Wars in progress:");
                    for (War value : handler.getWars().values()) {
                        sender.sendMessage(
                                ChatColor.YELLOW + "* Attacker: " + value.getAttacker().getTown().getName()
                                + " - Defender: " + value.getDefender().getTown().getName()
                        );
                    }
                    sender.sendMessage(ChatColor.GRAY + "To end a war, use /waradmin forceend <attacker>");
                    return true;
                }

                War war = handler.getWars().get(args[1].toLowerCase());
                if (war == null) {
                    sender.sendMessage(ChatColor.RED + "I didn't find any war with this attacker, are you sure you wrote correctly?");
                    return true;
                }

                war.sendAllMessage(plugin.getConfigValues().getAdminEnd().replace("%name%", sender.getName()));
                war.end();
                sender.sendMessage(ChatColor.GREEN + "War concluded.");
                return true;
            }
            case "forcewin": {
                if (handler.getWars().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "There are no wars in progress.");
                    return true;
                }

                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.GOLD + "Wars in progress:");
                    for (War value : handler.getWars().values()) {
                        sender.sendMessage(
                                ChatColor.YELLOW + "* Attacker: " + value.getAttacker().getTown().getName()
                                        + " - Defender: " + value.getDefender().getTown().getName()
                        );
                    }
                    sender.sendMessage(ChatColor.GRAY + "To forcewin a war, use /waradmin forcewin <target>");
                    return true;
                }

                TownWar townWar = handler.getTownWar(args[1].toLowerCase());
                if (townWar == null) {
                    sender.sendMessage(ChatColor.RED + "I didn't find any war with this target, are you sure you wrote correctly?");
                    return true;
                }

                War war = handler.getWar(townWar);
                if (war == null) {
                    war = handler.getWar(townWar.getOpponent());
                    if (war == null) {
                        sender.sendMessage(ChatColor.RED + "I didn't find any war with this target, are you sure you wrote correctly?");
                        return true;
                    }
                }

                war.sendAllMessage(plugin.getConfigValues().getAdminWin()
                        .replace("%name%", sender.getName())
                        .replace("%town%", townWar.getTown().getName()));

                war.win(townWar);
                sender.sendMessage(ChatColor.GREEN + "War concluded.");
                return true;
            }
            case "graces": {
                ConfigValues config = plugin.getConfigValues();
                sender.sendMessage(ChatColor.GOLD + "List all graces:");
                for (TownGrace value : handler.getGraces().values()) {
                    long minuteRemaining = TimeUnit.MILLISECONDS.toMinutes(config.getGracePeriodLosersHour()*60*60*1000L
                            -(System.currentTimeMillis() - value.getStartGrace()));
                    sender.sendMessage(ChatColor.YELLOW + " * " + value.getTown().getName() + ": " + minuteRemaining + " minutes");
                }
                return true;
            }
            case "reload": {
                sender.sendMessage(ChatColor.RED + "Reloaded...");
                plugin.reloadConfigValues();
                return true;
            }
        }
        sender.sendMessage(usage());
        return true;
    }

    private String[] usage() {
        return new String[] {
                ChatColor.GOLD + "Usage /waradmin command:",
                ChatColor.YELLOW + "/waradmin reload - Reload config.yml",
                ChatColor.YELLOW + "/waradmin stopall - Stop all wars",
                ChatColor.YELLOW + "/waradmin forceend <attacker> - Force end a war",
                ChatColor.YELLOW + "/waradmin forcewin <target> - Force win (and end) a war",
                ChatColor.YELLOW + "/waradmin graces - See all towns in grace"
        };
    }
}
