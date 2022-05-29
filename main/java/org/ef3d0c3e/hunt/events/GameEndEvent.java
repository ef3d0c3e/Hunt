package org.ef3d0c3e.hunt.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.teams.Team;

import java.util.List;


public class GameEndEvent extends Event
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
	private List<HuntPlayer> playerWinners;
	@Getter
	private List<Team> teamWinners;

	/**
	 * Constructor
	 * @param playerWinners Player(s) that won
	 * @param teamWinners Team(s) that won (if any)
	 */
	public GameEndEvent(final List<HuntPlayer> playerWinners, final List<Team> teamWinners)
	{
		this.playerWinners = playerWinners;
		this.teamWinners = teamWinners;
	}
}
