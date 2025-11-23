package dev.xf3d3.ultimatereports.discord.bot;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.config.Discord;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.Report;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.modals.Modal;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

public class DiscordBotSender {
    private final UltimateReports plugin;

    public DiscordBotSender(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    public void sendReportEmbed(@NotNull Report report) {
        if (plugin.getDiscordBot().isEmpty()) {
            return;
        }

        String channelId = plugin.getSettings().getDiscord().getBot().getReportsChannelId();
        if (channelId == null || channelId.isEmpty()) {
            plugin.log(Level.WARNING, "Discord Log Channel ID is empty, cannot send Embed.");
            return;
        }

        TextChannel channel = plugin.getDiscordBot().get().getJda().getTextChannelById(channelId);
        if (channel == null) {

            plugin.log(Level.WARNING, "Discord Log Channel not found! Check the ID: " + channelId);
            return;
        }


        channel
                .sendMessageEmbeds(getReportEmbed(report))
                .addComponents(ActionRow.of(getReportButtons(report)))
                .queue(message -> plugin.runSync(task -> {
                    Player randomPlayer = plugin.getUsersManager().getRandomPlayer();

                    report.setDiscordMessageId(message.getId());
                    plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));
                }));
    }

    public void updateReportMessage(@NotNull Report report) {
        String msgId = report.getDiscordMessageId();
        if (msgId == null || plugin.getDiscordBot().isEmpty()) return;

        TextChannel channel = plugin.getDiscordBot().get().getJda().getTextChannelById(plugin.getSettings().getDiscord().getBot().getReportsChannelId());
        if (channel == null) return;


        channel.retrieveMessageById(msgId).queue(message -> {
            message.editMessageEmbeds(getReportEmbed(report))
                    .setComponents(ActionRow.of(getReportButtons(report)))
                    .queue();
        });
    }

    public void deleteReportMessage(@NotNull Report report) {
        String msgId = report.getDiscordMessageId();
        if (msgId == null || plugin.getDiscordBot().isEmpty()) return;

        TextChannel channel = plugin.getDiscordBot().get().getJda().getTextChannelById(plugin.getSettings().getDiscord().getBot().getReportsChannelId());
        if (channel == null) return;


        channel.deleteMessageById(msgId).queue(
                success -> plugin.runSync(task -> {
                    Player randomPlayer = plugin.getUsersManager().getRandomPlayer();

                    report.setDiscordMessageId(null);
                    plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));
                })
        );
    }

    public List<Button> getReportButtons(@NotNull Report report) {

        return List.of(
                Button.primary("waiting:" + report.getId(), plugin.getDiscordConfig().getMessages().getButtons().getWaiting().getLabel())
                        .withDisabled(report.getStatus().equals(Report.Status.WAITING))
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getWaiting().getEmoji())),

                Button.primary("progress:" + report.getId(), plugin.getDiscordConfig().getMessages().getButtons().getProgress().getLabel())
                        .withDisabled(report.getStatus().equals(Report.Status.IN_PROGRESS))
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getProgress().getEmoji())),

                Button.success("done:" + report.getId(), plugin.getDiscordConfig().getMessages().getButtons().getDone().getLabel())
                        .withDisabled(report.getStatus().equals(Report.Status.DONE))
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getDone().getEmoji())),

                Button.secondary("archive:" + report.getId(), plugin.getDiscordConfig().getMessages().getButtons().getArchive().getLabel())
                        .withDisabled(report.getStatus().equals(Report.Status.ARCHIVED))
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getArchive().getEmoji())),

                Button.danger("delete:" + report.getId(), plugin.getDiscordConfig().getMessages().getButtons().getDelete().getLabel())
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getDelete().getEmoji()))
        );
    }

    public MessageEmbed getReportEmbed(@NotNull Report report) {
        if (plugin.getDiscordConfig() == null) {

            plugin.log(Level.WARNING, "Discord Config is null!");
            throw new IllegalStateException("Discord Config is null!");
        }

        Discord.ReportEmbed config = plugin.getDiscordConfig().getReportEmbed();

        String reporterName = report.getReporterName();
        String reportedName = report.getReportedName();
        String reason = report.getReason();
        int id = report.getId();
        int comments = report.getComments().size();

        EmbedBuilder builder = new EmbedBuilder();

        // TITLE
        builder.setTitle(format(config.getTitle(), id, reporterName, reportedName, reason, comments));

        // COLOR
        builder.setColor(config.getColor());

        // TIMESTAMP
        builder.setTimestamp(Instant.now());

        // AUTHOR
        builder.setAuthor(
                format(config.getAuthor().getName(), id, reporterName, reportedName, reason, comments),
                null,
                config.getAuthor().getIcon()
        );

        // FIELDS
        for (Discord.ReportEmbed.Field f : config.getFields()) {
            builder.addField(
                    format(f.getName(), id, reporterName, reportedName, reason, comments),
                    format(f.getValue(), id, reporterName, reportedName, reason, comments),
                    f.isInline()
            );
        }

        // FOOTER
        builder.setFooter(
                format(config.getFooter().getText(), id, reporterName, reportedName, reason, comments),
                config.getFooter().getIcon()
        );

        return builder.build();
    }

    private String format(String text, int id, String reporter, String reported, String reason, int comments) {
        if (text == null) return null;
        return text
                .replace("%ID%", String.valueOf(id))
                .replace("%REPORTER%", reporter != null ? reporter : "Unknown")
                .replace("%REPORTED%", reported != null ? reported : "Unknown")
                .replace("%COMMENTS_COUNT%", String.valueOf(comments))
                .replace("%REASON%", reason != null ? reason : "No Reason");
    }

    public void sendCommentEmbed(@NotNull Report report, @NotNull Comment comment) {
        if (plugin.getDiscordBot().isEmpty() || !plugin.getSettings().getDiscord().getBot().isSendNewComments()) {
            return;
        }

        String channelId = plugin.getSettings().getDiscord().getBot().getCommentsChannelId();
        if (channelId == null || channelId.isEmpty()) {
            plugin.log(Level.WARNING, "Discord Log Channel ID is empty, cannot send Embed.");
            return;
        }

        TextChannel channel = plugin.getDiscordBot().get().getJda().getTextChannelById(channelId);
        if (channel == null) {

            plugin.log(Level.WARNING, "Discord Log Channel not found! Check the ID: " + channelId);
            return;
        }


        channel
                .sendMessageEmbeds(getCommentsEmbed(report, comment))
                .addComponents(ActionRow.of(getCommentsButtons(report, comment)))
                .queue(message -> plugin.runSync(task -> {
                    Player randomPlayer = plugin.getUsersManager().getRandomPlayer();

                    comment.setDiscordMessageId(message.getId());
                    report.addOrUpdateComment(comment);
                    plugin.runAsync(task1 -> plugin.getReportsManager().updateReportData(randomPlayer, report));
                }));
    }

    public MessageEmbed getCommentsEmbed(@NotNull Report report, @NotNull Comment comment) {
        if (plugin.getDiscordConfig() == null) {

            plugin.log(Level.WARNING, "Discord Config is null!");
            throw new IllegalStateException("Discord Config is null!");
        }

        Discord.CommentsEmbed config = plugin.getDiscordConfig().getCommentsEmbed();

        EmbedBuilder builder = new EmbedBuilder();

        // TITLE
        builder.setTitle(config.getTitle().replace("%ID%", String.valueOf(report.getId())));

        // COLOR
        builder.setColor(config.getColor());

        // TIMESTAMP
        builder.setTimestamp(Instant.now());

        // AUTHOR
        builder.setAuthor(
                config.getAuthor().getName(),
                null,
                config.getAuthor().getIcon()
        );

        // FIELDS
        for (Discord.CommentsEmbed.Field f : config.getFields()) {
            builder.addField(
                    f.getName(),
                    f.getValue()
                            .replace("%AUTHOR%", comment.getAuthorName())
                            .replace("%STATUS%", plugin.getMessages().getGeneral().getCommentsStatus().get(comment.getStatus()))
                            .replace("%COMMENT%", comment.getMessage()),
                    f.isInline()
            );
        }

        // FOOTER
        builder.setFooter(
                config.getFooter().getText(),
                config.getFooter().getIcon()
        );

        return builder.build();
    }

    public void updateCommentMessage(@NotNull Report report, @NotNull Comment comment) {
        String msgId = comment.getDiscordMessageId();
        if (msgId == null || plugin.getDiscordBot().isEmpty()) return;

        TextChannel channel = plugin.getDiscordBot().get().getJda().getTextChannelById(plugin.getSettings().getDiscord().getBot().getCommentsChannelId());
        if (channel == null) return;


        channel.retrieveMessageById(msgId).queue(message -> {
            message.editMessageEmbeds(getCommentsEmbed(report, comment))
                    .setComponents(ActionRow.of(getCommentsButtons(report, comment)))
                    .queue();
        });
    }

    public List<Button> getCommentsButtons(@NotNull Report report, @NotNull Comment comment) {

        assert plugin.getDiscordConfig() != null;
        return List.of(
                Button.primary("add_comment:" + report.getId(), plugin.getDiscordConfig().getMessages().getButtons().getAdd_comment().getLabel())
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getAdd_comment().getEmoji())),

                Button.success("read:" + report.getId() + ":" + comment.getId(), plugin.getDiscordConfig().getMessages().getButtons().getRead().getLabel())
                        .withDisabled(comment.getStatus().equals(Comment.MessageStatus.READ))
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getRead().getEmoji())),

                Button.danger("delete:" + report.getId() + ":" + comment.getId(), plugin.getDiscordConfig().getMessages().getButtons().getDelete().getLabel())
                        .withEmoji(Emoji.fromUnicode(plugin.getDiscordConfig().getMessages().getButtons().getDelete().getEmoji()))
        );
    }

    public Modal getAddCommentModal(@NotNull Report report) {
        assert plugin.getDiscordConfig() != null;

        TextInput subject = TextInput.create("name", TextInputStyle.SHORT)
                .setPlaceholder(plugin.getDiscordConfig().getMessages().getCommentModal().getNamePlaceholder())
                .setMinLength(3)
                .setMaxLength(16)
                .build();

        TextInput body = TextInput.create("comment", TextInputStyle.PARAGRAPH)
                .setPlaceholder(plugin.getDiscordConfig().getMessages().getCommentModal().getCommentPlaceholder())
                .setMinLength(1)
                .setMaxLength(100)
                .build();

        return Modal.create("modal_add_comment:" + report.getId(), plugin.getDiscordConfig().getMessages().getCommentModal().getTitle().replace("%ID%", String.valueOf(report.getId())))
                .addComponents(Label.of(plugin.getDiscordConfig().getMessages().getCommentModal().getNameLabel(), subject), Label.of(plugin.getDiscordConfig().getMessages().getCommentModal().getCommentLabel(), body))
                .build();
    }
}