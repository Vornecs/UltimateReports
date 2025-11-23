package dev.xf3d3.ultimatereports.dialogs;

import de.themoep.minedown.adventure.MineDown;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.Report;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommentDialog {
    private final UltimateReports plugin;
    private final Player player;
    private final Report report;

    public CommentDialog(@NotNull UltimateReports plugin, @NotNull Player player, @NotNull Report report) {
        this.plugin = plugin;
        this.player = player;
        this.report = report;

        open();
    }

    private void open() {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(MineDown.parse(plugin.getDialogConfig().getComments().getTitle()
                                        .replace("%ID%", String.valueOf(report.getId()))
                                ))
                        .inputs(List.of(
                                DialogInput.text(
                                        "comment",
                                        plugin.getDialogConfig().getComments().getComment().getWidth(),
                                        MineDown.parse(plugin.getDialogConfig().getComments().getComment().getTitle()),
                                        true,
                                        plugin.getDialogConfig().getComments().getComment().getDefaultText(),
                                        plugin.getDialogConfig().getComments().getComment().getMaxLength(),
                                        null
                                )
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                MineDown.parse(plugin.getDialogConfig().getComments().getButtons().getConfirmation().getName()),
                                MineDown.parse(plugin.getDialogConfig().getComments().getButtons().getConfirmation().getHover()),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            String comment = view.getText("comment");

                                            if (comment == null) {
                                                return;
                                            }

                                            if (audience instanceof Player dialogPlayer) {
                                                // send confirmation
                                                plugin.getReportsManager().addComment(player, report, comment);
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(1) // Set the number of uses for this callback. Defaults to 1
                                                .lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                MineDown.parse(plugin.getDialogConfig().getComments().getButtons().getDiscard().getName()),
                                MineDown.parse(plugin.getDialogConfig().getComments().getButtons().getDiscard().getName()),
                                100,
                                null // If we set the action to null, it doesn't do anything and closes the dialog
                        )
                ))
        );

        player.showDialog(dialog);
    }

}
