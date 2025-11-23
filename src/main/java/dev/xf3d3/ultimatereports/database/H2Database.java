package dev.xf3d3.ultimatereports.database;

import com.google.gson.JsonSyntaxException;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Position;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("DuplicatedCode")
public class H2Database extends Database {

    /**
     * Path to the H2 HuskHomesData.h2 file.
     */
    private final File databaseFile;

    /**
     * The name of the database file.
     */
    private static final String DATABASE_FILE_NAME = "UltimateReportsData.h2";

    private JdbcConnectionPool connectionPool;

    public H2Database(@NotNull UltimateReports plugin) {
        super(plugin);
        this.databaseFile = new File(plugin.getDataFolder(), DATABASE_FILE_NAME);
    }

    /**
     * Fetch the auto-closeable connection from the H2 Connection Pool.
     *
     * @return The {@link Connection} to the H2 database
     * @throws SQLException if the connection fails for some reason
     */
    private Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    @Override
    public void initialize() throws IllegalStateException {
        // Prepare the database flat file
        final String url = String.format("jdbc:h2:%s", databaseFile.getAbsolutePath());
        this.connectionPool = JdbcConnectionPool.create(url, "sa", "sa");

        // Prepare database schema; make tables if they don't exist
        try (Connection connection = getConnection()) {
            final String[] databaseSchema = getSchema("database/h2_schema.sql");
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to initialize the H2 database", e);
        }
    }

    @Override
    public List<Report> loadReports() {
        final List<Report> reports = new ArrayList<>();

        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT `id`, `data`
                    FROM `%reports_data%`
                    """))) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final String data = resultSet.getString("data");
                    final Report report = plugin.getGson().fromJson(data, Report.class);

                    if (report != null) {
                        report.setId(resultSet.getInt("id"));
                        reports.add(report);
                    }
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to fetch list of reports from table", e);
        }

        return reports;
    }

    @Override
    public Report createReport(@NotNull Player reporter, @NotNull OfflinePlayer reported, @NotNull String reason, @NotNull Position reporterPosition, @Nullable Position reportedPosition) {
        final Report report = Report.create(reporter, reported, reason, reporterPosition, reportedPosition);

        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%reports_data%` (`data`)
                    VALUES (?)
                    """), Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, plugin.getGson().toJson(report));
                statement.executeUpdate();

                final ResultSet insertedRow = statement.getGeneratedKeys();
                if (insertedRow.next()) {
                    report.setId(insertedRow.getInt(1));
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to create report in table", e);
        }

        return report;
    }

    @Override
    public Optional<Report> getReport(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT *
                    FROM `%reports_data%`
                    WHERE `id` = ?
                    """))) {

                statement.setInt(1, id);

                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    final String data = resultSet.getString("data");
                    final Report report = plugin.getGson().fromJson(data, Report.class);

                    if (report != null) {
                        report.setId(resultSet.getInt("id"));
                        return Optional.of(report);
                    }
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            plugin.log(Level.SEVERE, "Failed to get report from table", e);
        }
        return Optional.empty();
    }

    @Override
    public void updateReport(@NotNull Report report) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%reports_data%`
                    SET `data` = ?
                    WHERE `id` = ?
                    """))) {

                statement.setString(1, plugin.getGson().toJson(report));
                statement.setInt(2, report.getId());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update report in table", e);
        }
    }

    @Override
    public void deleteReport(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    DELETE FROM `%reports_data%`
                    WHERE `id` = ?
                    """))) {

                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to delete report in table", e);
        }
    }

    @Override
    public void createPlayer(@NotNull OnlinePlayer onlinePlayer) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    INSERT INTO `%user_data%` (`uuid`, `username`, `preferences`)
                    VALUES (?, ?, ?)
                    """))) {

                statement.setString(1, String.valueOf(onlinePlayer.getUuid()));
                statement.setString(2, onlinePlayer.getLastPlayerName());
                statement.setString(3, plugin.getGson().toJson(onlinePlayer.getPreferences()));

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to create player in table", e);
        }
    }

    @Override
    public void updatePlayer(@NotNull OnlinePlayer onlinePlayer) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    UPDATE `%user_data%`
                    SET `username` = ?, `preferences` = ?
                    WHERE `uuid` = ?
                    """))) {

                statement.setString(1, onlinePlayer.getLastPlayerName());
                statement.setString(2, plugin.getGson().toJson(onlinePlayer.getPreferences()));
                statement.setString(3, String.valueOf(onlinePlayer.getUuid()));

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update player in table", e);
        }
    }

    @Override
    public Optional<OnlinePlayer> getPlayer(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(format("""
                    SELECT *
                    FROM `%user_data%`
                    WHERE `uuid` = ?
                    """))) {
                statement.setString(1, String.valueOf(uuid));

                final ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    final OnlinePlayer onlinePlayer = new OnlinePlayer(
                            UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("username"),
                            plugin.getPreferencesFromJson(resultSet.getString("preferences"))
                    );

                    return Optional.of(onlinePlayer);
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get player from table", e);
        }
        return Optional.empty();
    }


    public void close() {
        if (connectionPool != null) {
            connectionPool.dispose();
        }
    }



}

