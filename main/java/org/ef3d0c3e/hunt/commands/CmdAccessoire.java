package org.ef3d0c3e.hunt.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Messager;

import javax.swing.*;

public class CmdAccessoire
{

	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Messager.ErrorMessage(sender, "Seul un joueur peut effectuer cette commande!");
			return true;
		}

		//Player p = (Player)sender;
		//ArmorStand ar = (ArmorStand)p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
		//ar.setInvulnerable(true);
		//ar.setMarker(true);
		//ar.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		//p.addPassenger(ar);

		return true;
	}
}
