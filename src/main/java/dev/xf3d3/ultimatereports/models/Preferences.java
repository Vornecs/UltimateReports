package dev.xf3d3.ultimatereports.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Preferences {
    @Setter
    @Getter
    @Expose
    @SerializedName("notification_status")
    private boolean notifications = true;

    @Setter
    @Getter
    @Expose
    private int reports = 0;

    @Setter
    @Getter
    @Expose
    private int reported = 0;

    @Setter
    @Getter
    @Expose
    private long cooldown = 0;

    @Setter
    @Getter
    @Expose
    private int rewardsToClaim = 0;

    @Expose
    @SerializedName("teleport_target")
    @Nullable
    private Position teleportTarget;


    @NotNull
    public static Preferences getDefaults() {
        return new Preferences(
                true
        );
    }

    private Preferences(boolean notifications) {
        this.notifications = notifications;
    }

    @SuppressWarnings("unused")
    private Preferences() {
    }

    public Optional<Position> getTeleportTarget() {
        return Optional.ofNullable(teleportTarget);
    }

    public void setTeleportTarget(@NotNull Position target) {
        this.teleportTarget = target;
    }

    public void clearTeleportTarget() {
        this.teleportTarget = null;
    }
}
