package dev.drawethree.xprison.ascensions.service.impl;

import dev.drawethree.xprison.ascensions.repo.AscensionRepository;
import dev.drawethree.xprison.ascensions.service.AscensionService;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public class AscensionServiceImpl implements AscensionService {

	private final AscensionRepository repository;

	public AscensionServiceImpl(AscensionRepository repository) {
		this.repository = repository;
	}

	@Override
	public void setAscension(OfflinePlayer player, long ascension) {
		repository.updateAscension(player, ascension);
	}

	@Override
	public void createAscension(OfflinePlayer player) {
		repository.addIntoAscensions(player);
	}

	@Override
	public long getPlayerAscension(OfflinePlayer player) {
		return repository.getPlayerAscension(player);
	}

	@Override
	public Map<UUID, Long> getTopAscensions(int amountOfRecords) {
		return repository.getTopAscensions(amountOfRecords);
	}
}
