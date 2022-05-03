package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class KitLanczosEvents implements Listener
{
	/**
	 * Hooks to be called before a player dies
	 * @param hp Player
	 * @return true If player must not die, false otherwise
	 */
	public static boolean KitLanczosPreDeathHook(HuntPlayer hp)
	{
		if (hp.getKit() == null || hp.getKit().getID() != KitID.LANCZOS)
			return false;
		final KitLanczos kit = (KitLanczos)hp.getKit();
		if (kit.save == null)
			return false;
		if (Game.getTime() - kit.lastUse < 60)
		{
			hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cVotre Ange Gardien n'avait pas récupéré!"));
			return false;
		}

		hp.getPlayer().getWorld().spawnParticle(Particle.REVERSE_PORTAL, hp.getPlayer().getLocation(), 80, 0.5, 0.5, 0.5);
		hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.ITEM_TOTEM_USE, 16.f, 1.f);
		hp.getPlayer().teleport(kit.save);
		hp.getPlayer().setFireTicks(0);
		hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 3));
		hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 10));
		hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 80, 0));
		hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 3));
		hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 16.f, 1.f);
		kit.lastUse = Game.getTime();

		return true;
	}

	/**
	 * Moves guardian angel location & spawn indicator periodically
	 */
	public KitLanczosEvents()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (HuntPlayer hp : Game.getPlayerList().values())
				{
					if (!hp.isOnline() || !hp.isAlive())
						continue;
					if (hp.getKit() == null || hp.getKit().getID() != KitID.LANCZOS)
						continue;
					final KitLanczos kit = (KitLanczos)hp.getKit();


					if (hp.getPlayer().getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR ||
						hp.getPlayer().getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.LAVA)
						continue;

					kit.save = hp.getPlayer().getLocation();
					if (kit.indicator == null || kit.indicator.isDead()) // Spawn
					{
						kit.indicator = (ArmorStand)kit.save.getWorld().spawnEntity(kit.save, EntityType.ARMOR_STAND);
						kit.indicator.getEquipment().setHelmet(HuntItems.getSkull(hp));
						kit.indicator.setMarker(true);
						kit.indicator.setInvisible(true);
						kit.indicator.setSilent(true);
						kit.indicator.setGravity(false);
						kit.indicator.setInvulnerable(true);
						kit.indicator.setSmall(true);
					}

					kit.indicator.teleport(kit.save);
				}
			}
		}.runTaskTimer(Game.getPlugin(), 0, 20*16);
	}
}
