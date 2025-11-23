package dev.xf3d3.ultimatereports.listeners;

import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerConnectEvent implements Listener {

    private final UltimateReports plugin;
    public PlayerConnectEvent(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getUsersManager().getOnlineUserMap().remove(uuid);
        plugin.getUsersManager().getOnlineUserMap().put(player.getUniqueId(), player);

        plugin.getUsersManager().getPlayer(player.getUniqueId()).thenAccept(onlinePlayer -> {
            // Check if name has changes
            if (!player.getName().equals(onlinePlayer.getLastPlayerName())) {
                plugin.getUsersManager().updatePlayerName(player);
            }

            // Check if player needs to be teleported
            onlinePlayer.getPreferences().getTeleportTarget().ifPresent(position -> {
                        plugin.getUtils().teleport(player, position);

                        onlinePlayer.getPreferences().clearTeleportTarget();
                        plugin.getUsersManager().updatePlayer(onlinePlayer);
                    }
            );

            // REMIND TO CLAIM REWARDS
            if (onlinePlayer.getPreferences().getRewardsToClaim() > 0) {
                player.sendMessage(MineDown.parse(plugin.getSettings().getGeneral().getPrefix() + plugin.getMessages().getReport().getRewards()));
            }

        });

        // Synchronize the global player list
        plugin.runSyncDelayed(() -> plugin.getUsersManager().syncGlobalUserList(
                player, plugin.getUsersManager().getOnlineUserMap().values().stream().map(online -> User.of(online.getUniqueId(), online.getName())).toList()), 40L
        );
    }
}
