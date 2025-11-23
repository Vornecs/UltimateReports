package dev.xf3d3.ultimatereports.managers;

import com.google.common.collect.Maps;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Preferences;
import dev.xf3d3.ultimatereports.models.Report;
import dev.xf3d3.ultimatereports.models.User;
import dev.xf3d3.ultimatereports.network.Broker;
import dev.xf3d3.ultimatereports.network.Message;
import dev.xf3d3.ultimatereports.network.Payload;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.stream.Stream;

public class UsersManager {

    @Getter
    private final Map<UUID, OnlinePlayer> usermap = Maps.newConcurrentMap();
    private final UltimateReports plugin;

    @Getter
    private final Map<String, List<User>> globalUserList = Maps.newConcurrentMap();
    @Getter
    private final ConcurrentMap<UUID, Player> onlineUserMap = Maps.newConcurrentMap();


    public UsersManager(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    public Set<UUID> getRawUsermapList(){
        return usermap.keySet();
    }

    public List<User> getUserList() {
        return Stream.concat(
                globalUserList.values().stream().flatMap(Collection::stream),
                onlineUserMap.values().stream().map(player -> User.of(player.getUniqueId(), player.getName()))
        ).distinct().sorted().toList();
    }

    public Collection<Player> getOnlineUsers() {
        return getOnlineUserMap().values();
    }

    public Optional<Player> findOnlinePlayer(@NotNull String username) {
        return onlineUserMap.values().stream()
                .filter(online -> online.getName().equalsIgnoreCase(username))
                .findFirst();
    }

    public void setUserList(@NotNull String server, @NotNull List<User> players) {
        globalUserList.values().forEach(list -> {
            list.removeAll(players);
            list.removeAll(onlineUserMap.values().stream().map(player -> User.of(player.getUniqueId(), player.getName())).toList());
        });
        globalUserList.put(server, players);
    }

    // Synchronize the global player list
    public void syncGlobalUserList(@NotNull Player user, @NotNull List<User> onlineUsers) {
        final Optional<Broker> optionalBroker = plugin.getMessageBroker();
        if (optionalBroker.isEmpty()) {
            return;
        }
        final Broker broker = optionalBroker.get();

        // Send this server's player list to all servers
        Message.builder()
                .type(Message.Type.UPDATE_USER_LIST)
                .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                .payload(Payload.userList(onlineUsers))
                .build().send(broker, user);

        // Clear cached global player lists and request updated lists from all servers
        if (this.onlineUserMap.size() == 1) {
            this.globalUserList.clear();
            Message.builder()
                    .type(Message.Type.REQUEST_USER_LIST)
                    .target(Message.TARGET_ALL, Message.TargetType.SERVER)
                    .build().send(broker, user);
        }
    }

    public void removePlayer(UUID uuid) {
        usermap.remove(uuid);
    }

    public CompletableFuture<OnlinePlayer> getPlayer(UUID uuid) {
        final String name = Bukkit.getOfflinePlayer(uuid).getName();

        if (usermap.containsKey(uuid)) {
            return CompletableFuture.completedFuture(usermap.get(uuid));
        }

        return plugin.supplyAsync(() -> plugin.getDatabase().getPlayer(uuid).map(teamPlayer -> {
            usermap.put(teamPlayer.getUuid(), teamPlayer);

            return teamPlayer;
        }).orElseGet(() -> {

            if (name == null) {
                throw new IllegalArgumentException("Player " + uuid + " not found");
            }

            OnlinePlayer onlinePlayer = new OnlinePlayer(uuid, name, Preferences.getDefaults());
            plugin.getDatabase().createPlayer(onlinePlayer);
            usermap.put(uuid, onlinePlayer);

            return onlinePlayer;
        })).whenComplete((result, ex) -> {
            if (ex != null) {
                plugin.log(Level.WARNING, "Error while trying to get player", ex);
            }
        });
    }


    public void updatePlayerName(Player player) {
        UUID uuid = player.getUniqueId();
        String newPlayerName = player.getName();

        OnlinePlayer onlinePlayer = usermap.get(uuid);
        onlinePlayer.setLastPlayerName(newPlayerName);

        plugin.runAsync(task -> plugin.getDatabase().updatePlayer(onlinePlayer));
        usermap.replace(uuid, onlinePlayer);
    }

    public void updatePlayer(OnlinePlayer onlinePlayer) {
        plugin.runAsync(task -> plugin.getDatabase().updatePlayer(onlinePlayer));
        usermap.replace(onlinePlayer.getUuid(), onlinePlayer);
    }

    public boolean canUseDialogs(@NotNull Player player) {
        return plugin.getViaVersionHook() != null && plugin.getViaVersionHook().getPlayerVersion(player) >= 771 && plugin.getSettings().isUseDialogs();
    }

    public boolean canAddComments(@NotNull Player player, @NotNull Report report) {
        return player.hasPermission("ultimatereports.reports.manage") || player.getUniqueId().equals(report.getReporter());
    }

    public boolean canAddComments(@NotNull Player player, int id) {
        return plugin.getReportsManager().getReportById(id)
                .map(r -> player.hasPermission("ultimatereports.reports.manage") || player.getUniqueId().equals(r.getReporter()))
                .orElse(false);
    }

    public Player getRandomPlayer() {
        return Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
    }
}
