package io.github.niketion.townywar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.TownWar;
import io.github.niketion.townywar.structure.War;
import io.github.niketion.townywar.structure.handlers.WarHandler;
import io.github.niketion.townywar.utils.TownyUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListeners implements Listener {
    private final TownyWarPlugin plugin;

    public BlockListeners(TownyWarPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(TownyDestroyEvent event) {
        WarHandler handler = plugin.getWarHandler();

        Block block = event.getBlock();
        Town town = TownyAPI.getInstance().getTown(block.getLocation());
        if (town == null) return;

        TownWar townWar = handler.getTownWar(town.getName());
        if (townWar == null) return;

        War war = handler.getWar(townWar);
        if (war == null) return;

        Player player = event.getPlayer();
        Town townPlayer = TownyUtils.getTownFromPlayer(player);
        if (townPlayer == null || townPlayer.getName().equals(town.getName())) return;

        if (war.getState() == War.State.PREPARATION) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getConfigValues().getWhitelistBlockBreakWar()
                .stream()
                .noneMatch(
                        material -> block.getType() == material
                )) {
            event.setMessage(null);
            event.setCancelled(true);
            return;
        }

        war.logBreak(block);
        event.setMessage(null);
        event.getBlock().setType(Material.AIR);
        event.setCancelled(true);
    }

    @EventHandler
    public void on(TownyBuildEvent event) {
        WarHandler handler = plugin.getWarHandler();

        Block block = event.getBlock();
        Town town = TownyAPI.getInstance().getTown(block.getLocation());
        if (town == null) return;

        TownWar townWar = handler.getTownWar(town.getName());
        if (townWar == null) return;

        War war = handler.getWar(townWar);
        if (war == null) return;

        Player player = event.getPlayer();
        Town townPlayer = TownyUtils.getTownFromPlayer(player);
        if (townPlayer == null || townPlayer.getName().equals(town.getName())) return;

        if (war.getState() == War.State.PREPARATION) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getConfigValues().getWhitelistBlockPlaceWar()
                .stream()
                .noneMatch(
                        material -> block.getType() == material
                )) {
            event.setMessage(null);
            event.setCancelled(true);
            return;
        }

        war.logPlace(block);
        event.setMessage(null);
        event.setCancelled(false);
    }

}
