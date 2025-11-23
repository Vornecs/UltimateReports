package dev.xf3d3.ultimatereports.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Discord {

    public static final String DISCORD_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ UltimateReports Discord Config ┃
        ┃      Developed by xF3d3        ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://modrinth.com/plugin/ultimate-reports
        ┗╸ Documentation: https://ultimatereports.gitbook.io/ultimatereports-docs/documentation/getting-started/discord-configuration""";


    @Comment({"The embed sent when a report is submitted. Same for both webhook and bot users",
            "When using the Bot this message is automatically updated even when a report is modified on Minecraft"})
    private ReportEmbed reportEmbed = new ReportEmbed();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReportEmbed {
        private String title = "New Report (#%ID%)";
        private int color = 5814783;


        private Author author = new Author();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Author {
            private String name = "UltimateReports";
            private String icon = "https://cdn.modrinth.com/data/O5OQXCl8/682b02d8c2f0d184c5ec8fb1f6fad369a1e07241.png";
        }

        private List<Field> fields = new ArrayList<>(List.of(
                new Field("Reporter", "`%REPORTER%`", true),
                new Field("Reported", "`%REPORTED%`", true),
                new Field("Comments", "`%COMMENTS_COUNT%`", true),
                new Field("Reason", "`%REASON%`", false)
        ));

        @Getter
        @Configuration
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Field {
            private String name;
            private String value;
            private boolean inline;
        }

        private Footer footer = new Footer();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Footer {
            private String text = "Sent by UltimateReports";
            private String icon = "https://cdn.modrinth.com/data/O5OQXCl8/682b02d8c2f0d184c5ec8fb1f6fad369a1e07241.png";
        }
    }


    @Comment({"The embed sent when a comment is added to a report. Same for both webhook and bot users",
            "When using the Bot this message is automatically updated when needed"})
    private CommentsEmbed commentsEmbed = new CommentsEmbed();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentsEmbed {
        private String title = "New Comment on Report (#%ID%)";
        private int color = 5814783;


        private Author author = new Author();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Author {
            private String name = "UltimateReports";
            private String icon = "https://cdn.modrinth.com/data/O5OQXCl8/682b02d8c2f0d184c5ec8fb1f6fad369a1e07241.png";
        }

        private List<Field> fields = new ArrayList<>(List.of(
                new Field("Author", "`%AUTHOR%`", true),
                new Field("Status", "`%STATUS%`", true),
                new Field("Comment", "`%COMMENT%`", false)
        ));

        @Getter
        @Configuration
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Field {
            private String name;
            private String value;
            private boolean inline;
        }

        private Footer footer = new Footer();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Footer {
            private String text = "Sent by UltimateReports";
            private String icon = "https://cdn.modrinth.com/data/O5OQXCl8/682b02d8c2f0d184c5ec8fb1f6fad369a1e07241.png";
        }
    }

    private Messages messages = new Messages();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Messages {

        private Commands commands = new Commands();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Commands {
            private String reportCommand = "Reports related commands";

            private String checkCommand = "Check a specific report by ID";
            private String checkCommandUsage = "The report's ID";

        }

        private Buttons buttons = new Buttons();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Buttons {

            private Button waiting = new Button("Set as Waiting", "\uD83D\uDFE0");
            private Button progress = new Button("Set as In Progress", "\uD83D\uDFE1");
            private Button done = new Button("Set as Done", "\uD83D\uDFE2");
            private Button archive = new Button("Archive", "\uD83D\uDCC2");
            private Button delete = new Button("Delete", "\uD83D\uDDD1\uFE0F");

            private Button add_comment = new Button("Add a Comment", "📃");
            private Button read = new Button("Mark as Read", "\uD83D\uDFE2");


            @Getter
            @Configuration
            @AllArgsConstructor(access = AccessLevel.PRIVATE)
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Button {
                private String label;
                private String emoji;
            }

        }

        private CommentModal commentModal = new CommentModal();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class CommentModal {
            private String nameLabel = "Your Minecraft name (case sensitive)";
            private String namePlaceholder = "Notch";

            private String commentLabel = "The comment";
            private String commentPlaceholder = "Ok, we will look into it";

            private String title = "Add a comment to #%ID%";
        }

        private ReplyMessages replyMessages = new ReplyMessages();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ReplyMessages {

            private String reportNotFound = "❌ Could not find the report **#%ID%**. Probably it has been deleted by another staffer.";
            private String commentNotFound = "❌ Could not find the comment. Probably it has been deleted by another staffer.";

            private String commentSetAsRead = "✅ The comment has been marked as **READ**";
            private String commentDeleted = "✅ The comment has been **DELETED**";

            private String reportMarkedAsWaiting = "✅ Report marked as **Waiting**.";
            private String reportMarkedAsInProgress = "🚧 Report marked as **In Progress**.";
            private String reportMarkedAsDone = "✅ Report marked as **Done**.";
            private String reportArchived = "📂 Report **Archived**.";
            private String reportDeleted = "🗑️ Report **Deleted**.";

            private String playerNotFound = "Player not found";
            private String commentAdded = "The comment has been added";

        }
    }



















}
