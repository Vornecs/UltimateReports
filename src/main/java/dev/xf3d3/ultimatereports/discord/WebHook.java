package dev.xf3d3.ultimatereports.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.config.Discord;
import dev.xf3d3.ultimatereports.models.Comment;
import dev.xf3d3.ultimatereports.models.Report;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

public class WebHook {
    private final UltimateReports plugin;
    private final HttpClient client;

    public WebHook(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
    }

    public void sendReportEmbed(@NotNull Report report) {
        // Check if url is not empty
        if (plugin.getSettings().getDiscord().getWebhook().getReportsWebHookUrl().isEmpty()) {
            plugin.log(Level.WARNING, "Discord Webhook URL is empty! Cannot send webhook.");

            return;
        }

        if (plugin.getDiscordConfig() == null) {
            plugin.log(Level.WARNING, "Discord Config is null! Cannot send webhook.");

            return;
        }

        Discord.ReportEmbed config = plugin.getDiscordConfig().getReportEmbed();
        JsonObject root = new JsonObject();
        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();

        // TITLE, COLOR, TIMESTAMP
        embed.addProperty("title", config.getTitle().replace("%ID%", String.valueOf(report.getId())));
        embed.addProperty("color", config.getColor());
        embed.addProperty("timestamp", Instant.now().toString());

        // AUTHOR
        JsonObject authorObj = new JsonObject();
        authorObj.addProperty("name", config.getAuthor().getName());
        authorObj.addProperty("icon_url", config.getAuthor().getIcon());
        embed.add("author", authorObj);


        // FIELDS
        JsonArray fieldsArray = new JsonArray();
        for (Discord.ReportEmbed.Field f : config.getFields()) {
            JsonObject fieldObj = new JsonObject();

            fieldObj.addProperty("name", f.getName());
            fieldObj.addProperty("value", f.getValue()
                    .replace("%REPORTER%", report.getReporterName())
                    .replace("%REPORTED%", report.getReportedName())
                    .replace("%REASON%", report.getReason())
                    .replace("%COMMENTS_COUNT%", String.valueOf(report.getComments().size()))
            );
            fieldObj.addProperty("inline", f.isInline());

            fieldsArray.add(fieldObj);
        }
        embed.add("fields", fieldsArray);

        // FOOTER
        JsonObject footerObj = new JsonObject();
        footerObj.addProperty("text", config.getFooter().getText());
        footerObj.addProperty("icon_url", config.getFooter().getIcon());
        embed.add("footer", footerObj);

        embeds.add(embed);
        root.add("embeds", embeds);
        root.add("attachments", new JsonArray());

        sendWebHook(plugin.getSettings().getDiscord().getWebhook().getReportsWebHookUrl(), root.toString());
    }

    public void sendCommentEmbed(@NotNull Report report, @NotNull Comment comment) {
        // Check if url is not empty
        if (plugin.getSettings().getDiscord().getWebhook().getCommentsWebHookUrl().isEmpty()) {
            plugin.log(Level.WARNING, "Discord Webhook URL is empty! Cannot send webhook.");

            return;
        }

        if (plugin.getDiscordConfig() == null) {
            plugin.log(Level.WARNING, "Discord Config is null! Cannot send webhook.");

            return;
        }

        Discord.CommentsEmbed config = plugin.getDiscordConfig().getCommentsEmbed();
        JsonObject root = new JsonObject();
        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();

        // TITLE, COLOR, TIMESTAMP
        embed.addProperty("title", config.getTitle().replace("%ID%", String.valueOf(report.getId())));
        embed.addProperty("color", config.getColor());
        embed.addProperty("timestamp", Instant.now().toString());

        // AUTHOR
        JsonObject authorObj = new JsonObject();
        authorObj.addProperty("name", config.getAuthor().getName());
        authorObj.addProperty("icon_url", config.getAuthor().getIcon());
        embed.add("author", authorObj);


        // FIELDS
        JsonArray fieldsArray = new JsonArray();
        for (Discord.CommentsEmbed.Field f : config.getFields()) {
            JsonObject fieldObj = new JsonObject();

            fieldObj.addProperty("name", f.getName());
            fieldObj.addProperty("value", f.getValue()
                    .replace("%AUTHOR%", comment.getAuthorName())
                    .replace("%STATUS%", plugin.getMessages().getGeneral().getCommentsStatus().get(comment.getStatus()))
                    .replace("%COMMENT%", comment.getMessage())
            );
            fieldObj.addProperty("inline", f.isInline());

            fieldsArray.add(fieldObj);
        }
        embed.add("fields", fieldsArray);

        // FOOTER
        JsonObject footerObj = new JsonObject();
        footerObj.addProperty("text", config.getFooter().getText());
        footerObj.addProperty("icon_url", config.getFooter().getIcon());
        embed.add("footer", footerObj);

        embeds.add(embed);
        root.add("embeds", embeds);
        root.add("attachments", new JsonArray());

        sendWebHook(plugin.getSettings().getDiscord().getWebhook().getCommentsWebHookUrl(), root.toString());
    }

    private void sendWebHook(@NotNull String uri, @NotNull String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                plugin.log(Level.SEVERE, "Error while sending webhook. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            plugin.log(Level.SEVERE, "Error while sending webhook to discord.", e);
        }

    }
}
