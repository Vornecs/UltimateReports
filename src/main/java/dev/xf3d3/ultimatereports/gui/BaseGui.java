package dev.xf3d3.ultimatereports.gui;

import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Report;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class BaseGui {
    protected final UltimateReports plugin;
    protected final Player player;

    public BaseGui(@NotNull UltimateReports plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    protected abstract void open();

    protected void addCommonElements(InventoryGui gui, OnlinePlayer onlinePlayer) {
        // Go back action base
        gui.setCloseAction(close -> true);

        // Filler (g)
        gui.addElement(new StaticGuiElement('g', new ItemStack(plugin.getGuiConfig().getShared().getFillerItem()), 1, click -> true, " "));

        // Close (f)
        gui.addElement(new StaticGuiElement('f', new ItemStack(plugin.getGuiConfig().getShared().getCloseItem()), 1, click -> {
            click.getGui().close();
            return true;
        }, serialize(plugin.getGuiConfig().getShared().getClose())));

        // Navigation (d, e, h, i)
        gui.addElement(new GuiPageElement('d', new ItemStack(plugin.getGuiConfig().getShared().getFirstPageItem()), GuiPageElement.PageAction.FIRST, serialize(plugin.getGuiConfig().getShared().getFirstPage())));
        gui.addElement(new GuiPageElement('e', new ItemStack(plugin.getGuiConfig().getShared().getPrevPageItem()), GuiPageElement.PageAction.PREVIOUS, serialize(plugin.getGuiConfig().getShared().getPrevPage())));
        gui.addElement(new GuiPageElement('h', new ItemStack(plugin.getGuiConfig().getShared().getNextPageItem()), GuiPageElement.PageAction.NEXT, serialize(plugin.getGuiConfig().getShared().getNextPage())));
        gui.addElement(new GuiPageElement('i', new ItemStack(plugin.getGuiConfig().getShared().getLastPageItem()), GuiPageElement.PageAction.LAST, serialize(plugin.getGuiConfig().getShared().getLastPage())));

        // Notifications status (a)
        GuiStateElement notificationsStatus = new GuiStateElement('a',
                new GuiStateElement.State(
                        change -> {

                            change.getWhoClicked().sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getNotifications().getNotificationsEnabled()));
                            onlinePlayer.getPreferences().setNotifications(true);
                            plugin.getUsersManager().updatePlayer(onlinePlayer);
                        },
                        "notificationsEnabled",
                        new ItemStack(plugin.getGuiConfig().getShared().getNotificationsItem()),
                        serialize(plugin.getGuiConfig().getShared().getDisableNotification())
                ),
                new GuiStateElement.State(
                        change -> {

                            change.getWhoClicked().sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getNotifications().getNotificationsDisabled()));
                            onlinePlayer.getPreferences().setNotifications(false);
                            plugin.getUsersManager().updatePlayer(onlinePlayer);
                        },
                        "notificationsDisabled",
                        new ItemStack(plugin.getGuiConfig().getShared().getNotificationsItem()),
                        serialize(plugin.getGuiConfig().getShared().getEnableNotification())
                )
        );

        if (onlinePlayer.getPreferences().isNotifications()) {
            notificationsStatus.setState("notificationsEnabled");
        } else {
            notificationsStatus.setState("notificationsDisabled");
        }
        gui.addElement(notificationsStatus);
    }

    protected String serialize(String text) {
        return LegacyComponentSerializer.legacySection().serialize(MineDown.parse(text));
    }

    protected String[] serialize(List<String> mineDownList) {
        return mineDownList.stream()
                .map(this::serialize)
                .toArray(String[]::new);
    }

    protected String[] getReportInfo(@NotNull Report report) {
        return plugin.getGuiConfig().getShared().getReportInfo().stream()
                .map(s -> s.replace("%STATUS%", "%" + report.getStatus().name() + "%"))
                .map(s -> s.replace("%ID%", String.valueOf(report.getId())))
                .map(s -> s.replace("%DATE%", plugin.getUtils().formatDate(report.getTimestamp())))
                .map(s -> s.replace("%REPORTER%", report.getReporterName()))
                .map(s -> s.replace("%REPORTED%", report.getReportedName()))
                .map(s -> s.replace("%REASON%", report.getReason()))
                .map(this::replaceStatus)
                .map(this::serialize)
                .toArray(String[]::new);
    }


    protected String replaceStatus(String text) {
        return text
                .replace("%ALL%", plugin.getMessages().getGeneral().getReportStatus().get(Report.Status.OPEN))
                .replace("%WAITING%", plugin.getMessages().getGeneral().getReportStatus().get(Report.Status.WAITING))
                .replace("%IN_PROGRESS%", plugin.getMessages().getGeneral().getReportStatus().get(Report.Status.IN_PROGRESS))
                .replace("%DONE%", plugin.getMessages().getGeneral().getReportStatus().get(Report.Status.DONE))
                .replace("%ARCHIVED%", plugin.getMessages().getGeneral().getReportStatus().get(Report.Status.ARCHIVED))
                .replace("%NOT_READ%", plugin.getMessages().getGeneral().getCommentsStatus().get(Comment.MessageStatus.NOT_READ))
                .replace("%READ%", plugin.getMessages().getGeneral().getCommentsStatus().get(Comment.MessageStatus.READ));
    }


    protected ItemStack enchanted(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createPlayerSkull(OfflinePlayer owner) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(owner);
            skull.setItemMeta(skullMeta);
        }
        return skull;
    }

    protected void navigateBackToReportsList(Report.Status previousFilter) {
        plugin.runAsync(task -> {
            Set<Report> reports = switch (previousFilter) {
                case WAITING -> plugin.getReportsManager().getWaitingReports();
                case IN_PROGRESS -> plugin.getReportsManager().getInProgressReports();
                case DONE -> plugin.getReportsManager().getClosedReports();
                case ARCHIVED -> plugin.getReportsManager().getArchivedReports();
                case OPEN -> plugin.getReportsManager().getAllReports();
            };
            plugin.run(t -> new ReportsList(plugin, player, reports, previousFilter));
        });
    }
}