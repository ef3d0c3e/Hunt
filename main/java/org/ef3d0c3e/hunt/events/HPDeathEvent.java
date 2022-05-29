package org.ef3d0c3e.hunt.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class HPDeathEvent extends Event
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
	private HuntPlayer victim;
	@Getter
	private HuntPlayer playerKiller;
	@Getter
	private LivingEntity killer;

	public HPDeathEvent(final HuntPlayer victim, final HuntPlayer playerKiller)
	{
		this.victim = victim;
		this.playerKiller = playerKiller;
	}

	public HPDeathEvent(final HuntPlayer victim, final LivingEntity killer)
	{
		this.victim = victim;
		this.killer = killer;
	}
}
