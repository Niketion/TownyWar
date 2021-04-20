package io.github.niketion.townywar.structure;

import io.github.niketion.townywar.structure.handlers.ParticipantHandler;
import io.github.niketion.townywar.utils.ConfigValues;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

@Getter
public class Participant {
    private final ParticipantHandler handler;
    private final UUID uuid;
    private final TownWar town;
    @Setter private Long invincibilityTime;

    public Participant(ParticipantHandler handler, UUID uuid, TownWar town) {
        this.handler = handler;
        this.uuid = uuid;
        this.town = town;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void teleport() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        player.teleport(this.getTeleportLocation());
    }

    public Location getTeleportLocation() {
        Chunk chunk = town.getRespawnLocation().getChunk();
        Location center = new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4)
                .add(7, 0, 7);

        Location toTeleport = center.clone().add(
                new Random().nextInt(14)-7D,
                center.getY(),
                new Random().nextInt(14)-7D);
        toTeleport.setY(toTeleport.getWorld().getHighestBlockYAt(toTeleport.getBlockX(), toTeleport.getBlockZ())+2D);

        return toTeleport;
    }

    public void giveEffects() {
        Player player = getPlayer();
        if (player == null) return;

        ConfigValues values = handler.getPlugin().getConfigValues();

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, values.getSecondsInvisibilityRespawn()*20, 0));
        handler.addInvincibility(this);
    }
}
