package dev.drawethree.xprison.ascensions.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import dev.drawethree.xprison.ascensions.repo.AscensionRepository;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AscensionRepositoryImpl implements AscensionRepository {

    private static final String TABLE_NAME = "UltraPrison_Ascensions";
    private static final String ASCENSIONS_UUID_COLNAME = "UUID";
    private static final String ASCENSIONS_ASCENSION_COLNAME = "id_ascension";

    private final SQLDatabase database;

    public AscensionRepositoryImpl(SQLDatabase database) {
        this.database = database;
    }

    @Override
    public void updateAscension(OfflinePlayer player, long newAscension) {
        this.database.executeSql("UPDATE " + TABLE_NAME + " SET " + ASCENSIONS_ASCENSION_COLNAME + "=? WHERE " + ASCENSIONS_UUID_COLNAME + "=?", newAscension, player.getUniqueId().toString());
    }

    @Override
    public void addIntoAscensions(OfflinePlayer player) {
        String sql = this.database.getDatabaseType() == SQLDatabaseType.SQLITE ? "INSERT OR IGNORE INTO " + TABLE_NAME + "(UUID,id_ascension)  VALUES(?,?)" : "INSERT IGNORE INTO " + TABLE_NAME + "(UUID,id_ascension) VALUES(?,?)";
        this.database.executeSql(sql, player.getUniqueId().toString(), 0);
    }

    @Override
    public long getPlayerAscension(OfflinePlayer player) {
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT * FROM " + TABLE_NAME + " WHERE " + ASCENSIONS_UUID_COLNAME + "=?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getLong(ASCENSIONS_ASCENSION_COLNAME);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Map<UUID, Long> getTopAscensions(int amountOfRecords) {
        Map<UUID, Long> top10Ascension = new LinkedHashMap<>();
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT " + ASCENSIONS_UUID_COLNAME + "," + ASCENSIONS_ASCENSION_COLNAME + " FROM " + TABLE_NAME + " ORDER BY " + ASCENSIONS_ASCENSION_COLNAME + " DESC LIMIT " + amountOfRecords); ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                top10Ascension.put(UUID.fromString(set.getString(ASCENSIONS_UUID_COLNAME)), set.getLong(ASCENSIONS_ASCENSION_COLNAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top10Ascension;
    }

    @Override
    public void createTables() {
        this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_ascension bigint, primary key (UUID))");
    }

    @Override
    public void clearTableData() {
        this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME);
    }
}
