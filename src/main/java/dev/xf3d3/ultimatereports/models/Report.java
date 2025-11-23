package dev.xf3d3.ultimatereports.models;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Report {
    @Getter @Setter
    @Builder.Default
    private int id = 0;

    @Getter @Setter
    @Expose @Builder.Default
    private long timestamp = System.currentTimeMillis();

    @Getter @Setter
    @Expose @SerializedName("reporter_uuid")
    private UUID reporter;

    @Getter @Setter
    @Expose @SerializedName("reported_uuid")
    private UUID reported;

    @Getter @Setter
    @Expose @SerializedName("reporter_name")
    private String reporterName;

    @Getter @Setter
    @Expose @SerializedName("reported_name")
    private String reportedName;

    @Getter @Setter
    @Expose
    private String reason;

    @Getter @Setter
    @Expose @Builder.Default
    private Status status = Status.WAITING;

    @Getter @Setter
    @Expose
    private MarkedAs markedAs;

    @Getter @Setter
    @Expose @SerializedName("reporter_position")
    private Position reporterPosition;

    @Getter @Setter @Nullable
    @Expose @SerializedName("reported_position")
    private Position reportedPosition;

    @Getter @Setter @Nullable
    @Expose @SerializedName("discord_message_id")
    private String discordMessageId;

    @Getter @Setter
    @Expose @Builder.Default
    private List<Comment> comments = Lists.newCopyOnWriteArrayList();


    @NotNull
    @ApiStatus.Internal
    public static Report create(@NotNull Player reporter, @NotNull OfflinePlayer reported, @NotNull String reason, @NotNull Position reporterPosition, @Nullable Position reportedPosition) {
        return Report.builder()
                .reported(reported.getUniqueId())
                .reporter(reporter.getUniqueId())
                .reporterName(reporter.getName())
                .reportedName(reported.getName())
                .reason(reason)
                .reporterPosition(reporterPosition)
                .reportedPosition(reportedPosition)
                .build();
    }

    public void addOrUpdateComment(@NotNull Comment comment) {
        final int index = comments.indexOf(comment);

        if (index >= 0) {

            comments.set(index, comment);
        } else {

            comments.add(comment);
        }
    }

    public Optional<Comment> getComment(@NotNull UUID uuid) {
        return this.comments.stream()
                .filter(c -> c.getId().equals(uuid))
                .findFirst();
    }

    public void removeComment(@NotNull UUID commentId) {
        comments.removeIf(c -> c.getId().equals(commentId));
    }


    public enum Status {
        WAITING,
        IN_PROGRESS,
        DONE,
        ARCHIVED,
        OPEN; // internal use only. not correlated to report status.

        public static Optional<Status> parse(@NotNull String string) {
            return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(string))
                    .findFirst();
        }
    }

    public enum MarkedAs {
        TRUE,
        FALSE;

        public static Optional<MarkedAs> parse(@NotNull String string) {
            return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(string))
                    .findFirst();
        }
    }
}
