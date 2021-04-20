package io.github.niketion.townywar.tasks;

import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.TownWar;
import io.github.niketion.townywar.structure.War;
import io.github.niketion.townywar.utils.ConfigValues;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class WarTask extends BukkitRunnable {
    private final TownyWarPlugin plugin;

    @Override
    public void run() {
        if (plugin.getWarHandler().getWars().isEmpty()) return;

        ConfigValues config = plugin.getConfigValues();

        plugin.getWarHandler().getWars().forEach((string, war) -> {
            war.sendActionBar();
            if (war.getState() != War.State.GAME && System.currentTimeMillis() - war.getStartTime() >=
                    config.getSecondsBeforeWarStarts() * 1000L) {
                war.startGame();
                return;
            }

            if (System.currentTimeMillis() - war.getStartTime() >= config.getTimeMinuteDurationWar()*1000L*60) {
                war.softWin(war.getPossibilityWinner());
                return;
            }
        });
    }
}
