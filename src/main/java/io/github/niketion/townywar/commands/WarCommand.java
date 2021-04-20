package io.github.niketion.townywar.commands;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.TownWar;
import io.github.niketion.townywar.structure.War;
import io.github.niketion.townywar.structure.handlers.WarHandler;
import io.github.niketion.townywar.utils.ConfigValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarCommand implements CommandExecutor {
    private final TownyWarPlugin plugin;

    public WarCommand(TownyWarPlugin plugin) {
        plugin.getCommand("war").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        WarHandler warHandler = plugin.getWarHandler();
        ConfigValues config = plugin.getConfigValues();

        if (!player.hasPermission("war.opengui")) {
            player.sendMessage(config.getNoPermission());
            return true;
        }

        TownWar town = warHandler.getTownWar(player.getUniqueId());

        if (town == null) {
            player.sendMessage(config.getResidentErrorMessage());
            return true;
        }

        if (strings.length == 0) {
            town.selection(player);
            return true;
        }

        switch (strings[0].toLowerCase()) {
            case "declare": {
                if (!player.hasPermission("war.declare")) {
                    player.sendMessage(config.getNoPermission());
                    return true;
                }

                if (strings.length <= 1) {
                    player.sendMessage(config.getUsageWarCommand());
                    return true;
                }

                War war = warHandler.getWar(town);
                if (war != null) {
                    player.sendMessage(config.getAlreadyInWar());
                    return true;
                }

                warHandler.declare(player, town, warHandler.getTownWar(strings[1]));
                return true;
            }
            case "invite": {
                if (!player.hasPermission("war.inviteally")) {
                    player.sendMessage(config.getNoPermission());
                    return true;
                }

                if (strings.length <= 2 || !strings[1].equalsIgnoreCase("ally")) {
                    player.sendMessage(config.getUsageWarCommand());
                    return true;
                }

                TownWar allyTownWar = warHandler.getTownWar(strings[2]);
                if (allyTownWar == null) {
                   player.sendMessage(config.getTownNotFound());
                   return true;
                }

                War war = warHandler.getWar(town);
                if (war == null) {
                    player.sendMessage(config.getTownNotInWar());
                    return true;
                }

                if (war.getState() != War.State.PREPARATION) {
                    player.sendMessage(config.getActionPreWar());
                    return true;
                }

                if (!town.inviteAlly(allyTownWar.getTown(), war)) {
                    player.sendMessage(config.getTownNotAlly());
                }
                return true;
            }
            case "accept": {
                if (!player.hasPermission("war.acceptallyinvite")) {
                    player.sendMessage(config.getNoPermission());
                    return true;
                }

                if (strings.length <= 2) {
                    player.sendMessage(config.getUsageWarCommand());
                    return true;
                }

                War war = warHandler.getWar(town);
                if (war == null) {
                    player.sendMessage(config.getTownNotInWar());
                    return true;
                }

                if (war.getState() != War.State.PREPARATION) {
                    player.sendMessage(config.getActionPreWar());
                    return true;
                }

                TownWar target = plugin.getWarHandler().getTownWar(strings[2]);
                if (target == null) {
                    player.sendMessage(config.getTownNotFound());
                    return true;
                }

                if (!town.acceptInvite(target)) {
                    player.sendMessage(config.getNoInviteToAccept());
                }
                return true;
            }
            case "setrespawnchunk": {
                if (!player.hasPermission("war.setrespawnchunk")) {
                    player.sendMessage(config.getNoPermission());
                    return true;
                }

                town.setRespawnLocation(player.getLocation());
                player.sendMessage(config.getRespawnChunkCommand());
                return true;
            }
        }
        player.sendMessage(config.getUsageWarCommand());
        return true;
    }
}
