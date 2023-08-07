package dev.drawethree.xprison.ascensions.service;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface AscensionService {

	void setAscension(OfflinePlayer player, long ascension);

	void createAscension(OfflinePlayer player);

	long getPlayerAscension(OfflinePlayer player);

	Map<UUID, Long> getTopAscensions(int amountOfRecords);
}
