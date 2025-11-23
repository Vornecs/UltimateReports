package dev.xf3d3.ultimatereports.discord.bot;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.Report;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ButtonListener extends ListenerAdapter {
    private final UltimateReports plugin;

    public ButtonListener(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        assert plugin.getDiscordConfig() != null;
        String[] args = event.getComponentId().split(":");

        int reportId;
        try {
            reportId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {

            event.reply("There was an unexpected error while performing this action").setEphemeral(true).queue();
            return;
        }


        // GET THE REPORT
        Optional<Report> optionalReport = plugin.getReportsManager().getReportById(reportId);
        if (optionalReport.isEmpty()) {

            event.reply(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportNotFound().replace("%ID%", String.valueOf(reportId))).queue();
            event.getMessage().editMessageComponents().queue();
            return;
        }
        Report report = optionalReport.get();

        String action = args[0];

        if (action.equals("add_comment")) {

            addCommentInteraction(event, report);
            return;
        }

        event.deferReply().setEphemeral(true).queue();
        if (args.length == 2) {

            reportInteraction(event, report, action);
        } else if (args.length == 3) {

            final UUID uuid = UUID.fromString(args[2]);
            final Optional<Comment> comment = report.getComment(uuid);

            if (comment.isEmpty()) {

                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getCommentNotFound()).queue();
                return;
            }

            commentInteraction(event, report, comment.get(), action);
        }

    }

    private void addCommentInteraction(@NotNull ButtonInteractionEvent event, @NotNull Report report) {
        if (plugin.getDiscordBot().isEmpty()) return;

        event.replyModal(plugin.getDiscordBot().get().getEmbedSender().getAddCommentModal(report)).queue();
    }

    private void commentInteraction(@NotNull ButtonInteractionEvent event, @NotNull Report report, @NotNull Comment comment, @NotNull String action) {
        switch (action) {
            case "read":
                comment.setStatus(Comment.MessageStatus.READ);
                report.addOrUpdateComment(comment);

                plugin.runSync(task -> {
                    Player randomPlayer = plugin.getUsersManager().getRandomPlayer();

                    plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getCommentUpdate());
                    plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));
                });

                if (plugin.getDiscordBot().isEmpty()) return;
                event.getMessage().editMessageEmbeds(plugin.getDiscordBot().get().getEmbedSender().getCommentsEmbed(report, comment))
                        .setComponents(ActionRow.of(plugin.getDiscordBot().get().getEmbedSender().getCommentsButtons(report, comment)))
                        .queue();

                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getCommentSetAsRead()).queue();

                break;

            case "delete":
                report.removeComment(comment.getId());
                plugin.runSync(task -> {
                    Player randomPlayer = plugin.getUsersManager().getRandomPlayer();

                    plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));
                });

                plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));
                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getCommentDeleted()).queue();
        }
    }

    private void reportInteraction(@NotNull ButtonInteractionEvent event, @NotNull Report report, @NotNull String action) {
        boolean updateMessage = false;

        switch (action) {
            case "waiting":
                if (report.getStatus() == Report.Status.WAITING) return;

                report.setStatus(Report.Status.WAITING);
                updateMessage = true;
                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportMarkedAsWaiting()).queue();
                break;

            case "progress":
                if (report.getStatus() == Report.Status.IN_PROGRESS) return;

                report.setStatus(Report.Status.IN_PROGRESS);
                updateMessage = true;
                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportMarkedAsInProgress()).queue();
                break;

            case "done":
                if (report.getStatus() == Report.Status.DONE) return;

                report.setStatus(Report.Status.DONE);
                updateMessage = true;
                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportMarkedAsDone()).queue();
                break;

            case "archive":
                if (report.getStatus() == Report.Status.ARCHIVED) return;

                report.setStatus(Report.Status.ARCHIVED);
                updateMessage = true;
                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportArchived()).queue();
                break;

            case "delete":


                plugin.runSync(task -> {
                    final Player player = plugin.getUsersManager().getRandomPlayer();

                    plugin.getReportsManager().deleteReport(player, report);
                });

                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportDeleted()).queue();
                event.getMessage().delete().queue();
                return;
        }

        if (updateMessage) {
            plugin.runSync(task -> {
                Player randomPlayer = plugin.getUsersManager().getRandomPlayer();

                plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getStatusUpdate());
                plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));
            });

            if (plugin.getDiscordBot().isEmpty()) return;
            event.getMessage().editMessageEmbeds(plugin.getDiscordBot().get().getEmbedSender().getReportEmbed(report))
                    .setComponents(ActionRow.of(plugin.getDiscordBot().get().getEmbedSender().getReportButtons(report)))
                    .queue();
        }
    }
}
