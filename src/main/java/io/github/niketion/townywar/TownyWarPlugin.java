package io.github.niketion.townywar;

import com.samjakob.spigui.SpiGUI;
import io.github.niketion.townywar.commands.WarAdminCommand;
import io.github.niketion.townywar.commands.WarCommand;
import io.github.niketion.townywar.listeners.BlockListeners;
import io.github.niketion.townywar.listeners.PlayerListeners;
import io.github.niketion.townywar.listeners.TownyListeners;
import io.github.niketion.townywar.structure.handlers.GUIHandler;
import io.github.niketion.townywar.structure.handlers.ParticipantHandler;
import io.github.niketion.townywar.tasks.InvincibilityTask;
import io.github.niketion.townywar.tasks.WarTask;
import io.github.niketion.townywar.utils.ConfigValues;
import io.github.niketion.townywar.structure.handlers.WarHandler;
import io.github.niketion.townywar.utils.Configuration;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class TownyWarPlugin extends JavaPlugin {

    private WarHandler warHandler;
    private ParticipantHandler participantHandler;
    private GUIHandler guiHandler;

    private ConfigValues configValues;
    private Configuration configLocations;

    private SpiGUI spiGUI;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.configLocations = new Configuration(this, "locations.yml");
        this.configLocations.saveDefaultConfig();
        this.configValues = new ConfigValues(this);

        this.warHandler = new WarHandler(this);
        this.warHandler.init();
        this.participantHandler = new ParticipantHandler(this);

        this.spiGUI = new SpiGUI(this);
        this.guiHandler = new GUIHandler(this);

        new WarCommand(this);
        new WarAdminCommand(this);

        new TownyListeners(this);
        new BlockListeners(this);
        new PlayerListeners(this);

        new WarTask(this).runTaskTimerAsynchronously(this, 20*5, 20);
        new InvincibilityTask(this).runTaskTimerAsynchronously(this, 20*5, 20);
    }

    public void reloadConfigValues() {
        this.reloadConfig();
        this.configValues = new ConfigValues(this);
        this.guiHandler = new GUIHandler(this);
    }
}
