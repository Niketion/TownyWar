package io.github.niketion.townywar.listeners;

import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.Participant;
import io.github.niketion.townywar.structure.TownWar;
import io.github.niketion.townywar.structure.War;
import io.github.niketion.townywar.structure.handlers.ParticipantHandler;
import io.github.niketion.townywar.utils.ConfigValues;
import io.github.niketion.townywar.utils.TownyUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListeners implements Listener {
    private final TownyWarPlugin plugin;

    public PlayerListeners(TownyWarPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        Participant participant = plugin.getParticipantHandler().getParticipant(event.getEntity().getUniqueId());
        if (participant == null) return;

        War war = plugin.getWarHandler().getWar(participant.getTown());
        if (war == null) return;

        participant.getTown().registerKill();

        if (participant.getTown().getLives() == 0)
            war.win(participant.getTown().getOpponent());
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ParticipantHandler handler = plugin.getParticipantHandler();

        Participant participant = handler.getParticipant(player.getUniqueId());
        if (participant == null) return;

        if (TownyUtils.getResidentsOnline(participant.getTown().getTown()).size() == 1) {
            War war = plugin.getWarHandler().getWar(participant.getTown());
            if (war == null) return;

            war.win(participant.getTown().getOpponent());
        }
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Participant participant = plugin.getParticipantHandler().getParticipant(player.getUniqueId());
        if (participant == null) return;

        War war = plugin.getWarHandler().getWar(participant.getTown());
        if (war == null) return;

        ConfigValues config = plugin.getConfigValues();

        String command = event.getMessage().toLowerCase();
        if (command.split(" ").length > 0) {
            command = command.split(" ")[0];
        }

        if (war.getState() == War.State.GAME) {
            for (String blacklist : config.getBlacklistCommandWar()) {
                if (("/" + blacklist.toLowerCase()).equals(command)) {
                    event.setCancelled(true);
                    player.sendMessage(config.getBlacklistMessage());
                    return;
                }
            }
        } else {
            for (String blacklist : config.getBlacklistCommandPrewar()) {
                if (("/" + blacklist.toLowerCase()).equals(command)) {
                    event.setCancelled(true);
                    player.sendMessage(config.getBlacklistMessagePrewar());
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Participant participant = plugin.getParticipantHandler().getParticipant(player.getUniqueId());
        if (participant == null) return;

        War war = plugin.getWarHandler().getWar(participant.getTown());
        if (war == null) return;

        event.setRespawnLocation(participant.getTeleportLocation());
        Bukkit.getScheduler().runTask(plugin, participant::giveEffects);
    }

    @EventHandler
    public void on(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (plugin.getParticipantHandler().hasInvincibility(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        ParticipantHandler participantHandler = plugin.getParticipantHandler();

        Participant targetParticipant = participantHandler.getParticipant(target.getUniqueId());
        if (targetParticipant == null) return;

        Participant participant = participantHandler.getParticipant(player.getUniqueId());
        if (participant == null) {
            event.setCancelled(true);
            return;
        }

        War war = plugin.getWarHandler().getWar(participant.getTown());
        if (war == null) return;

        War warTarget = plugin.getWarHandler().getWar(targetParticipant.getTown());
        if (war != warTarget) {
            event.setCancelled(true);
            return;
        }

        if (war.getState() == War.State.PREPARATION) {
            event.setCancelled(true);
            return;
        }

        if (participantHandler.hasInvincibility(player.getUniqueId())) {
            participantHandler.removeInvincibility(participant);
        }
    }
}
