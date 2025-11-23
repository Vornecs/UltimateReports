package dev.xf3d3.ultimatereports.network;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public interface MessageHandler {

    // Handle inbound user list requests
    default void handleRequestUserList(@NotNull Message message, @Nullable Player receiver) {
        if (receiver == null) {
            return;
        }

        Message.builder()
                .type(Message.Type.UPDATE_USER_LIST)
                .payload(Payload.userList(Bukkit.getOnlinePlayers().stream().map(online -> User.of(online.getUniqueId(), online.getName())).toList()))
                .target(message.getSourceServer(), Message.TargetType.SERVER).build()
                .send(getBroker(), receiver);
    }

    // Handle inbound user list updates (returned from requests)
    default void handleUpdateUserList(@NotNull Message message) {
        message.getPayload().getUserList().ifPresent(
                (players) -> getPlugin().getUsersManager().setUserList(message.getSourceServer(), players)
        );
    }

    default void handleReportDelete(@NotNull Message message) {
        message.getPayload().getInteger()
                .flatMap(reportID -> getPlugin().getReportsManager().getReports().stream().filter(team -> team.getId() == reportID).findFirst())
                .ifPresent(report -> getPlugin().getReportsManager().getReports().remove(report));
    }

    default void handleReportUpdate(@NotNull Message message) {
        message.getPayload().getInteger()
                .ifPresent(id -> getPlugin().runAsync(task -> getPlugin().getDatabase().getReport(id)
                        .ifPresentOrElse(
                                report -> {
                                    getPlugin().getReportsManager().updateReportLocal(report, report.getId());
                                    getPlugin().getReportsManager().notifyReporter(report, getPlugin().getMessages().getReport().getGeneralUpdate());
                                },
                                () -> getPlugin().log(Level.WARNING, "Failed to update report: Report not found")
                        )));
    }

    default void handleReportCreate(@NotNull Message message) {
        message.getPayload().getInteger()
                .ifPresent(id -> getPlugin().runAsync(task -> getPlugin().getDatabase().getReport(id)
                        .ifPresentOrElse(
                                report -> {
                                    getPlugin().getReportsManager().updateReportLocal(report, report.getId());
                                    getPlugin().getReportsManager().notifyStaffers(report);
                                },
                                () -> getPlugin().log(Level.WARNING, "Failed to get created report: Report not found")
                        )));
    }



    @NotNull
    Broker getBroker();

    @NotNull
    UltimateReports getPlugin();

}
