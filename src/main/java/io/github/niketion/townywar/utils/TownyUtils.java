package io.github.niketion.townywar.utils;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class TownyUtils {

    public static List<Resident> getResidentsOnline(Town town) {
        return town.getResidents()
                .stream()
                .filter(
                        resident -> Bukkit.getPlayer(resident.getUUID()) != null
                )
                .collect(Collectors.toList());
    }

    @Nullable
    public static Town getTownFromPlayer(Player player) {
        Town townPlayer = null;
        try {
            Resident resident = TownyUniverse.getInstance().getResident(player.getName());
            if (resident == null) return null;

            townPlayer = resident.getTown();
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
        return townPlayer;
    }
}
