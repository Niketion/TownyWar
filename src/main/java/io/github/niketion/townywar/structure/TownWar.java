package io.github.niketion.townywar.structure;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.compress.utils.Lists;
import com.samjakob.spigui.SGMenu;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.structure.handlers.WarHandler;
import io.github.niketion.townywar.utils.ConfigValues;
import io.github.niketion.townywar.utils.Configuration;
import io.github.niketion.townywar.utils.LocationUtils;
import io.github.niketion.townywar.utils.TownyUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class TownWar {
    private final WarHandler handler;
    private final Town town;
    private final Map<UUID, Participant> participants;
    private final Set<Town> allies;

    @Setter private TownWar opponent;
    private Location respawnLocation;
    @Setter private int lives;
    @Setter private War pendingInvites;

    public TownWar(
            WarHandler handler,
            Town town,
            Location respawnLocation
    ) {
        this.handler = handler;
        this.town = town;
        this.respawnLocation = respawnLocation;
        this.participants = Maps.newHashMap();
        this.allies = Sets.newHashSet();
    }

    public void setRespawnLocation(Location location) {
        this.respawnLocation = location;

        Configuration confLoc = handler.getPlugin().getConfigLocations();
        confLoc.getConfig().set(town.getName().toLowerCase(), LocationUtils.getLiteStringFromLocation(location));
        confLoc.saveConfig();
    }

    public void clearParticipants() {
        participants.forEach((
                (uuid, participant) -> handler.getPlugin().getParticipantHandler().removeParticipant(uuid)
        ));
        participants.clear();
    }

    public void registerKill() {
        this.lives--;
    }

    public void clearWar() {
        clearParticipants();
        this.lives = 0;
        this.opponent = null;
        this.allies.clear();
        this.pendingInvites = null;
    }

    public void registerAlly(War war) {
        Bukkit.getScheduler().runTaskAsynchronously(handler.getPlugin(), () -> {
            Nation nation = null;
            try {
                nation = town.getNation();
            } catch (NotRegisteredException ignored) { }

            if (nation != null) {
                for (Nation ally : nation.getAllies()) {
                    for (Town townAlly : ally.getTowns()) {
                        if (town == townAlly) continue;
                        allies.add(townAlly);
                    }
                }

                for (Town nationTown : nation.getTowns()) {
                    if (nationTown == town) continue;
                    allies.add(nationTown);
                }
            }

            if (handler.getPlugin().getConfigValues().isAutomaticInviteAllyStartWar())  {
                for (Town ally : allies) {
                    inviteAlly(ally, war);
                }
            }
        });
    }

    public boolean isAlly(TownWar townWar) {
        for (Town ally : allies) {
            if (townWar.getTown().getName().equalsIgnoreCase(ally.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean inviteAlly(Town town, War war) {
        TownWar townWar = handler.getTownWar(town.getName());
        if (townWar == null || !isAlly(townWar)) {
            return false;
        }

        for (Resident resident : TownyUtils.getResidentsOnline(town)) {
            Player player = resident.getPlayer();
            if (player == null) continue;

            if (!player.hasPermission("war.acceptallyinvite")) continue;

            player.sendMessage(handler.getPlugin().getConfigValues().getInviteAlly()
                    .replace("%name%", town.getName())
                    .replace("%target%", opponent.getTown().getName())
            );
        }

        townWar.setPendingInvites(war);
        return true;
    }

    public boolean acceptInvite(TownWar townWar) {
        if (pendingInvites == null) {
            return false;
        }

        this.pendingInvites = null;
        for (Resident resident : TownyUtils.getResidentsOnline(town)) {
            townWar.addParticipant(resident.getUUID()).teleport();
        }
        allies.remove(town);
        return true;
    }

    public Participant addParticipant(UUID uuid) {
        Participant participant = handler.getPlugin()
                .getParticipantHandler().addParticipant(uuid, this);
        this.participants.put(uuid, participant);
        return participant;
    }

    public void addParticipants(Collection<Resident> residents) {
        for (Resident resident : residents) {
            this.addParticipant(resident.getUUID());
        }
    }

    public void removeParticipant(UUID uuid) {
        this.participants.remove(uuid);
        handler.getPlugin().getParticipantHandler().removeParticipant(uuid);
    }

    public void selection(Player player) {
        ConfigValues config = handler.getPlugin().getConfigValues();
        SGMenu menu = handler.getPlugin().getSpiGUI().create(config.getGuiTitleWar(), 2);

        player.sendMessage(config.getSearchingDeclareWar());
        findOpponents(request -> {
            if (request.isEmpty()) {
                player.sendMessage(config.getSearchNotFound());
                return;
            }

            for (TownWar war : request) {
                List<String> lore = Lists.newArrayList();
                for (String s : config.getLoreItemTown()) {
                    lore.add(s.replace(
                            "%online%", TownyUtils.getResidentsOnline(war.getTown()).size()+"")
                    );
                }

                SGButton button = new SGButton(
                        new ItemBuilder(config.getMaterialItemTown())
                                .name(config.getDisplayItemNameItemTown()
                                        .replace("%name%", war.getTown().getName()))
                                .lore(lore)
                                .build()
                ).withListener((InventoryClickEvent mainEvent) -> {
                    player.closeInventory();
                    SGMenu confirmMenu = handler.getPlugin().getSpiGUI().create(config.getGuiConfirmWar(), 3);
                    confirmMenu.setAutomaticPaginationEnabled(false);

                    for (int i=0; i<29; i++){
                        if (i==11 || i==15) continue;
                        confirmMenu.addButtons(
                                new SGButton(new ItemBuilder(config.getFillGuiConfirmItem())
                                .name(ChatColor.GRAY+"").build())
                        );
                    }

                    List<String> confirmLore = Lists.newArrayList();
                    for (String s : config.getConfirmItemLore()) {
                        confirmLore.add(s.replace("%name%", war.getTown().getName()));
                    }

                    confirmMenu.setButton(11, new SGButton(
                            new ItemBuilder(config.getConfirmItem())
                                    .name(config.getConfirmItemDisplayname())
                                    .lore(confirmLore)
                                    .build()
                    ).withListener((InventoryClickEvent event) -> {
                        handler.declare(player, this, war);
                        event.getWhoClicked().closeInventory();
                    }));

                    confirmMenu.setButton(15, new SGButton(
                            new ItemBuilder(config.getDenyItem())
                                    .name(config.getDenyItemDisplayname())
                                    .lore(config.getDenyItemLore())
                                    .build()
                    ).withListener((InventoryClickEvent event) -> {
                        event.getWhoClicked().closeInventory();
                        Bukkit.getScheduler().runTaskAsynchronously(handler.getPlugin(), () -> selection(player));
                    }));

                    Bukkit.getScheduler().runTask(handler.getPlugin(), () -> player.openInventory(confirmMenu.getInventory()));
                });

                menu.addButton(button);
            }

            Bukkit.getScheduler().runTask(handler.getPlugin(), () -> player.openInventory(menu.getInventory()));
        });
    }

    public void initWar(TownWar opponent) {
        this.opponent = opponent;

        sendAllMessage(handler.getPlugin().getConfigValues().getWarStarted()
                .replace("%name%", opponent.getTown().getName())
                .replace("%seconds%", handler.getPlugin().getConfigValues().getSecondsBeforeWarStarts()+"")
        );

        for (Participant participant : participants.values()) {
            Player player = participant.getPlayer();
            if (player == null) {
                removeParticipant(participant.getUuid());
                continue;
            }

            participant.teleport();
        }
    }

    public void sendAllMessage(String message) {
        for (Participant participant : participants.values()) {
            Player player = participant.getPlayer();
            if (player == null) continue;

            player.sendMessage(message);
        }
    }

    public void findOpponents(Consumer<Collection<TownWar>> request) {
        TownyWarPlugin plugin = handler.getPlugin();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<TownWar> list = Lists.newArrayList();

            plugin.getWarHandler().getTownWars()
                .values().stream()
                .filter(
                        townWar -> !handler.hasGrace(townWar.getTown()) && townWar.getTown().getNumResidents() != 0
                                && townWar != this && !townWar.getTown().isNeutral()
                ).forEach(
                        townWar -> {
                            int residentOpponent = TownyUtils.getResidentsOnline(townWar.getTown()).size();
                            if (residentOpponent == 0) return;

                            int resident = TownyUtils.getResidentsOnline(town).size();

                            if (residentOpponent+(residentOpponent*plugin.getConfigValues()
                                    .getMorePlayerForWarCoeff()/100) >= resident) {
                                list.add(townWar);
                            }
                        });

            request.accept(list);
        });
    }
}
