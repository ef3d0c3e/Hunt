package org.ef3d0c3e.hunt.island;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Slime;
import org.bukkit.scheduler.BukkitRunnable;

public class IslandData
{
	public BukkitRunnable reel = null;
	public BukkitRunnable reelBack = null;
	public ArmorStand hook = null;
	public Arrow arrow = null;
	public Slime slime = null;

	public IslandData()
	{
	}

	public void reset()
	{
		if (reel != null)
			reel.cancel();
		if (reelBack != null)
			reelBack.cancel();
		if (arrow != null)
			arrow.remove();
		if (slime != null)
			slime.remove();
		if (hook != null)
			hook.remove();

		reel = reelBack = null;
		hook =  null;
		arrow =  null;
		slime =  null;
	}
}
