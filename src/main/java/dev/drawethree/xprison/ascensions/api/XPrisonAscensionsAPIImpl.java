package dev.drawethree.xprison.ascensions.api;

import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import dev.drawethree.xprison.ascensions.model.Ascension;
import org.bukkit.entity.Player;

public final class XPrisonAscensionsAPIImpl implements XPrisonAscensionsAPI {

	private final XPrisonAscensions plugin;

	public XPrisonAscensionsAPIImpl(XPrisonAscensions plugin) {
		this.plugin = plugin;
	}

	@Override
	public Ascension getPlayerAscension(Player p) {
		return plugin.getAscensionManager().getPlayerAscension(p);
	}

	@Override
	public void setPlayerAscension(Player player, Ascension ascension) {
		plugin.getAscensionManager().setPlayerAscension(null, player, ascension.getId());
	}

	@Override
	public void setPlayerAscension(Player player, long ascension) {
		plugin.getAscensionManager().setPlayerAscension(null, player, ascension);

	}
}
