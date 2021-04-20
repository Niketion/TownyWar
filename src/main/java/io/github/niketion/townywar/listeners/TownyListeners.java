package io.github.niketion.townywar.listeners;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import io.github.niketion.townywar.TownyWarPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyListeners implements Listener {
    private final TownyWarPlugin plugin;

    public TownyListeners(TownyWarPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(NewTownEvent event) {
        plugin.getWarHandler().createTown(event.getTown());
    }

    @EventHandler
    public void on(DeleteTownEvent event) {
        plugin.getWarHandler().removeTown(event.getTownName());
    }
}
