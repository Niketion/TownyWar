package io.github.niketion.townywar.structure.handlers;

import com.palmergames.compress.utils.Lists;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import com.samjakob.spigui.pagination.SGPaginationButtonBuilder;
import io.github.niketion.townywar.TownyWarPlugin;
import io.github.niketion.townywar.utils.ConfigValues;
import org.bukkit.Material;

import java.util.List;

public class GUIHandler {
    private final TownyWarPlugin plugin;
    private ConfigValues config;

    public GUIHandler(TownyWarPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigValues();

        this.plugin.getSpiGUI().setEnableAutomaticPagination(true);

        SGPaginationButtonBuilder defaultPaginationButtonBuilder = (type, inventory) -> {
            switch (type) {
                case PREV_BUTTON:
                    if (inventory.getCurrentPage() > 0) return new SGButton(new ItemBuilder(config.getPreviousPageMaterial())
                            .name(config.getPreviousPageDisplayname())
                            .build()
                    ).withListener(event -> {
                        event.setCancelled(true);
                        inventory.previousPage(event.getWhoClicked());
                    });
                    else return null;

                case CURRENT_BUTTON:
                    List<String> currentPageLore = Lists.newArrayList();
                    for (String s : config.getCurrentPageLore()) {
                        currentPageLore.add(s.replace("%page%", inventory.getCurrentPage() + ""));
                    }

                    return new SGButton(new ItemBuilder(Material.NAME_TAG)
                            .name(config.getCurrentPageDisplayName()
                                    .replace("%page%", (inventory.getCurrentPage() + 1) + "")
                                    .replace("%maxpage%", inventory.getMaxPage() + "")
                            )
                            .lore(
                                    currentPageLore
                            ).build()
                    ).withListener(event -> event.setCancelled(true));

                case NEXT_BUTTON:
                    if (inventory.getCurrentPage() < inventory.getMaxPage() - 1)
                        return new SGButton(new ItemBuilder(config.getNextPageMaterial())
                                .name(config.getNextPageDisplayname())
                                .build()
                        ).withListener(event -> {
                            event.setCancelled(true);
                            inventory.nextPage(event.getWhoClicked());
                        });
                    else return null;

                case UNASSIGNED:
                default:
                    return null;
            }
        };
        this.plugin.getSpiGUI().setDefaultPaginationButtonBuilder(defaultPaginationButtonBuilder);
    }
}
