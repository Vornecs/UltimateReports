package dev.xf3d3.ultimatereports.discord.bot;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.Report;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class ModalListener extends ListenerAdapter {
    private final UltimateReports plugin;

    public ModalListener(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        assert plugin.getDiscordConfig() != null;
        event.deferReply().setEphemeral(true).queue();
        String[] args = event.getModalId().split(":");

        int reportId;
        try {
            reportId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {

            event.getHook().setEphemeral(true).sendMessage("There was an unexpected error while performing this action").setEphemeral(true).queue();
            return;
        }


        // GET THE REPORT
        Optional<Report> optionalReport = plugin.getReportsManager().getReportById(reportId);
        if (optionalReport.isEmpty()) {

            event.getHook().setEphemeral(true).sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportNotFound().replace("%ID%", String.valueOf(reportId))).queue();
            return;
        }
        Report report = optionalReport.get();


        if (!args[0].equals("modal_add_comment")) return;

        String authorName = Objects.requireNonNull(event.getValue("name")).getAsString();
        String message = Objects.requireNonNull(event.getValue("comment")).getAsString();

        plugin.runSync(task -> {
            final OfflinePlayer author = Bukkit.getOfflinePlayerIfCached(authorName);
            if (author == null) {

                event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getPlayerNotFound()).setEphemeral(true).queue();
                return;
            }

            Comment comment = Comment.create(author, message);
            report.getComments().add(comment);

            final Player randomPlayer = plugin.getUsersManager().getRandomPlayer();
            plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));

            // DISCORD
            plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().updateReportMessage(report));
            plugin.getDiscordBot().ifPresent(bot -> bot.getEmbedSender().sendCommentEmbed(report, comment));
            plugin.getWebHookSender().ifPresent(sender -> plugin.runAsync(task2 -> sender.sendCommentEmbed(report, comment)));

            plugin.getReportsManager().notifyReporter(report, plugin.getMessages().getReport().getNewComment());

            event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getCommentAdded()).setEphemeral(true).queue();
        });

    }
}
