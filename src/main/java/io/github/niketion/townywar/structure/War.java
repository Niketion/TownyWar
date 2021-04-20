package io.github.niketion.townywar.structure;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.compress.utils.Lists;
import io.github.niketion.townywar.structure.handlers.ParticipantHandler;
import io.github.niketion.townywar.structure.handlers.WarHandler;
import io.github.niketion.townywar.utils.ConfigValues;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import sun.security.krb5.Config;

import java.util.*;

@Getter
public class War {
    private final WarHandler handler;
    private final TownWar attacker, defender;

    private final Map<Location, Material> toReplace;
    private final Map<Location, Block> toRemove;

    private final long startTime;
    private final int moneyReward;
    @Setter private State state;

    public War(
            WarHandler warHandler,
            TownWar attacker,
            TownWar defender
    ) {
        this.handler = warHandler;

        this.attacker = attacker;
        this.attacker.registerAlly(this);

        this.defender = defender;
        this.defender.registerAlly(this);

        this.startTime = System.currentTimeMillis();

        this.moneyReward = warHandler.getPlugin().getConfigValues().getCostDeclareWarCoeff() *
                attacker.getParticipants().size();
        this.state = State.PREPARATION;

        this.toReplace = Maps.newHashMap();
        this.toRemove = Maps.newHashMap();
    }

    public void sendActionBar() {
        ConfigValues config = handler.getPlugin().getConfigValues();
        if (getState() == State.PREPARATION) {
            for (Participant participant : getAllParticipants()) {
                long seconds = ((config.getSecondsBeforeWarStarts()*1000L)-(System.currentTimeMillis() - startTime))/1000;

                if (seconds <= 0) {
                    return;
                }

                Player player = participant.getPlayer();
                if (player == null) continue;

                player.sendActionBar(
                        handler.getPlugin().getConfigValues()
                                .getGameCooldownActionBar()
                                .replace("%seconds%", seconds+"")
                );
            }
        } else {
            for (Participant participant : getAllParticipants()) {
                Player player = participant.getPlayer();
                if (player == null) continue;

                player.sendActionBar(
                        handler.getPlugin().getConfigValues()
                                .getGameActionBar()
                                .replace("%attacker_lives%", attacker.getLives()+"")
                                .replace("%defender_lives%", defender.getLives()+"")
                );
            }
        }
    }

    public void logPlace(Block block) {
        if (toReplace.containsKey(block.getLocation())) return;
        if (toRemove.containsKey(block.getLocation())) return;
        toRemove.put(block.getLocation(), block);
    }

    public void logBreak(Block block) {
        if (toRemove.containsKey(block.getLocation())) return;
        if (toReplace.containsKey(block.getLocation())) return;
        toReplace.put(block.getLocation(), block.getType());
    }

    public void win(TownWar winner) {
        ConfigValues config = handler.getPlugin().getConfigValues();
        sendAllMessage(config.getTownWinWarLife()
                .replace("%name%", winner.getTown().getName()));

        TownWar looser = winner.getOpponent();
        Town looserTown = looser.getTown();

        int numberClaims = looserTown.getPurchasedBlocks()+looserTown.getBonusBlocks();
        float percentage =(numberClaims/100F)*config.getPercentageWinTownblocks();
        int numberTownBlocks = (int) percentage;
        if (numberTownBlocks == 0) numberTownBlocks++;

        winner.sendAllMessage(config.getWinTownblocks()
                .replace("%number%", numberTownBlocks+"")
        );

        looser.sendAllMessage(config.getLostTownblocks()
                .replace("%number%", numberTownBlocks+"")
        );

        if (looserTown.getPurchasedBlocks()>=numberTownBlocks) {
            looserTown.setPurchasedBlocks(looserTown.getPurchasedBlocks()-numberTownBlocks);
        } else if (looserTown.getBonusBlocks()>=numberTownBlocks) {
            looserTown.setBonusBlocks(looserTown.getBonusBlocks()-numberTownBlocks);
        }

        winner.getTown().setBonusBlocks(winner.getTown().getBonusBlocks()+numberTownBlocks);

        try {
            winner.getTown().getAccount().deposit(moneyReward, "Win war");
        } catch (EconomyException e) {
            e.printStackTrace();
        }

        handler.addGrace(winner.getOpponent().getTown());

        this.end();
    }

    public void softWin(TownWar winner) {
        if (winner == null) {
            sendAllMessage(handler.getPlugin().getConfigValues().getNoTownWon());

            try {
                attacker.getTown().getAccount().deposit(moneyReward, "Win war");
            } catch (EconomyException e) {
                e.printStackTrace();
            }
        } else {
            sendAllMessage(handler.getPlugin().getConfigValues().getTownWinWarTime()
                    .replace("%name%", winner.getTown().getName()));

            try {
                winner.getTown().getAccount().deposit(moneyReward, "Win war");
            } catch (EconomyException e) {
                e.printStackTrace();
            }

            handler.addGrace(winner.getOpponent().getTown());
        }

        this.end();
    }

    @Nullable
    public TownWar getPossibilityWinner() {
        if (attacker.getLives() > defender.getLives())
            return attacker;
        if (attacker.getLives() < defender.getLives())
            return defender;
        return null;
    }

    public void end() {
        toRemove.forEach((location, block) -> {
            location.getWorld().getBlockAt(location).setType(Material.AIR);
        } );
        toReplace.forEach((location, block) -> {
            location.getWorld().getBlockAt(location).setType(block);
        });

        Bukkit.getScheduler().runTaskAsynchronously(handler.getPlugin(), () -> {
            ParticipantHandler participantHandler = handler.getPlugin().getParticipantHandler();
            participantHandler.clear(attacker);
            participantHandler.clear(defender);

            attacker.clearWar();

            defender.clearWar();
        });

        handler.removeWar(this);
    }

    private Collection<Participant> getAllParticipants() {
        HashSet<Participant> residents = Sets.newHashSet();
        residents.addAll(attacker.getParticipants().values());
        residents.addAll(defender.getParticipants().values());
        return residents;
    }

    public void sendAllMessage(String message) {
        for (Participant participant : getAllParticipants()) {
            Player player = participant.getPlayer();
            if (player == null) continue;
            player.sendMessage(message);
        }
    }

    public void startGame() {
        ConfigValues config = handler.getPlugin().getConfigValues();

        int lives = (defender.getParticipants().size()+attacker.getParticipants().size())*config.getCoeffMultiplyLives();
        attacker.setLives(lives);
        defender.setLives(lives);

        this.state = State.GAME;
        attacker.sendAllMessage(config.getGameStart());
        defender.sendAllMessage(config.getGameStart());
    }

    public enum State {
        PREPARATION,
        GAME
    }


}
