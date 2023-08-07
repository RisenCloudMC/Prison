package dev.drawethree.xprison.ascensions.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import dev.drawethree.xprison.ascensions.model.Ascension;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class PlayerAscensionEvent extends XPrisonPlayerEvent implements Cancellable {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final Ascension oldAscension;

	@Getter
	@Setter
	private Ascension newAscension;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Called when player receive gems
	 *
	 * @param player      Player
	 * @param oldAscension old ascension
	 * @param newAscension new ascension
	 */
	public PlayerAscensionEvent(Player player, Ascension oldAscension, Ascension newAscension) {
		super(player);
		this.oldAscension = oldAscension;
		this.newAscension = newAscension;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
