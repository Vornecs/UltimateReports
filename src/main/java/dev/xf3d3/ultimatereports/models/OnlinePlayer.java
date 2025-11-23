package dev.xf3d3.ultimatereports.models;

import dev.xf3d3.ultimatereports.UltimateReports;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class OnlinePlayer {
    @Getter @Setter private UUID uuid;
    @Getter @Setter private String lastPlayerName;
    @Getter @Setter private Preferences preferences;

    public OnlinePlayer(@NotNull UUID UUID, @NotNull String playerName, @Nullable Preferences preferences) {
        this.uuid = UUID;
        this.lastPlayerName = playerName;
        this.preferences = preferences != null ? preferences : Preferences.getDefaults();
    }

    public long addCoolDown(@NotNull UltimateReports plugin, long minutes) {
        long now = System.currentTimeMillis();
        long currentCooldown = this.getPreferences().getCooldown();
        long durationToAdd = TimeUnit.MINUTES.toMillis(minutes);

        long newExpirationTime;
        if (currentCooldown > now) {

            newExpirationTime = currentCooldown + durationToAdd;
        } else {

            newExpirationTime = now + durationToAdd;
        }

        this.getPreferences().setCooldown(newExpirationTime);
        plugin.getUsersManager().updatePlayer(this);

        return newExpirationTime;
    }

    public boolean isOnCooldown() {
        return this.getPreferences().getCooldown() > System.currentTimeMillis();
    }

    public long getRemainingTime() {
        long now = System.currentTimeMillis();
        long cooldown = this.getPreferences().getCooldown();

        if (cooldown <= now) return 0;

        return cooldown - now;
    }
}
