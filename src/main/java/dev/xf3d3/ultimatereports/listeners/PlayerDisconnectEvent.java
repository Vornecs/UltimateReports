package dev.xf3d3.ultimatereports.listeners;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.User;
import dev.xf3d3.ultimatereports.network.Broker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerDisconnectEvent implements Listener {
    private final UltimateReports plugin;
    public PlayerDisconnectEvent(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        plugin.getUsersManager().removePlayer(player.getUniqueId());
        plugin.getUsersManager().getOnlineUserMap().remove(player.getUniqueId());

        if (plugin.getSettings().getCrossServer().isEnabled()) {
            final List<User> localPlayerList = plugin.getUsersManager().getOnlineUserMap().values().stream()
                    .filter(u -> !u.equals(player)).map(u -> User.of(u.getUniqueId(), u.getName())).toList();

            // Update global user list if needed
            if (plugin.getSettings().getCrossServer().getBrokerType() == Broker.Type.REDIS) {
                plugin.getUsersManager().syncGlobalUserList(player, localPlayerList);
            } else {
                plugin.getUsersManager().getOnlineUserMap().values().stream()
                        .filter(u -> !u.equals(player))
                        .findAny()
                        .ifPresent(user -> plugin.getUsersManager().syncGlobalUserList(user, localPlayerList));
            }
        }
    }
}