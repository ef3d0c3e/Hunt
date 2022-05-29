package org.ef3d0c3e.hunt.stats;

import org.bukkit.Bukkit;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.io.*;

public class StatSaves
{
	static File dir;

	/**
	 * Creates required folders
	 */
	public static void init()
	{
		Bukkit.getConsoleSender().sendMessage("§f[§9Hunt§f] Creating stats directory...");

		dir = new File("plugins/Hunt/stats/");
		dir.mkdirs();
	}

	/**
	 * Saves stats associated with a player
	 * @param hp Player to save stats for
	 * @note Called when player leaves
	 */
	public static void save(final HuntPlayer hp)
	{
		// Trick is to only save currently used variables (useful if we later decide to rename/remove keys)
		try
		{
			FileWriter fw = new FileWriter(dir.getPath() + "/" + hp.getUUID());
			fw.write(StatsMenu.HUNT.serialize(hp));
			fw.write(StatsMenu.KITS.serialize(hp));
			fw.write(StatsMenu.MINING.serialize(hp));
			fw.close();
		}
		catch (IOException e)
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	/**
	 * Load stats associated with a player
	 * @param hp Player to save stats for
	 * @note Called when player leaves
	 */
	public static void load(final HuntPlayer hp)
	{
		StatsMenu.HUNT.init(hp);
		StatsMenu.KITS.init(hp);
		StatsMenu.MINING.init(hp);

		// Load from disk
		try
		{
			FileReader fr = new FileReader(dir.getPath() + "/" + hp.getUUID());
			String line;
			BufferedReader br = new BufferedReader(fr);
			while ((line = br.readLine()) != null)
			{
				final int pos = line.indexOf(':');
				final String key = line.substring(0, pos);
				final String value = line.substring(pos+1);

				StatValue sv = hp.getStat(key);
				sv.deserialize(value);
			}
			fr.close();
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}


	}
}
