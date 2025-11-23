package dev.xf3d3.ultimatereports.network;

import com.google.common.collect.Sets;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.util.Set;
import java.util.logging.Level;

/**
 * Redis message broker implementation
 */
public class RedisBroker extends PluginMessageBroker {
    private Pool<Jedis> jedisPool;

    public RedisBroker(@NotNull UltimateReports plugin) {
        super(plugin);
    }

    @Override
    public void initialize() throws RuntimeException {
        super.initialize();

        this.jedisPool = establishJedisPool();
        new Thread(getSubscriber(), "UltimateReports_redis_subscriber").start();

        plugin.log(Level.INFO, "Initialized Redis connection pool");
    }

    @NotNull
    private Pool<Jedis> establishJedisPool() {
        final Settings.CrossServerSettings.RedisSettings settings = plugin.getSettings().getCrossServer().getRedis();
        final Set<String> sentinelNodes = Sets.newHashSet(settings.getSentinel().getNodes());
        final Pool<Jedis> pool;
        if (sentinelNodes.isEmpty()) {
            pool = new JedisPool(
                    new JedisPoolConfig(),
                    settings.getHost(),
                    settings.getPort(),
                    0,
                    settings.getPassword().isEmpty() ? null : settings.getPassword(),
                    settings.isUseSsl()
            );
            plugin.log(Level.INFO, "Using Redis pool");
        } else {
            pool = new JedisSentinelPool(
                    settings.getSentinel().getMasterName(),
                    sentinelNodes,
                    settings.getPassword().isEmpty() ? null : settings.getPassword(),
                    settings.getSentinel().getPassword().isEmpty() ? null : settings.getSentinel().getPassword()
            );
            plugin.log(Level.INFO, "Using Redis Sentinel pool");
        }
        return pool;
    }

    @NotNull
    private Runnable getSubscriber() {
        return () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(@NotNull String channel, @NotNull String encodedMessage) {
                        if (!channel.equals(getSubChannelId())) {
                            return;
                        }

                        final Message message = plugin.getMessageFromJson(encodedMessage);
                        if (message.getTargetType() == Message.TargetType.PLAYER) {
                            Bukkit.getOnlinePlayers().stream()
                                    .filter(online -> online.getName().equalsIgnoreCase(message.getTarget()))
                                    .findFirst()
                                    .ifPresent(receiver -> handle(receiver, message));
                            return;
                        }
                        handle(Bukkit.getOnlinePlayers().stream().findAny().orElse(null), message);
                    }
                }, getSubChannelId());
            }
        };
    }

    @Override
    protected void send(@NotNull Message message, @Nullable Player sender) {
        plugin.runAsync(task -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(getSubChannelId(), plugin.getGson().toJson(message));
            }
        });
    }

    @Override
    public void close() {
        super.close();
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

}
