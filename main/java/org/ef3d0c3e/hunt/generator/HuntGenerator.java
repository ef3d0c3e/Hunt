package org.ef3d0c3e.hunt.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.List;
import java.util.Random;

public class HuntGenerator extends ChunkGenerator
{
	@Override
	public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData)
	{
		SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(worldInfo.getSeed()), 6);
		generator.setScale(0.008);
		int worldX = chunkX * 16;
		int worldZ = chunkZ * 16;
		for (int x = 0; x < 16; x++)
		{
			for (int z = 0; z < 16; z++)
			{
				// normalised noise value between -1 and 1
				double noise = generator.noise(worldX + x, worldZ + z, 1, 1, true);
				// get a height between -40 and 40
				int height = (int) (noise * 20);
				// move height range up to between 24 and 104
				height += 64;
				if (height > chunkData.getMaxHeight())
					height = chunkData.getMaxHeight();
				// set blocks to stone with a top layer of sand
				for (int y = chunkData.getMinHeight(); y < height; y++)
				{
					if (y <= height - 12)
						chunkData.setBlock(x, y, z, Material.STONE);
					else
						chunkData.setBlock(x, y, z, Material.SAND);
				}
			}
		}
	}
	/**
	 * Generates an empty world!
	 */
	@Override
	public boolean shouldGenerateNoise()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateSurface()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateBedrock()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateCaves()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateDecorations()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateMobs()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateStructures()
	{
		return true;
	}

	/**
	 * Sets the entire world to the DESERT biome
	 *
	 * @param worldInfo the information about this world
	 * @return the Hunt biome provider
	 */
	@Override
	public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo)
	{
		return new HuntBiomeProvider();
	}

	//@Override
	//public List<BlockPopulator> getDefaultPopulators(World world) {
	//	List<BlockPopulator> populators = super.getDefaultPopulators(world);
	//	populators.add(new TARDISTreeBlockPopulator(CustomTree.TARDISTree.SKARO, 4));
	//	populators.add(new SkaroStructurePopulator(plugin));
	//	return populators;
	//}
}
