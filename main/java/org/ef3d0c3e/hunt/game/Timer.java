package org.ef3d0c3e.hunt.game;

import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable
{
	private int m_seconds; // Total number of seconds
	
	public Timer()
	{
		m_seconds = 0;
	}
	
	@Override
	public void run()
	{
		if (Game.hasEnded())
			this.cancel();

		if (Game.isPaused())
			return;

		Game.run();
		++m_seconds;
	}
	
	// Get total time
	public int getTime()
	{
		return m_seconds;
	}
	
	// Get the seconds [0-59]
	public int getSeconds()
	{
		return m_seconds % 60;
	}

	// Get the minutes
	public int getMinutes()
	{
		return m_seconds / 60;
	}
}
