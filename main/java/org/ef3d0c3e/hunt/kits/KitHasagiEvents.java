package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.nbt.TextComponentTagVisitor;
import org.bukkit.*;
import org.bukkit.block.data.type.Wall;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;

// TODO: Prevent sword dragging (and also prevent putting sword in item frame...)

/**
 * Hasagi's kit's events
 */
public class KitHasagiEvents implements Listener
{
	/**
	 * Prevents enchanting the blade
	 * @param ev Event
	 */
	@EventHandler
	public void onEnchantItem(EnchantItemEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getEnchanter().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.HASAGI)
			return;
		if (!Util.isSimilarBasic(ev.getItem(), KitHasagi.bladeItem))
			return;

		ev.setCancelled(true);
	}

	/**
	 * Adds stacks
	 * @param ev Event
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
	{
		if (!(ev.getDamager() instanceof Player))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getDamager().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.HASAGI)
			return;
		final KitHasagi kit = (KitHasagi)hp.getKit();
		if (kit.charge == 2)
			return;
		if (!Util.isSimilarBasic(hp.getPlayer().getInventory().getItemInMainHand(), KitHasagi.bladeItem))
			return;

		++kit.charge;
		hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), "hunt.hasagi.hasagi", SoundCategory.MASTER, 1.f, 1.f);
		if (kit.charge == 2)
			hp.getPlayer().getInventory().getItemInMainHand().addEnchantment(Enchantment.DURABILITY, 1);
	}

	/**
	 * Shoots tornado
	 * @param ev Event
	 */
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null)
			return;
		if (!Util.isSimilarBasic(KitHasagi.bladeItem, ev.getItem()))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.HASAGI)
			return;
		ev.setCancelled(true);
		final KitHasagi kit = (KitHasagi)hp.getKit();
		if (kit.charge != 2)
			return;

		kit.charge = 0;
		hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), "hunt.hasagi.aseryo", SoundCategory.MASTER, 1.f, 1.f);
		hp.getPlayer().getInventory().getItemInMainHand().removeEnchantment(Enchantment.DURABILITY);
	}

	/**
	 * Spawns wind wall that blocks projectiles
	 * @param ev Event
	 */
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.HASAGI)
			return;
		if (!Util.isSimilarBasic(ev.getItemDrop().getItemStack(), KitHasagi.bladeItem))
			return;
		final KitHasagi kit = (KitHasagi)hp.getKit();

		ev.setCancelled(true);

		if (Game.getTime() - kit.lastWall < 22)
		{
			hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
				new TextComponent(MessageFormat.format(
					"§bMur de vent {0}",
					Util.getLoadingBar("§a|", "§8|", 22, Game.getTime() - kit.lastWall)
					)
				)
			);
			return;
		}

		hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), "hunt.hasagi.face", SoundCategory.MASTER, 1.f, 1.f);
		kit.lastWall = Game.getTime()-20;

		final double height = 4.0;
		final double width = 5.0;

		final double x = hp.getPlayer().getLocation().getX();
		final double y = hp.getPlayer().getLocation().getY();
		final double z = hp.getPlayer().getLocation().getZ();

		Location noPitch = hp.getPlayer().getLocation();
		noPitch.setPitch(0.f);
		final Vector dir = noPitch.getDirection().normalize();
		final Vector pdir = dir.crossProduct(new Vector(0, 1.0, 0)); // pdir . dir == pdir . ey == dir . ey = 0

		final Vector pos = new Vector(x, y, z).add(new Vector(2.0, 0.0, 2.0).multiply(dir));
		final World world = hp.getPlayer().getWorld();

		 /*    .______________________________.
		      /|           width^            /|
		     / |                            / |
		    '______________________________/  |           y dir
			|  A___________________________|__D           ^ /\
			| /<1           pos     height>| /            | /
			|/               X             |/             |/
			B______________________________C   pdir <-----" (pdir = dir ^ y)*/
		final Vector p = new Vector(1.0, 0.0, 1.0);
		final Vector v = new Vector(width/2.0, 0.0, width/2.0);

		final Vector A = pdir.clone().multiply(v).add(dir.clone().multiply(p));
		final Vector B = pdir.clone().multiply(v).subtract(dir.clone().multiply(p));
		final Vector C = pdir.clone().multiply(v).multiply(-1.0).add(dir.clone().multiply(p));
		final Vector D = pdir.clone().multiply(v).multiply(-1.0).subtract(dir.clone().multiply(p));

		new BukkitRunnable()
		{
			int ticks = 0;

			@Override
			public void run()
			{
				if (Game.isPaused())
					this.cancel();

				for (int j = 0; j < height; ++j) // Vertical
				{
					for (int i = 0; i < width; ++i) // Horizontal
					{
						final Vector p = pos.clone()
							.add(new Vector(((double)i)-width/2.0, 0.0, ((double)i)-width/2.0).multiply(pdir))
							.add(new Vector(0.0, j, 0.0));

						world.spawnParticle(Particle.SWEEP_ATTACK, p.getX(), p.getY(), p.getZ(), 1, 0.0, 0.0, 0.0);
					}
				}

				// Particles
				++ticks;
				if (ticks == 180)
					this.cancel();
			}
		}.runTaskTimer(Game.getPlugin(), 0, 1);
	}
}
