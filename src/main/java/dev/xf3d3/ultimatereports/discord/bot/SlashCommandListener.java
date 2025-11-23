package dev.xf3d3.ultimatereports.discord.bot;

import dev.xf3d3.ultimatereports.UltimateReports;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlashCommandListener extends ListenerAdapter {
    private final UltimateReports plugin;

    public SlashCommandListener(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!Objects.equals(event.getSubcommandName(), "check"))
            return;

        if (event.getOption("id") == null) {

            event.reply("Wrong usage! Provide the report ID to check.");
            return;
        }

        event.deferReply().queue();
        plugin.getReportsManager().getReportById(Objects.requireNonNull(event.getOption("id")).getAsInt()).ifPresentOrElse(
                report -> {
                    plugin.runSync(task -> {
                        if (plugin.getDiscordBot().isEmpty()) {

                             // if the bot doesn't exist we can't send the message anyway
                            return;
                        }

                        event.getHook()
                                .sendMessageEmbeds(plugin.getDiscordBot().get().getEmbedSender().getReportEmbed(report))
                                .addComponents(ActionRow.of(plugin.getDiscordBot().get().getEmbedSender().getReportButtons(report)))
                                .queue();
                    });
                },
                () -> event.getHook().sendMessage(plugin.getDiscordConfig().getMessages().getReplyMessages().getReportNotFound().replace("%ID%", event.getOption("id").getAsString())).queue()
        );
    }
}
