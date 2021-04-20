package io.github.niketion.townywar.structure;

import com.palmergames.bukkit.towny.object.Town;
import lombok.Getter;

@Getter
public class TownGrace {
    private final Town town;
    private final long startGrace;

    public TownGrace(Town town) {
        this.town = town;
        this.startGrace = System.currentTimeMillis();
    }
}
