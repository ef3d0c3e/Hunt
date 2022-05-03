package org.ef3d0c3e.hunt.generator;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.text.MessageFormat;
import java.util.List;


public class HuntBiomeProvider extends BiomeProvider
{
	@Override
	public Biome getBiome(WorldInfo worldInfo, int x, int y, int z)
	{
		if ((x/16) % 2 == 0)
			return Biome.FOREST;
		else
			return Biome.DESERT;
	}

	@Override
	public List<Biome> getBiomes(WorldInfo worldInfo)
	{
		return Lists.newArrayList(Biome.DESERT, Biome.FOREST);
	}
}
