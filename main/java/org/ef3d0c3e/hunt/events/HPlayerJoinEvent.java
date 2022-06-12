package org.ef3d0c3e.hunt.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class HPlayerJoinEvent extends Event
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
	@Getter
	final boolean newPlayer;

	/**
	 * Constructor
	 * @param hp Player
	 * @param newPlayer Whether player is a new player or not (first time joining)
	 */
	public HPlayerJoinEvent(final HuntPlayer hp, final boolean newPlayer)
	{
		this.player = hp;
		this.newPlayer = newPlayer;
	}
}
