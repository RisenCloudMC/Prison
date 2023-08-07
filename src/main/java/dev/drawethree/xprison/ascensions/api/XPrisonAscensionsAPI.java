package dev.drawethree.xprison.ascensions.api;


import dev.drawethree.xprison.ascensions.model.Ascension;
import org.bukkit.entity.Player;

public interface XPrisonAscensionsAPI {

	/**
	 * Method to get player Ascension
	 *
	 * @param p Player
	 * @return Ascension
	 */
	Ascension getPlayerAscension(Player p);

	/**
	 * Sets a ascension to online player
	 *
	 * @param player   Player
	 * @param ascension Ascension
	 */
	void setPlayerAscension(Player player, Ascension ascension);

	/**
	 * Sets a ascension to online player
	 *
	 * @param player   Player
	 * @param ascension Ascension
	 */
	void setPlayerAscension(Player player, long ascension);

}
