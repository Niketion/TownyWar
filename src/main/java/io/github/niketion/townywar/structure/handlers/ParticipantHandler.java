package io.github.niketion.townywar.structure.handlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.Participant;
import io.github.niketion.townywar.structure.TownWar;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class ParticipantHandler {
    private final TownyWarPlugin plugin;
    private final Map<UUID, Participant> participants;
    private final Map<UUID, Participant> invincibility;

    public ParticipantHandler(TownyWarPlugin plugin) {
        this.plugin = plugin;
        this.participants = Maps.newHashMap();
        this.invincibility = Maps.newConcurrentMap();
    }

    @Nullable
    public Participant getParticipant(UUID uuid) {
        return this.participants.get(uuid);
    }

    public Participant addParticipant(UUID uuid, TownWar townWar) {
        Participant participant = new Participant(this, uuid, townWar);
        this.participants.put(uuid, participant);
        return participant;
    }

    public void removeParticipant(UUID uuid) {
        this.participants.remove(uuid);
    }

    public boolean hasInvincibility(UUID uuid) {
        return invincibility.containsKey(uuid);
    }

    public void clear(TownWar war) {
        List<Participant> toRemove = Lists.newArrayList();
        for (Participant participant : participants.values()) {
            if (participant.getTown().getTown().getName().equals(war.getTown().getName())) {
                toRemove.add(participant);
            }
        }

        for (Participant participant : toRemove) participants.remove(participant.getUuid());

        toRemove.clear();

        for (Participant participant : invincibility.values()) {
            if (participant.getTown().getTown().getName().equals(war.getTown().getName())) {
                toRemove.add(participant);
            }
        }

        for (Participant participant : toRemove) participants.remove(participant.getUuid());
    }

    public void addInvincibility(Participant participant) {
        participant.setInvincibilityTime(System.currentTimeMillis());
        invincibility.put(participant.getUuid(), participant);
    }

    public void removeInvincibility(Participant participant) {
        participant.setInvincibilityTime(null);
        invincibility.remove(participant.getUuid());
    }
}
