package dev.xf3d3.ultimatereports.database;

import dev.xf3d3.ultimatereports.UltimateReports;
import dev.xf3d3.ultimatereports.models.OnlinePlayer;
import dev.xf3d3.ultimatereports.models.Position;
import dev.xf3d3.ultimatereports.models.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class Database {
	protected final UltimateReports plugin;
	private boolean loaded;

	protected Database(@NotNull UltimateReports plugin) {
		this.plugin = plugin;
	}

	@NotNull
	protected final String[] getSchema(@NotNull String schemaFileName) throws IOException {
		return format(
				new String(Objects.requireNonNull(plugin.getResource(schemaFileName)).readAllBytes(),
						StandardCharsets.UTF_8))
				.split(";");
	}

	@NotNull
	protected final String format(@NotNull @Language("SQL") String statement) {
		final Pattern pattern = Pattern.compile("%(\\w+)%");
		final Matcher matcher = pattern.matcher(statement);
		final StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			final TableName tableName = TableName.match(matcher.group(1));
			matcher.appendReplacement(sb, plugin.getSettings().getDatabase().getTableName(tableName));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public abstract void initialize();
	public abstract List<Report> loadReports();
	public abstract Optional<Report> getReport(int id);
	public abstract Report createReport(@NotNull Player reporter, @NotNull OfflinePlayer reported, @NotNull String reason, @NotNull Position reporterPosition, @Nullable Position reportedPosition);
	public abstract void updateReport(@NotNull Report report);
	public abstract void deleteReport(int id);
	public abstract Optional<OnlinePlayer> getPlayer(@NotNull UUID uuid);
	public abstract void updatePlayer(@NotNull OnlinePlayer onlinePlayer);
	public abstract void createPlayer(@NotNull OnlinePlayer onlinePlayer);

	public abstract void close();

	/**
	 * Check if the database has been loaded
	 *
	 * @return {@code true} if the database has loaded successfully; {@code false} if it failed to initialize
	 */
	public boolean hasLoaded() {
		return loaded;
	}

	/**
	 * Set if the database has loaded
	 *
	 * @param loaded whether the database has loaded successfully
	 */
	protected void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	@Getter
	@AllArgsConstructor
	public enum Type {
		MYSQL("MySQL", "mysql"),
		MARIADB("MariaDB", "mariadb"),
		SQLITE("SQLite", "sqlite"),
		H2("H2", "h2"),
		POSTGRESQL("PostgreSQL", "postgresql");

		private final String displayName;
		private final String protocol;
	}

	/**
	 * Represents the names of tables in the database
	 */
	@Getter
	public enum TableName {
		REPORTS_DATA("ultimatereports_reports"),
		USER_DATA("ultimatereports_users"),;

		@NotNull
		private final String defaultName;

		TableName(@NotNull String defaultName) {
			this.defaultName = defaultName;
		}

		@NotNull
		public static Database.TableName match(@NotNull String placeholder) throws IllegalArgumentException {
			return TableName.valueOf(placeholder.toUpperCase());
		}

		@NotNull
		private Map.Entry<String, String> toEntry() {
			return Map.entry(name().toLowerCase(Locale.ENGLISH), defaultName);
		}

		@NotNull
		@SuppressWarnings("unchecked")
		public static Map<String, String> getDefaults() {
			return Map.ofEntries(Arrays.stream(values())
					.map(TableName::toEntry)
					.toArray(Map.Entry[]::new));
		}

	}
}