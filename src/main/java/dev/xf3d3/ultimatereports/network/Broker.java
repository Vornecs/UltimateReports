package dev.xf3d3.ultimatereports.network;

import dev.xf3d3.ultimatereports.UltimateReports;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * A broker for dispatching {@link Message}s across the proxy network
 */
@Getter
public abstract class Broker implements MessageHandler {

    protected final UltimateReports plugin;

    protected Broker(@NotNull UltimateReports plugin) {
        this.plugin = plugin;
    }

    protected void handle(@Nullable Player receiver, @NotNull Message message) {
        if (message.getSourceServer().equals(getServer())) {
            return;
        }
        switch (message.getType()) {
            case REQUEST_USER_LIST -> handleRequestUserList(message, receiver);
            case UPDATE_USER_LIST -> handleUpdateUserList(message);

            case REPORT_DELETE -> handleReportDelete(message);
            case REPORT_UPDATE -> handleReportUpdate(message);
            case REPORT_CREATE -> handleReportCreate(message);

            default -> plugin.log(Level.SEVERE, "Received unknown message type: " + message.getType());
        }
    }

    /**
     * Initialize the message broker
     *
     * @throws RuntimeException if the broker fails to initialize
     */
    public abstract void initialize() throws RuntimeException;

    /**
     * Move a player to a different server
     *
     * @param user   the user
     * @param server the destination server ID
     */
    public abstract void changeServer(@NotNull Player user, @NotNull String server);

    /**
     * Send a message to the broker
     *
     * @param message the message to send
     * @param sender  the sender of the message
     */
    protected abstract void send(@NotNull Message message, @Nullable Player sender);

    /**
     * Terminate the broker
     */
    public abstract void close();

    /**
     * Get the sub-channel ID for broker communications
     *
     * @return the sub-channel ID
     */
    @NotNull
    protected String getSubChannelId() {
        return String.valueOf(plugin.getSettings().getCrossServer().getClusterId());
    }

    protected String getServer() {
        return plugin.getSettings().getCrossServer().getServerName();
    }

    // Returns the formatted version of the plugin (format: x.x)
    @NotNull
    private String getFormattedVersion() {
        return String.format("%s.%s", plugin.getPluginVersion().getMajor(), plugin.getPluginVersion().getMinor());
    }

    // Return this broker instance
    @NotNull
    @Override
    public Broker getBroker() {
        return this;
    }


    /**
     * Identifies types of message brokers
     */
    @Getter
    public enum Type {
        PLUGIN_MESSAGE("Plugin Messages"),
        REDIS("Redis");

        @NotNull
        private final String displayName;

        Type(@NotNull String displayName) {
            this.displayName = displayName;
        }
    }


}
