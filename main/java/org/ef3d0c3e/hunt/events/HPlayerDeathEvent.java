package org.ef3d0c3e.hunt.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class HPlayerDeathEvent extends Event implements Cancellable
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
	@Getter
	private boolean cancellable;
	@Getter @Setter
	private boolean cancelled = false;

	public HPlayerDeathEvent(final HuntPlayer victim, final HuntPlayer playerKiller, final boolean cancellable)
	{
		this.victim = victim;
		this.playerKiller = playerKiller;
		this.cancellable = cancellable;
	}

	public HPlayerDeathEvent(final HuntPlayer victim, final LivingEntity killer, final boolean cancellable)
	{
		this.victim = victim;
		this.killer = killer;
		this.cancellable = cancellable;
	}

}
