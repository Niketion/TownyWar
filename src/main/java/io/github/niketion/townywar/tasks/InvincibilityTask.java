package io.github.niketion.townywar.tasks;

import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.Participant;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class InvincibilityTask extends BukkitRunnable {
    private final TownyWarPlugin plugin;

    @Override
    public void run() {
        if (plugin.getParticipantHandler().getInvincibility().isEmpty()) return;

        for (Participant participant : plugin.getParticipantHandler().getInvincibility().values()) {
            if (System.currentTimeMillis() - participant.getInvincibilityTime() >=
                    plugin.getConfigValues().getSecondsInvincibilityRespawn()*1000L) {
                plugin.getParticipantHandler().removeInvincibility(participant);
            }
        }
    }
}
