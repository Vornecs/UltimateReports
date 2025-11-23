package dev.xf3d3.ultimatereports.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.dialogs.CommentDialog;
import dev.xf3d3.ultimatereports.gui.ReportGui;
import dev.xf3d3.ultimatereports.gui.ReportsList;
import dev.xf3d3.ultimatereports.gui.RewardsGui;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("unused")
@CommandAlias("reports")
@CommandPermission("ultimatereports.reports")
public class ReportsCommand extends BaseCommand {
    private final UltimateReports plugin;

    public ReportsCommand(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }


    @Default
    public void onReports(Player player) {
        Set<Report> reports;
        if (player.hasPermission("ultimatereports.reports.manage")) {
            reports = plugin.getReportsManager().getOpenReports();
        } else {
            reports = plugin.getReportsManager().getPlayerReports(player.getUniqueId());
        }

        new ReportsList(plugin, player, reports, Report.Status.OPEN);
    }

    @Subcommand("reload")
    @CommandPermission("ultimatereports.reload")
    public void onReload(CommandSender sender) {

        plugin.loadConfigs();
        plugin.getUtils().setDateFormat();

        sender.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getPluginReloaded()));
    }

    @Subcommand("about")
    @CommandCompletion("@nothing")
    @CommandPermission("ultimatereports.about")
    public void aboutSubcommand(CommandSender sender) {
        sender.sendMessage(MineDown.parse("         &6UltimateReports&r         "));
        sender.sendMessage(MineDown.parse("&3Version: &6" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(MineDown.parse("&3Database Type: &6" + plugin.getSettings().getDatabase().getType().getDisplayName()));
        plugin.getMessageBroker().ifPresent(broker -> sender.sendMessage(MineDown.parse("&3Broker Type: &6" + plugin.getSettings().getCrossServer().getBrokerType().getDisplayName())));
        sender.sendMessage(MineDown.parse("&3Author: &6" + plugin.getPluginMeta().getAuthors()));
        sender.sendMessage(MineDown.parse("&3Contributors: &6" + plugin.getPluginMeta().getContributors()));
        sender.sendMessage(MineDown.parse("&3Description: &6" + plugin.getPluginMeta().getDescription()));
        sender.sendMessage(MineDown.parse("&3Wiki: https://ultimatereports.gitbook.io/ultimatereports-docs"));
        sender.sendMessage(MineDown.parse("         &6UltimateReports&r         "));
    }


    @Subcommand("notify")
    @CommandPermission("ultimatereports.notify")
    public void onNotify(Player player) {
        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {
           if (onlinePlayer.getPreferences().isNotifications()) {

               player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getNotifications().getNotificationsDisabled()));
               onlinePlayer.getPreferences().setNotifications(false);
               plugin.getUsersManager().updatePlayer(onlinePlayer);
           } else {

               player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getNotifications().getNotificationsEnabled()));
               onlinePlayer.getPreferences().setNotifications(true);
               plugin.getUsersManager().updatePlayer(onlinePlayer);
           }
        });
    }

    @Subcommand("check")
    @CommandPermission("ultimatereports.reports.check")
    @CommandCompletion("@reports @nothing")
    public void onCheck(Player player, @Values("@reports") int id) {
        if (plugin.getUsersManager().canAddComments(player, id)) {
            new ReportGui(plugin, player, id);

            return;
        }

        player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
    }


    @Subcommand("comment")
    @Description("Add a comment to a report")
    @CommandPermission("ultimatereports.reports.comment")
    @CommandCompletion("@reports <comment> @nothing")
    public void onComment(Player player, @Values("@reports") int id, @Optional String[] args) {
        if (!plugin.getUsersManager().canAddComments(player, id)) {

            player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getNoPermission()));
            return;
        }

        String commentText = String.join(" ", args);
        plugin.getReportsManager().getReportById(id).ifPresentOrElse(
                report -> {
                    if (commentText.isEmpty()) {
                        if (plugin.getUsersManager().canUseDialogs(player)) {

                            new CommentDialog(plugin, player, report);
                            return;
                        }

                        player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getComment().getCommentCannotBeEmpty()));
                        return;
                    }

                    plugin.getReportsManager().addComment(player, report, commentText);
                    player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getComment().getCommentAdded()));
                },
                () -> player.sendMessage(MineDown.parse("&cReport does not exist!"))
        );
    }

    @Subcommand("cooldown add")
    @Description("Add a cooldown in minutes to a player")
    @CommandPermission("ultimatereports.cooldown.add")
    @CommandCompletion("@onlineUsers <minutes> @nothing")
    public void onCooldownAdd(CommandSender sender, OfflinePlayer offlinePlayer, int minutes) {
        plugin.getUsersManager().getPlayer(offlinePlayer.getUniqueId()).thenAccept(onlinePlayer -> {

           onlinePlayer.addCoolDown(plugin, minutes);
           sender.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getCooldown().getCooldownAdd()
                   .replace("%COOLDOWN%", plugin.getUtils().formatTime(onlinePlayer.getRemainingTime()))
                   .replace("%PLAYER%", onlinePlayer.getLastPlayerName())
           ));
        });
    }

    @Subcommand("cooldown reset")
    @Description("Reset a player's cooldown")
    @CommandPermission("ultimatereports.cooldown.reset")
    @CommandCompletion("@onlineUsers @nothing")
    public void onCooldownReset(CommandSender sender, OfflinePlayer offlinePlayer) {
        plugin.getUsersManager().getPlayer(offlinePlayer.getUniqueId()).thenAccept(onlinePlayer -> {

            onlinePlayer.getPreferences().setCooldown(0);
            plugin.getUsersManager().updatePlayer(onlinePlayer);
            sender.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getCooldown().getCooldownReset()));
        });
    }

    @Subcommand("cooldown")
    @Description("Check a player's cooldown")
    @CommandPermission("ultimatereports.cooldown.check")
    @CommandCompletion("@onlineUsers @nothing")
    public void onCooldownCheck(CommandSender sender, OfflinePlayer offlinePlayer) {
        plugin.getUsersManager().getPlayer(offlinePlayer.getUniqueId()).thenAccept(onlinePlayer -> {

            sender.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getCooldown().getCooldownCheck()
                    .replace("%COOLDOWN%", plugin.getUtils().formatTime(onlinePlayer.getRemainingTime()))
            ));
        });
    }

    @Subcommand("reward")
    @Description("Claim the reward for your report")
    @CommandPermission("ultimatereports.reward")
    @CommandCompletion("@nothing")
    public void onReward(Player player) {
        if (!plugin.getSettings().isEnableRewardsSystem()) {

            player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getGeneral().getFeatureDisabled()));
            return;
        }

        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {

            if (onlinePlayer.getPreferences().getRewardsToClaim() < 1) {

                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getNoRewardsToClaim()));
                return;
            }

            new RewardsGui(plugin, player, onlinePlayer);

        });
    }
}
