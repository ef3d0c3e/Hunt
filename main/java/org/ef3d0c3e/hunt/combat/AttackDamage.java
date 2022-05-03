package org.ef3d0c3e.hunt.combat;

import org.bukkit.Material;

public class AttackDamage
{
	public static double getDamage(Material mat, double damage)
	{
		switch (mat)
		{
		case WOODEN_AXE:
		case STONE_AXE:
		case IRON_AXE:
		case GOLDEN_AXE:
		case DIAMOND_AXE:
		case NETHERITE_AXE:
			return damage / 2.0;
		default:
				return damage;
		}
	}
}
