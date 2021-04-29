package io.github.niketion.townywar.utils;

import com.google.common.collect.Sets;
import io.github.niketion.townywar.TownyWarPlugin;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

@Getter
public class ConfigValues {
    private final int costDeclareWarCoeff;
    private final int morePlayerForWarCoeff;
    private final String residentErrorMessage;
    private final String[] usageWarCommand;
    private final String respawnChunkCommand;
    private final String youRespawnChunkNull;
    private final String targetRespawnChunkNull;
    private final String guiTitleWar;
    private final Material materialItemTown;
    private final String displayItemNameItemTown;
    private final String[] loreItemTown;
    private final String searchingDeclareWar;
    private final String searchNotFound;
    private final String currentPageDisplayName;
    private final String[] currentPageLore;
    private final Material currentPageMaterial;
    private final String previousPageDisplayname;
    private final String nextPageDisplayname;
    private final Material previousPageMaterial;
    private final Material nextPageMaterial;
    private final String notEnoughMoney;
    private final int secondsBeforeWarStarts;
    private final String warStarted;
    private final String gameStart;
    private final String gameCooldownActionBar;
    private final String gameActionBar;
    private final int coeffMultiplyLives;
    private final String alreadyInWarTarget;
    private final String townInWar;
    private final Set<Material> whitelistBlockPlaceWar;
    private final Set<Material> whitelistBlockBreakWar;
    private final int secondsInvisibilityRespawn;
    private final int secondsInvincibilityRespawn;
    private final String townWinWarTime;
    private final int timeMinuteDurationWar;
    private final String noTownWon;
    private final String townWinWarLife;
    private final int percentageWinTownblocks;
    private final int gracePeriodLosersHour;
    private final String guiConfirmWar;
    private final Material fillGuiConfirmItem;
    private final Material confirmItem;
    private final Material denyItem;
    private final String confirmItemDisplayname;
    private final String[] confirmItemLore;
    private final String denyItemDisplayname;
    private final String[] denyItemLore;
    private final String townNotFound;
    private final String townNotInWar;
    private final String townNotAlly;
    private final String inviteAlly;
    private final String noInviteToAccept;
    private final String actionPreWar;
    private final String alreadyInWar;
    private final String noPermission;
    private final boolean automaticInviteAllyStartWar;
    private final String winTownblocks;
    private final String lostTownblocks;
    private final String blacklistMessage;
    private final String blacklistMessagePrewar;
    private final List<String> blacklistCommandPrewar;
    private final List<String> blacklistCommandWar;
    private final String cantDeclareWar;
    private final String adminEnd;
    private final String adminWin;
    private final String adminStopAll;

    public ConfigValues(TownyWarPlugin plugin) {
        FileConfiguration configuration = plugin.getConfig();

        this.adminStopAll = StringUtils.c(configuration.getString("admin-stop-all"));
        this.adminWin = StringUtils.c(configuration.getString("admin-win"));
        this.adminEnd = StringUtils.c(configuration.getString("admin-end"));
        this.winTownblocks = StringUtils.c(configuration.getString("win-townblocks"));
        this.lostTownblocks = StringUtils.c(configuration.getString("lost-townblocks"));
        this.automaticInviteAllyStartWar = configuration.getBoolean("automatic-invite-ally-start-war");
        this.noPermission = StringUtils.c(configuration.getString("no-permission"));
        this.costDeclareWarCoeff = configuration.getInt("cost-declare-war-coeff");
        this.morePlayerForWarCoeff = configuration.getInt("more-player-for-war-coeff");
        this.residentErrorMessage = StringUtils.c(configuration.getString("resident-error-message"));
        this.usageWarCommand = StringUtils.c(configuration.getStringList("usage-war-command"));
        this.respawnChunkCommand = StringUtils.c(configuration.getString("respawn-chunk-command"));
        this.youRespawnChunkNull = StringUtils.c(configuration.getString("you-respawn-chunk-null"));
        this.targetRespawnChunkNull = StringUtils.c(configuration.getString("target-respawn-chunk-null"));
        this.guiTitleWar = StringUtils.c(configuration.getString("gui-title-war"));
        this.materialItemTown = Material.valueOf(configuration.getString("material-item-town"));
        this.displayItemNameItemTown = StringUtils.c(configuration.getString("displayname-item-town"));
        this.loreItemTown = StringUtils.c(configuration.getStringList("lore-item-town"));
        this.searchingDeclareWar = StringUtils.c(configuration.getString("searching-declare-war"));
        this.searchNotFound = StringUtils.c(configuration.getString("search-not-found"));
        this.currentPageDisplayName = StringUtils.c(configuration.getString("current-page-displayname"));
        this.currentPageLore = StringUtils.c(configuration.getStringList("current-page-lore"));
        this.currentPageMaterial = Material.valueOf(configuration.getString("current-page-material"));
        this.previousPageDisplayname = StringUtils.c(configuration.getString("previous-page-displayname"));
        this.nextPageDisplayname = StringUtils.c(configuration.getString("next-page-displayname"));
        this.previousPageMaterial = Material.valueOf(configuration.getString("previous-page-material"));
        this.nextPageMaterial = Material.valueOf(configuration.getString("next-page-material"));
        this.notEnoughMoney = StringUtils.c(configuration.getString("not-enough-money"));
        this.secondsBeforeWarStarts = configuration.getInt("seconds-before-war-starts");
        this.warStarted = StringUtils.c(configuration.getString("war-started"));
        this.gameStart = StringUtils.c(configuration.getString("game-start"));
        this.gameCooldownActionBar = StringUtils.c(configuration.getString("game-cooldown-actionbar"));
        this.gameActionBar = StringUtils.c(configuration.getString("game-actionbar"));
        this.coeffMultiplyLives = configuration.getInt("coeff-multiply-lives");
        this.alreadyInWarTarget = StringUtils.c(configuration.getString("already-in-war-target"));
        this.townInWar = StringUtils.c(configuration.getString("town-in-war"));
        this.whitelistBlockPlaceWar = loadMaterials(configuration.getStringList("whitelist-block-place-war"));
        this.whitelistBlockBreakWar = loadMaterials(configuration.getStringList("whitelist-block-break-war"));
        this.secondsInvincibilityRespawn = configuration.getInt("seconds-invicibility-respawn");
        this.secondsInvisibilityRespawn = configuration.getInt("seconds-invisibility-respawn");
        this.townWinWarTime = StringUtils.c(configuration.getString("town-win-war-time"));
        this.timeMinuteDurationWar = configuration.getInt("time-minute-duration-war");
        this.noTownWon = StringUtils.c(configuration.getString("no-town-won"));
        this.townWinWarLife = StringUtils.c(configuration.getString("town-win-war-life"));
        this.percentageWinTownblocks = configuration.getInt("percentage-win-townblocks");
        this.gracePeriodLosersHour = configuration.getInt("grace-period-losers-hour");
        this.guiConfirmWar = StringUtils.c(configuration.getString("gui-confirm-war"));
        this.fillGuiConfirmItem = Material.valueOf(configuration.getString("fill-gui-confirm-item"));
        this.confirmItem = Material.valueOf(configuration.getString("confirm-item"));
        this.denyItem = Material.valueOf(configuration.getString("deny-item"));
        this.confirmItemDisplayname = StringUtils.c(configuration.getString("confirm-item-displayname"));
        this.denyItemDisplayname = StringUtils.c(configuration.getString("deny-item-displayname"));
        this.confirmItemLore = StringUtils.c(configuration.getStringList("confirm-item-lore"));
        this.denyItemLore = StringUtils.c(configuration.getStringList("deny-item-lore"));
        this.townNotFound = StringUtils.c(configuration.getString("town-not-found"));
        this.townNotInWar = StringUtils.c(configuration.getString("town-not-in-war"));
        this.townNotAlly = StringUtils.c(configuration.getString("town-not-ally"));
        this.inviteAlly = StringUtils.c(configuration.getString("invite-ally"));
        this.noInviteToAccept = StringUtils.c(configuration.getString("no-invite-to-accept"));
        this.actionPreWar = StringUtils.c(configuration.getString("action-prewar"));
        this.alreadyInWar = StringUtils.c(configuration.getString("already-in-war"));
        this.blacklistMessage = StringUtils.c(configuration.getString("blacklist-message"));
        this.blacklistMessagePrewar = StringUtils.c(configuration.getString("blacklist-message-prewar"));
        this.blacklistCommandPrewar = configuration.getStringList("blacklist-command-prewar");
        this.blacklistCommandWar = configuration.getStringList("blacklist-command-war");
        this.cantDeclareWar = StringUtils.c(configuration.getString("cant-declare-war"));
    }

    private Set<Material> loadMaterials(List<String> list) {
        Set<Material> materials = Sets.newHashSet();
        for (String s : list) {
            materials.add(Material.valueOf(s));
        }
        return materials;
    }
}
