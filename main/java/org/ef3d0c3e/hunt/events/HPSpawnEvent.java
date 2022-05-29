package org.ef3d0c3e.hunt.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ef3d0c3e.hunt.player.HuntPlayer;

@AllArgsConstructor
public class HPSpawnEvent extends Event
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
	private HuntPlayer player;

	@Getter
	private boolean revived;
}
