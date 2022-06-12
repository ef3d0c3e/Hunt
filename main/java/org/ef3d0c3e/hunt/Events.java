package org.ef3d0c3e.hunt;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ef3d0c3e.hunt.events.HPlayerJoinEvent;
import org.ef3d0c3e.hunt.events.HPlayerQuitEvent;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class Events implements Listener
{
	@EventHandler
	public void onJoin(final PlayerJoinEvent ev)
	{
		ev.setJoinMessage(null);

		if (HuntPlayer.getPlayer(ev.getPlayer()) == null) // new player
		{
			Bukkit.getPluginManager().callEvent(new HPlayerJoinEvent(HuntPlayer.addPlayer(ev.getPlayer()), true));
		}
		else // Update player struct
		{
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			hp.setPlayer(ev.getPlayer());
			Bukkit.getPluginManager().callEvent(new HPlayerJoinEvent(hp, false));
		}
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent ev)
	{
		ev.setQuitMessage(null);

		Bukkit.getPluginManager().callEvent(new HPlayerQuitEvent(HuntPlayer.getPlayer(ev.getPlayer())));
	}
}
