package org.ef3d0c3e.hunt.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class HPlayerQuitEvent extends Event
{
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLERS_LIST;
	}
	@Override
	public HandlerList getHandlers()
	{
		return HANDLERS_LIST;
	}

	@Getter
	final HuntPlayer player;

	/**
	 * Constructor
	 * @param hp Player
	 */
	public HPlayerQuitEvent(final HuntPlayer hp)
	{
		this.player = hp;
	}
}
