package dev.xf3d3.ultimatereports.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {
    @Getter @Setter
    @Expose @Builder.Default
    private long timestamp = System.currentTimeMillis();

    @Getter @Setter
    @Expose @Builder.Default
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    @Getter @Setter
    @Expose
    private UUID author;

    @Getter @Setter
    @Expose @SerializedName("author_name")
    private String authorName;

    @Getter @Setter
    @Expose
    private String message;

    @Getter @Setter
    @Expose @Builder.Default
    private MessageStatus status = MessageStatus.NOT_READ;

    @Getter @Setter @Nullable
    @Expose @SerializedName("discord_message_id")
    private String discordMessageId;

    @NotNull
    @ApiStatus.Internal
    public static Comment create(@NotNull Player author, @NotNull String message) {
        return Comment.builder()
                .author(author.getUniqueId())
                .authorName(author.getName())
                .message(message)
                .build();
    }

    @NotNull
    @ApiStatus.Internal
    public static Comment create(@NotNull OfflinePlayer author, @NotNull String message) {
        return Comment.builder()
                .author(author.getUniqueId())
                .authorName(author.getName())
                .message(message)
                .build();
    }


    public enum MessageStatus {
        READ,
        NOT_READ;

        public static Optional<MessageStatus> parse(@NotNull String string) {
            return Arrays.stream(values())
                    .filter(operation -> operation.name().equalsIgnoreCase(string))
                    .findFirst();
        }
    }
}
