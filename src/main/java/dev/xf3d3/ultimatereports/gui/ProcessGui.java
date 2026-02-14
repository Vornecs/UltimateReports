package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ProcessGui extends BaseGui {
    private final Report report;
    private OnlinePlayer reporter;
    private final Report.Status previousFilter;

    public ProcessGui(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull Report report, @NotNull OnlinePlayer reporter) {
        this(plugin, player, report, reporter, Report.Status.OPEN);
    }

    public ProcessGui(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull Report report, @NotNull OnlinePlayer reporter, @NotNull Report.Status previousFilter) {
        super(plugin, player);
        this.report = report;
        this.reporter = reporter;
        this.previousFilter = previousFilter;

        open();
    }

    @Override
    protected void open() {
        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {

            final InventoryGui gui = new InventoryGui(plugin, player, serialize(plugin.getGuiConfig().getProcess().getTitle().replace("%ID%", String.valueOf(report.getId()))), plugin.getGuiConfig().getProcess().getSetup());
            addCommonElements(gui, onlinePlayer);

            // TRUE
            gui.addElement(
                    new StaticGuiElement('t',
                            new ItemStack(plugin.getGuiConfig().getProcess().getMarkAsTrueMaterial()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                if (report.getMarkedAs() != null) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getAlreadyProcessed()));
                                    return true;
                                }

                                report.setMarkedAs(Report.MarkedAs.TRUE);
                                report.setStatus(Report.Status.ARCHIVED);
                                plugin.runAsync(task -> plugin.getReportsManager().updateReportData(player, report));
                                plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));

                                if (plugin.getSettings().isEnableRewardsSystem()) {
                                    this.reporter.getPreferences().setRewardsToClaim(this.reporter.getPreferences().getRewardsToClaim() + 1);
                                    plugin.getUsersManager().updatePlayer(this.reporter);

                                    final Player onlineReporter = Bukkit.getPlayer(report.getReporter());
                                    if (onlineReporter != null) onlineReporter.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getRewards()));
                                }

                                // Navigate back to the previous reports list page
                                plugin.runAsync(task -> {
                                    Set<Report> reports;
                                    if (previousFilter.equals(Report.Status.WAITING)) {
                                        reports = plugin.getReportsManager().getWaitingReports();
                                    } else if (previousFilter.equals(Report.Status.IN_PROGRESS)) {
                                        reports = plugin.getReportsManager().getInProgressReports();
                                    } else if (previousFilter.equals(Report.Status.DONE)) {
                                        reports = plugin.getReportsManager().getClosedReports();
                                    } else if (previousFilter.equals(Report.Status.ARCHIVED)) {
                                        reports = plugin.getReportsManager().getArchivedReports();
                                    } else {
                                        reports = plugin.getReportsManager().getAllReports();
                                    }
                                    plugin.run(t -> new ReportsList(plugin, player, reports, previousFilter));
                                });

                                return true;
                            },
                            serialize(
                                    plugin.getGuiConfig().getProcess().getMarkAsTrue()
                            )
                    )
            );

            // FALSE
            gui.addElement(
                    new StaticGuiElement('y',
                            new ItemStack(plugin.getGuiConfig().getProcess().getMarkAsFalseMaterial()),
                            1,
                            click -> {
                                if (!click.getWhoClicked().hasPermission("ultimatereports.reports.manage")) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
                                    return true;
                                }

                                if (report.getMarkedAs() != null) {

                                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getAlreadyProcessed()));
                                    return true;
                                }

                                plugin.getReportsManager().deleteReport(player, report);

                                // Navigate back to the previous reports list page
                                plugin.runAsync(task -> {
                                    Set<Report> reports;
                                    if (previousFilter.equals(Report.Status.WAITING)) {
                                        reports = plugin.getReportsManager().getWaitingReports();
                                    } else if (previousFilter.equals(Report.Status.IN_PROGRESS)) {
                                        reports = plugin.getReportsManager().getInProgressReports();
                                    } else if (previousFilter.equals(Report.Status.DONE)) {
                                        reports = plugin.getReportsManager().getClosedReports();
                                    } else if (previousFilter.equals(Report.Status.ARCHIVED)) {
                                        reports = plugin.getReportsManager().getArchivedReports();
                                    } else {
                                        reports = plugin.getReportsManager().getAllReports();
                                    }
                                    plugin.run(t -> new ReportsList(plugin, player, reports, previousFilter));
                                });

                                return true;
                            },
                            serialize(
                                    plugin.getGuiConfig().getProcess().getMarkAsFalse()
                            )
                    )
            );

            gui.show(player);
        });

    }
}