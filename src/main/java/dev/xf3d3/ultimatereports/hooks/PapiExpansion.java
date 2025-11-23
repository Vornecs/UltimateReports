package dev.xf3d3.ultimatereports.hooks;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Preferences;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PapiExpansion extends PlaceholderExpansion {

    private final UltimateReports plugin;
    private final Set<UUID> requesting = ConcurrentHashMap.newKeySet();

    public PapiExpansion(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "UltimateReports";
    }

    @Override
    public @NotNull String getAuthor() {
        return "xF3d3";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginVersion().toStringWithoutMetadata();
    }

    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer player, String params) {

        if (params.startsWith("reports_")) {
            return Optional.of(params.substring("reports_".length()))
                    .map(name -> name.replace("%", ""))
                    .filter(name -> !name.isEmpty())
                    .map(Bukkit::getOfflinePlayerIfCached)
                    .map(OfflinePlayer::getUniqueId)
                    .map(uuid -> {
                        OnlinePlayer cached = plugin.getUsersManager().getUsermap().get(uuid);

                        if (cached == null && requesting.add(uuid)) {

                            plugin.getUsersManager().getPlayer(uuid)
                                    .thenRun(() -> requesting.remove(uuid));
                        }
                        return cached;
                    })
                    .map(OnlinePlayer::getPreferences)
                    .map(Preferences::getReports)
                    .map(String::valueOf)
                    .orElse("0");
        }

        if (params.startsWith("reported_")) {
            return Optional.of(params.substring("reported_".length()))
                    .map(name -> name.replace("%", ""))
                    .filter(name -> !name.isEmpty())
                    .map(Bukkit::getOfflinePlayerIfCached)
                    .map(OfflinePlayer::getUniqueId)
                    .map(uuid -> {
                        OnlinePlayer cached = plugin.getUsersManager().getUsermap().get(uuid);

                        if (cached == null && requesting.add(uuid)) {

                            plugin.getUsersManager().getPlayer(uuid)
                                    .thenRun(() -> requesting.remove(uuid));
                        }
                        return cached;
                    })
                    .map(OnlinePlayer::getPreferences)
                    .map(Preferences::getReported)
                    .map(String::valueOf)
                    .orElse("0");
        }


        return switch (params.toLowerCase()) {
            case "reports" -> Optional.ofNullable(plugin.getUsersManager().getUsermap().get(player.getUniqueId()))
                    .map(OnlinePlayer::getPreferences)
                    .map(Preferences::getReports)
                    .map(String::valueOf)
                    .orElse("0");

            case "reported" -> Optional.ofNullable(plugin.getUsersManager().getUsermap().get(player.getUniqueId()))
                    .map(OnlinePlayer::getPreferences)
                    .map(Preferences::getReported)
                    .map(String::valueOf)
                    .orElse("0");

            case "rewards" -> Optional.ofNullable(plugin.getUsersManager().getUsermap().get(player.getUniqueId()))
                    .map(OnlinePlayer::getPreferences)
                    .map(Preferences::getRewardsToClaim)
                    .map(String::valueOf)
                    .orElse("0");


            // No matching placeholder found
            default -> null;
        };
    }
}