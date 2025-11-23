package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReportsList extends BaseGui {
    private final Report.Status type;
    private final Set<Report> reports;

    public ReportsList(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull Set<Report> reports, @NotNull Report.Status type) {
        super(plugin, player);
        this.reports = reports;
        this.type = type;

        open();
    }

    @Override
    protected void open() {
        final OnlinePlayer onlinePlayer = plugin.getUsersManager().getUsermap().get(player.getUniqueId());
        
        if (onlinePlayer == null) {
            plugin.log(Level.WARNING, "Player with UUID " + player.getUniqueId() + " not found!");
            
            return;
        }

        String title;
        if (player.hasPermission("ultimatereports.reports.manage")) {
            title = plugin.getGuiConfig().getReportsList().getTitleAllReports();
        } else title = plugin.getGuiConfig().getReportsList().getTitleYourReports();


        final InventoryGui gui = new InventoryGui(plugin, player, serialize(title), plugin.getGuiConfig().getReportsList().getSetup());
        addCommonElements(gui, onlinePlayer);


        // Reports
        GuiElementGroup group = new GuiElementGroup('c');
        reports.forEach(report -> {
            OfflinePlayer reporter = Bukkit.getOfflinePlayer(report.getReporter());

            group.addElement((new StaticGuiElement('o',
                    createPlayerSkull(reporter),
                    click -> {
                        new ReportGui(plugin, player, report.getId());

                        return true;
                    },
                    getReportInfo(report)
            )));
        });
        gui.addElement(group);


        // Report Type
        GuiStateElement reportsType = new GuiStateElement('b',
                new GuiStateElement.State(
                        change -> new ReportsList(plugin, player, plugin.getReportsManager().getWaitingReports(), Report.Status.WAITING),
                        "waiting",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%IN_PROGRESS%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList())
                        )
                ),
                new GuiStateElement.State(
                        change -> new ReportsList(plugin, player, plugin.getReportsManager().getInProgressReports(), Report.Status.IN_PROGRESS),
                        "in_progress",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%DONE%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList())
                        )
                ),
                new GuiStateElement.State(
                        change -> new ReportsList(plugin, player, plugin.getReportsManager().getClosedReports(), Report.Status.DONE),
                        "done",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%ARCHIVED%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList())
                        )
                ),
                new GuiStateElement.State(
                        change -> new ReportsList(plugin, player, plugin.getReportsManager().getArchivedReports(), Report.Status.ARCHIVED),
                        "archived",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%ALL%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList())
                        )
                ),
                new GuiStateElement.State(
                        change -> new ReportsList(plugin, player, plugin.getReportsManager().getAllReports(), Report.Status.OPEN),
                        "all",
                        new ItemStack(plugin.getGuiConfig().getReportsList().getFiltersItem()),
                        serialize(plugin.getGuiConfig().getReportsList().getFilters().stream()
                                .map(s -> s.replace("%FILTER%", "%WAITING%"))
                                .map(this::replaceStatus)
                                .collect(Collectors.toList())
                        )
                )
        );

        if (type.equals(Report.Status.OPEN)) {
            reportsType.setState("all");

        } else if (type.equals(Report.Status.WAITING)) {

            reportsType.setState("waiting");
        } else if (type.equals(Report.Status.IN_PROGRESS)) {

            reportsType.setState("in_progress");
        } else if (type.equals(Report.Status.DONE)) {

            reportsType.setState("done");
        } else if (type.equals(Report.Status.ARCHIVED)) {

            reportsType.setState("archived");
        }

        if (player.hasPermission("ultimatereports.reports.manage")) {
            gui.addElement(reportsType);
        }

        gui.show(player);
    }
}
