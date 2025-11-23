package dev.xf3d3.ultimatereports.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dialog {
    public static final String DIALOG_HEADER = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃  UltimateReports Dialog Config  ┃
        ┃      Developed by xF3d3         ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://modrinth.com/plugin/ultimate-reports
        ┗╸ Documentation: https://ultimatereports.gitbook.io/ultimatereports-docs/documentation/basics/markdown""";


    @Comment("Messages for the report Dialog")
    private ReportDialog report = new ReportDialog();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReportDialog {
        private String title = "[Please provide a reason for your report](dark_aqua)";


        private ReasonInput reason = new ReasonInput();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ReasonInput {
            private String title = "[Reason:](green)";

            @Comment("You can't use MineDown here")
            private String defaultText = "He killed my friend";

            private int maxLength = 60;

            @Comment("The width of the text area")
            private int width = 300;
        }

        private Buttons buttons = new Buttons();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Buttons {

            @Comment("Confirmation button")
            private Confirmation confirmation = new Confirmation();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Confirmation {

                private String name = "[Confirm](green)";

                private String hover = "[Click to confirm and submit the report.](green)";
            }

            @Comment("Discard button")
            private Discard discard = new Discard();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Discard {

                private String name = "[Discard](red)";

                private String hover = "[Click to discard the report.](red)";
            }
        }
    }


    @Comment("Messages for the Comments Dialog")
    private CommentsDialog comments = new CommentsDialog();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentsDialog {
        private String title = "[Add a comment to the report](dark_aqua) [#%ID%](gold)";


        private CommentInput comment = new CommentInput();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class CommentInput {
            private String title = "[Comment:](green)";

            @Comment("You can't use MineDown here")
            private String defaultText = "He also burned my house";

            private int maxLength = 60;

            @Comment("The width of the text area")
            private int width = 300;
        }

        private Buttons buttons = new Buttons();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Buttons {

            @Comment("Confirmation button")
            private Confirmation confirmation = new Confirmation();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Confirmation {

                private String name = "[Confirm](green)";

                private String hover = "[Click to confirm and submit the comment.](green)";
            }

            @Comment("Discard button")
            private Discard discard = new Discard();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Discard {

                private String name = "[Discard](red)";

                private String hover = "[Click to discard the comment.](red)";
            }
        }

    }
}
