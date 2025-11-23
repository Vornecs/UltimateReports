package dev.xf3d3.ultimatereports.database;

import com.google.gson.JsonSyntaxException;
import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Position;
import dev.xf3d3.ultimatereports.models.Report;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteDatabase extends Database {

    /**
     * The name of the database file
     */
    private static final String DATABASE_FILE_NAME  = "UltimateReportsData.db";

    /**
     * Path to the SQLite HuskTownsData.db file
     */
    private final File databaseFile;

    /**
     * The persistent SQLite database connection
     */
    private Connection connection;

    public SQLiteDatabase(@NotNull UltimateReports plugin) {
        super(plugin);
        this.databaseFile = new File(plugin.getDataFolder(), DATABASE_FILE_NAME);
    }

    /**
     *
     * @return The {@link Connection} to the database
     * @throws SQLException if the connection fails for some reason
     */
    private Connection getConnection() throws SQLException {
        if (connection == null) {
            setConnection();
        } else if (connection.isClosed()) {
            setConnection();
        }
        return connection;
    }


    /**
     * Used to set up a connection from the provided data
     */
    private void setConnection() {
        try {
            //plugin.log(Level.INFO, "Attempting to connect to database");

            // Ensure that the database file exists
            if (databaseFile.createNewFile()) {
                plugin.log(Level.INFO, "Created the SQLite database file");
            }

            // Specify use of the JDBC SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Set SQLite database properties
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);

            // Establish the connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath(), config.toProperties());

            //plugin.log(Level.INFO, "SQLite Database Connected!");
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "An exception occurred creating the database file", e);

        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "An SQL exception occurred initializing the SQLite database", e);

        } catch (ClassNotFoundException e) {
            plugin.log(Level.SEVERE, "Failed to load the necessary SQLite driver", e);
        }
    }



    public void initialize() {
        // Establish connection
        this.setConnection();

        // Create tables
        try (Statement statement = getConnection().createStatement()) {
            for (String tableCreationStatement : getSchema("database/sqlite_schema.sql")) {
                statement.execute(tableCreationStatement);
            }

            setLoaded(true);
        } catch (SQLException | IOException e) {
            setLoaded(false);

            throw new IllegalStateException("Failed to create SQLite database tables.", e);
        }

        plugin.getLogger().info("Database tables created");
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
                    final String data = new String(resultSet.getBytes("data"), StandardCharsets.UTF_8);
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

                statement.setBytes(1, plugin.getGson().toJson(report).getBytes(StandardCharsets.UTF_8));
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
                    final String data = new String(resultSet.getBytes("data"), StandardCharsets.UTF_8);
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

                statement.setBytes(1, plugin.getGson().toJson(report).getBytes(StandardCharsets.UTF_8));
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
                statement.setBytes(3, plugin.getGson().toJson(onlinePlayer.getPreferences()).getBytes(StandardCharsets.UTF_8));

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
                statement.setBytes(2, plugin.getGson().toJson(onlinePlayer.getPreferences()).getBytes(StandardCharsets.UTF_8));
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
                            plugin.getPreferencesFromJson(new String(resultSet.getBytes("preferences"), StandardCharsets.UTF_8))
                    );

                    return Optional.of(onlinePlayer);
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get player from table", e);
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to close connection", e);
        }
    }
}