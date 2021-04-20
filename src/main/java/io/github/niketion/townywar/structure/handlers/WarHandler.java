package io.github.niketion.townywar.structure.handlers;

import com.google.common.collect.Maps;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.TownGrace;
import io.github.niketion.townywar.structure.TownWar;
import io.github.niketion.townywar.structure.War;
import io.github.niketion.townywar.utils.ConfigValues;
import io.github.niketion.townywar.utils.LocationUtils;
import io.github.niketion.townywar.utils.TownyUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class WarHandler {
    private final TownyWarPlugin plugin;
    private final Map<String, TownGrace> graces;
    private final Map<String, War> wars;
    private final Map<String, TownWar> townWars;

    public WarHandler(TownyWarPlugin plugin) {
        this.wars = Maps.newConcurrentMap();
        this.graces = Maps.newHashMap();
        this.townWars = Maps.newHashMap();
        this.plugin = plugin;
    }

    public void init() {
        TownyUniverse.getInstance()
                .getTowns()
                .forEach(town -> {
                    String locationString = plugin.getConfigLocations()
                            .getConfig().getString(town.getName().toLowerCase());

                    Location location = null;
                    if (locationString != null) {
                        location = LocationUtils.getLiteLocationFromString(locationString);
                    }

                    TownWar townWar = new TownWar(this, town, location);
                    townWars.put(town.getName().toLowerCase(), townWar);
                });
    }
    
    public void createTown(Town town) {
        townWars.put(town.getName().toLowerCase(), new TownWar(this, town, null));
    }
    
    public void removeTown(String town) {
        townWars.remove(town.toLowerCase());

        plugin.getConfigLocations().getConfig().set(town.toLowerCase(), null);
        plugin.getConfigLocations().saveConfig();
    }

    public void addGrace(Town town) {
        graces.put(town.getName().toLowerCase(), new TownGrace(town));
    }

    public boolean hasGrace(Town town) {
        TownGrace townGrace = graces.get(town.getName().toLowerCase());
        if (townGrace == null) return false;

        if (System.currentTimeMillis()-townGrace.getStartGrace()>=plugin.getConfigValues().getGracePeriodLosersHour()*1000L*60*60) {
            graces.remove(town.getName().toLowerCase());
            return false;
        }
        return true;
    }

    @Nullable
    public TownWar getTownWar(String name) {
        return townWars.get(name.toLowerCase());
    }

    @Nullable
    public War getWar(TownWar war) {
        if (wars.containsKey(war.getTown().getName().toLowerCase())) {
            return wars.get(war.getTown().getName().toLowerCase());
        }

        if (war.getOpponent() != null && wars.containsKey(war.getOpponent().getTown().getName().toLowerCase())) {
            return wars.get(war.getOpponent().getTown().getName().toLowerCase());
        }
        return null;
    }

    @Nullable
    public TownWar getTownWar(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        ConfigValues config = plugin.getConfigValues();
        Resident resident = TownyUniverse.getInstance().getResident(uuid);

        if (resident == null) {
            player.sendMessage(config.getResidentErrorMessage());
            return null;
        }

        Town town;
        try {
            town = resident.getTown();
        } catch (NotRegisteredException e) {
            player.sendMessage(config.getResidentErrorMessage());
            return null;
        }

        return this.getTownWar(town.getName().toLowerCase());
    }

    public void removeWar(War war) {
        wars.remove(war.getAttacker().getTown().getName().toLowerCase());
    }

    @SneakyThrows
    public boolean declare(
            Player whoDeclare,
            TownWar attacker,
            TownWar defender
    ) {
        ConfigValues config = plugin.getConfigValues();

        if (getWar(attacker) != null) {
            whoDeclare.sendMessage(config.getTownInWar());
            return false;
        }

        if (getWar(defender) != null) {
            whoDeclare.sendMessage(config.getAlreadyInWarTarget());
            return false;
        }

        if (attacker.getRespawnLocation() == null) {
            whoDeclare.sendMessage(config.getYouRespawnChunkNull());
            return false;
        }

        if (defender.getRespawnLocation() == null) {
            whoDeclare.sendMessage(config.getTargetRespawnChunkNull());
            return false;
        }

        attacker.addParticipants(TownyUtils.getResidentsOnline(attacker.getTown()));
        if (attacker.getTown().getAccount().getHoldingBalance() < calculateMoneyReward(attacker)) {
            whoDeclare.sendMessage(config.getNotEnoughMoney()
                    .replace("%cost%", calculateMoneyReward(attacker)+""));
            attacker.clearParticipants();
            return false;
        }

        attacker.getTown().getAccount().withdraw(calculateMoneyReward(attacker), "War - "+ defender.getTown().getName());

        defender.addParticipants(TownyUtils.getResidentsOnline(defender.getTown()));

        War war = new War(this, attacker, defender);
        wars.put(attacker.getTown().getName().toLowerCase(), war);

        war.getAttacker().initWar(defender);
        war.getDefender().initWar(attacker);
        return true;
    }

    private int calculateMoneyReward(TownWar attacker) {
        return plugin.getConfigValues().getCostDeclareWarCoeff() *
                attacker.getParticipants().size();
    }
}
