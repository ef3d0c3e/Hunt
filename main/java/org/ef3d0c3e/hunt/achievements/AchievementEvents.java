package org.ef3d0c3e.hunt.achievements;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class AchievementEvents implements Listener
{
	public AchievementEvents()
	{
		for (final HuntPlayer hp : Game.getPlayerList().values())
		{
			HuntAchievement.PLAY_1.award(hp.getPlayer(), 1);
			HuntAchievement.PLAY_5.award(hp.getPlayer(), 1);
			HuntAchievement.PLAY_10.award(hp.getPlayer(), 1);
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent ev)
	{
		if (ev.getBlock().getType() != Material.DIAMOND_ORE &&
			ev.getBlock().getType() != Material.DEEPSLATE_DIAMOND_ORE)
			return;

		// Play moulaga if first diamond mined this game
		if (HuntAchievement.DIAMOND_10_ONCE.get(ev.getPlayer()) == 0)
		{
			for (final Player p : Bukkit.getOnlinePlayers())
				p.playSound(p.getLocation(), "hunt.moulaga", SoundCategory.MASTER, 65566, 1.f);
		}

		HuntAchievement.DIAMOND_1.award(ev.getPlayer(), 1);
		HuntAchievement.DIAMOND_10.award(ev.getPlayer(), 1);
		HuntAchievement.DIAMOND_10_ONCE.award(ev.getPlayer(), 1);
	}
}
