package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.events.HPlayerDeathEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.util.Iterator;
import java.util.Map;

import static org.bukkit.event.EventPriority.LOWEST;

/**
 * Lanczos's kit
 */
public class KitLanczos extends Kit
{
	@Override
	public String getName() { return "lanczos"; }
	@Override
	public String getDisplayName() { return "Lanczos"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return Items.createGuiItem(Material.CLOCK, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ A un ange gardient qui le",
			Kit.itemLoreColor + " sauve avant de mourir"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bVous avez un §6Ange Gardien§b qui vous sauve lorsque vous allez mourir.::§d● Il vous soigne légèrement et\n vous téléporte vers une\n position ancienne",
				"§c ╸ §bAprès avoir été utilisé, votre §6Ange Gardien§b à un délai de §e60s§b avant de pouvoir être utilisé à nouveau.",
				"§c ╸ §bUn indicateur apparaît, vous indiquant où vous serez téléporté si votre §6Ange Gardien§b s'active.::§d● Tous les joueurs\n peuvent le voir",
			}
		};

		return desc;
	}

	@Override
	public void changeOwner(final HuntPlayer prev, final HuntPlayer next)
	{
		if (indicator != null)
			indicator.remove();
		indicator = null;
		save = null;
		lastUse = -60;
	}

	public KitLanczos() {}

	ArmorStand indicator = null;
	Location save = null;
	int lastUse = -60;

	public static class Events implements Listener
	{
		/**
		 * Hooks to be called before a player dies
		 */
		@EventHandler(ignoreCancelled = true, priority = LOWEST)
		public void onDeath(final HPlayerDeathEvent ev)
		{
			final HuntPlayer hp = ev.getVictim();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitLanczos))
				return;
			final KitLanczos kit = (KitLanczos)hp.getKit();
			if (kit.save == null)
				return;
			if (Game.getTime() - kit.lastUse < 60)
			{
				hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cVotre Ange Gardien n'avait pas récupéré!"));
				return;
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

			ev.setCancelled(true);
		}

		/**
		 * Moves guardian angel location & spawn indicator periodically
		 */
		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					HuntPlayer.forEach(hp -> {
						if (!hp.isOnline() || !hp.isAlive())
							return;
						if (hp.getKit() == null || !(hp.getKit() instanceof KitLanczos))
							return;
						final KitLanczos kit = (KitLanczos)hp.getKit();


						if (hp.getPlayer().getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR ||
							hp.getPlayer().getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.LAVA)
							return;

						kit.save = hp.getPlayer().getLocation();
						if (kit.indicator == null || kit.indicator.isDead()) // Spawn
						{
							kit.indicator = (ArmorStand)kit.save.getWorld().spawnEntity(kit.save, EntityType.ARMOR_STAND);
							kit.indicator.getEquipment().setHelmet(Items.getSkull(hp));
							kit.indicator.setMarker(true);
							kit.indicator.setInvisible(true);
							kit.indicator.setSilent(true);
							kit.indicator.setGravity(false);
							kit.indicator.setInvulnerable(true);
							kit.indicator.setSmall(true);
						}

						kit.indicator.teleport(kit.save);
					});
				}
			}.runTaskTimer(Hunt.plugin, 0, 20*16);
		}
	}
}
