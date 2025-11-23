package dev.xf3d3.ultimatereports.utils;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.xf3d3.ultimatereports.UltimateReports;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TaskRunner {
    UltimateReports plugin = UltimateReports.getPlugin();

    default void runAsync(@NotNull Consumer<WrappedTask> runnable) {
        getScheduler().runAsync(runnable);
    }

    default <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        runAsync(task -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }


    default void runLater(@NotNull Runnable runnable, long delay) {
        getScheduler().runLater(runnable, delay * 20);
    }

    default void runSync(@NotNull Consumer<WrappedTask> runnable) {
        getScheduler().runNextTick(runnable);
    }

    default void runSyncDelayed(@NotNull Runnable runnable, long delay) {
        getScheduler().runLater(runnable, delay);
    }

    default void runSyncRepeating(@NotNull Runnable runnable, long period) {
        getScheduler().runTimer(runnable, 0, period);
    }

    @NotNull
    PlatformScheduler getScheduler();



    @NotNull
    default Duration getDurationTicks(long ticks) {
        return Duration.of(ticks * 50, ChronoUnit.MILLIS);
    }
}
