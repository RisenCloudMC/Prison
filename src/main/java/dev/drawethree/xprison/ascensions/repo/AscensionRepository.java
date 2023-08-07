package dev.drawethree.xprison.ascensions.repo;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface AscensionRepository {

	void updateAscension(OfflinePlayer player, long ascension);

	void addIntoAscensions(OfflinePlayer player);

	long getPlayerAscension(OfflinePlayer player);

	Map<UUID, Long> getTopAscensions(int amountOfRecords);

	void createTables();

	void clearTableData();
}
