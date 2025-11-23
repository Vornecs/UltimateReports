package dev.xf3d3.ultimatereports.utils;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Position;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils {
    private final UltimateReports plugin;
    private DateTimeFormatter formatter;

    public Utils(@NotNull UltimateReports plugin) {
        this.plugin = plugin;

        setDateFormat();
    }

    public void teleport(@NotNull Player player, @NotNull Position position) {
        // If cross server is enabled and the position is not on this server
        if (plugin.getSettings().getCrossServer().isEnabled() && position.getServer() != null && !position.getServer().equals(plugin.getSettings().getCrossServer().getServerName())) {

            plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(user -> {
                user.getPreferences().setTeleportTarget(position);

                plugin.getUsersManager().updatePlayer(user);
                plugin.getMessageBroker().ifPresent(broker -> broker.changeServer(player, position.getServer()));
            });

            return;
        }

        player.teleportAsync(position.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    // todo: make the modifiable
    public String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "g " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public String formatDate(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .format(formatter);
    }

    public void setDateFormat() {
        this.formatter = DateTimeFormatter.ofPattern(plugin.getSettings().getGeneral().getDateFormat());
    }
}
