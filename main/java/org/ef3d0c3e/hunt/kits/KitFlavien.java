package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.HPDeathEvent;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.util.Arrays;
import java.util.UUID;

/**
 * Flavien's kit
 */
public class KitFlavien extends Kit
{
	static ItemStack elytraItem;
	static ItemStack fireworkItem;

	@Override
	public String getName() { return "flavien"; }
	@Override
	public String getDisplayName() { return "Flavien"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.ELYTRA, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ A un double jump",
			Kit.itemLoreColor + "╸ Peut crafter des élytras",
			Kit.itemLoreColor + "╸ Les creepers lâchent des fireworks",
			Kit.itemLoreColor + "╸ Peut utiliser du sucre pour se doper"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bVous pouvez crafter des §6Elytra§b §e(voir p2)§b.::§d● Les élytras vous font\n vous déplacer plus\n vite au sol",
				"§c ╸ §bLes creepers ont une chance de vous donner des §6Fireworks§b pour vos §6Elytra§b.",
				"§c ╸ §bEn consommant du §6Sucre§b, vous obtenez §6Speed III§b pendant 5 secondes.::§d● Les effets de speed\n se stack en durée",
				"§c ╸ §bVous avez un double saut.",
			},
			{
				"§b§nCraft des Elytra:",
				"",
				" §8[ §dF§7#§dF §8]",
				" §8[ §dFLF §8]",
				" §8[ §dF§7#§dF §8]",
				"§c ╸ §dF§b = Plume",
				"§c ╸ §dP§b = Plastron en Cuit"
			}
		};

		return desc;
	}

	static
	{
		elytraItem = new ItemStack(Material.ELYTRA);
		{
			ItemMeta meta = elytraItem.getItemMeta();
			meta.setDisplayName("§5Elytra");
			meta.setLore(Arrays.asList(
				"",
				"§9+20% Vitesse à Pieds"
			));
			meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "GENERIC_MOVEMENT_SPEED", 0.20, AttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlot.CHEST));
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			elytraItem.setItemMeta(meta);
			elytraItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "flavien_elytra");
			ShapedRecipe recipe = new ShapedRecipe(key, elytraItem);
			recipe.shape("F F", "FLF", "F F");
			recipe.setIngredient('F', Material.FEATHER);
			recipe.setIngredient('L', Material.LEATHER_CHESTPLATE);
			Bukkit.addRecipe(recipe);
		}

		fireworkItem = new ItemStack(Material.FIREWORK_ROCKET);
		{
			FireworkMeta meta = (FireworkMeta)fireworkItem.getItemMeta();
			meta.setPower(2);
			meta.setDisplayName("§dFusée");
			fireworkItem.setItemMeta(meta);
		}
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]{ elytraItem, fireworkItem };
	}
	@Override
	public boolean itemFilter(final ItemStack item)
	{
		return !item.isSimilar(elytraItem) &&
			!item.isSimilar(fireworkItem);
	}

	public KitFlavien() {}

	public static class Events implements Listener
	{
		@EventHandler
		public void onSpawn(final HPSpawnEvent ev)
		{
			final HuntPlayer hp = ev.getPlayer();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien))
				return;

			hp.getPlayer().discoverRecipe(new NamespacedKey(Game.getPlugin(), "flavien_elytra"));
			hp.getPlayer().setAllowFlight(true);
		}

		@EventHandler
		public void onDeath(final HPDeathEvent ev)
		{
			final HuntPlayer hp = ev.getVictim();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien))
				return;

			hp.getPlayer().setAllowFlight(false);
		}

		/**
		 * Double jump
		 * @param ev Event
		 */
		@EventHandler
		public void onFly(PlayerToggleFlightEvent ev)
		{
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien))
				return;

			ev.setCancelled(true);
			hp.getPlayer().setAllowFlight(false);
			hp.getPlayer().setVelocity(hp.getPlayer().getVelocity().multiply(new Vector(2.0, 0.0, 2.0)).add(new Vector(0.0, 0.85, 0.0)));

			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!hp.getPlayer().isOnline())
						return;
					if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien)) // Player's kit may have changed
						return;

					hp.getPlayer().setAllowFlight(true);
				}
			}.runTaskLater(Game.getPlugin(), 40);
		}

		/**
		 * Allows flight for player when they connect
		 * @param ev Event
		 */
		@EventHandler
		public void onJoin(final PlayerJoinEvent ev)
		{
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (!hp.isAlive())
				return;
			if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien))
				return;

			hp.getPlayer().setAllowFlight(true);
		}

		/**
		 * Consuming sugar gives speed
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null)
				return;
			if (ev.getItem().getType() != Material.SUGAR)
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien))
				return;

			// Stop if clicking an inventory
			if (!hp.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
				Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
				return;

			ev.setCancelled(true);

			final PotionEffect current = hp.getPlayer().getPotionEffect(PotionEffectType.SPEED);
			int duration = 0;
			if (current != null)
			{
				if (current.getAmplifier() > 2)
					return;
				else if (current.getAmplifier() == 2)
					duration += current.getDuration();
			}

			ev.getItem().setAmount(ev.getItem().getAmount()-1);
			hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration + 100, 2));
		}

		/**
		 * Creepers drop firework when killed
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDeath(EntityDeathEvent ev)
		{
			if (!(ev.getEntity() instanceof Creeper))
				return;
			final HuntPlayer killer = Util.getPlayerKiller(ev.getEntity().getKiller());
			if (killer == null || killer.getKit() == null || !(killer.getKit() instanceof KitFlavien))
				return;

			if (Game.nextPosInt() % 2 == 0)
				ev.getDrops().add(KitFlavien.fireworkItem);
			if (Game.nextPosInt() % 4 == 0)
				ev.getDrops().add(KitFlavien.fireworkItem);
		}

		/**
		 * Player is immune to fall damage
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDamage(EntityDamageEvent ev)
		{
			if (!(ev.getEntity() instanceof Player))
				return;
			if (ev.getCause() != EntityDamageEvent.DamageCause.FALL)
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitFlavien))
				return;

			ev.setCancelled(true);
		}
	}
}
