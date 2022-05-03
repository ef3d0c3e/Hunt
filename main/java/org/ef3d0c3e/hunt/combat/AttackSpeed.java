package org.ef3d0c3e.hunt.combat;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class AttackSpeed
{
	public static void update(Player p)
	{
		AttributeInstance attr = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		attr.setBaseValue(24);
		p.saveData();
	}
}
