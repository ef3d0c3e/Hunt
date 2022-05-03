package org.ef3d0c3e.hunt.items;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.skins.Skin;

public class HuntItems
{
	private static ItemStack m_prevArrow;
	private static ItemStack m_nextArrow;

	private static ItemStack m_skinSelector;
	private static ItemStack m_accessorySelector;
	private static ItemStack m_kitSelector;
	private static ItemStack m_teamSelector;
	private static ItemStack m_statsMenu;

	private static ItemStack m_tracker;
	private static ItemStack m_zombieTracker;

	/**
	 * Init hook
	 * Initializes custom items
	 */
	public static void init()
	{
		m_prevArrow = createHead(
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19",
			"§bPrécédent"
		);
		m_nextArrow = createHead(
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=",
			"§bSuivant"
		);
		
		m_skinSelector = new ItemStack(Material.KNOWLEDGE_BOOK);
		{
			ItemMeta meta = m_skinSelector.getItemMeta();
			meta.setDisplayName("§aSkins");
			List<String> lore = new ArrayList<>();
			lore.add("§7Click droit pour choisir un skin");
			meta.setLore(lore);
			m_skinSelector.setItemMeta(meta);
		}
		m_accessorySelector = createHead(
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVjNmRjMmJiZjUxYzM2Y2ZjNzcxNDU4NWE2YTU2ODNlZjJiMTRkNDdkOGZmNzE0NjU0YTg5M2Y1ZGE2MjIifX19",
			"§aAccessoires",
			"§7Click droit pour ouvrir le",
			"§7menu des accessoires"
		);
		m_kitSelector = new ItemStack(Material.CLOCK);
		{
			ItemMeta meta = m_kitSelector.getItemMeta();
			meta.setDisplayName("§aKits");
			List<String> lore = new ArrayList<>();
			lore.add("§7Click droit pour choisir un kit");
			meta.setLore(lore);
			m_kitSelector.setItemMeta(meta);
		}
		m_teamSelector = new ItemStack(Material.WHITE_BANNER);
		{
			ItemMeta meta = m_teamSelector.getItemMeta();
			meta.setDisplayName("§aÉquipes");
			List<String> lore = new ArrayList<>();
			lore.add("§7Click droit pour choisir uné équipe");
			meta.setLore(lore);
			m_teamSelector.setItemMeta(meta);
		}
		
		m_tracker = new ItemStack(Material.COMPASS);
		{
			ItemMeta meta = m_tracker.getItemMeta();
			meta.setDisplayName("§eTraqueur");
			List<String> lore = new ArrayList<>();
			lore.add("§7Indique la direction de sa cible");
			lore.add("");
			lore.add("§8Clic-droit§7 pour actualiser la localisation");
			lore.add("§7Ceci consommera un des items suivants:");
			lore.add("§7 - §oRotten Flesh");
			lore.add("§7 - §oBone");
			lore.add("§7 - §oGunpowder");
			lore.add("§7 - §oSpider Eye");
			meta.setLore(lore);
			m_tracker.setItemMeta(meta);
		}

		m_zombieTracker = new ItemStack(Material.COMPASS);
		{
			ItemMeta meta = m_zombieTracker.getItemMeta();
			meta.setDisplayName("§2Traqueur Zombie");
			List<String> lore = new ArrayList<>();
			lore.add("§7Indique la direction du joueur en");
			lore.add("§7vie le plus proche");
			lore.add("");
			lore.add("§7Ce traqueur s'auto actualise");
			lore.add("§7toute les minutes");
			meta.setLore(lore);
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			m_zombieTracker.setItemMeta(meta);
		}

		m_statsMenu = new ItemStack(Material.BOOKSHELF);
		{
			ItemMeta meta = m_statsMenu.getItemMeta();
			meta.setDisplayName("§aStatistiques");
			List<String> lore = new ArrayList<>();
			lore.add("§7Click droit pour afficher vos statistiques");
			meta.setLore(lore);
			m_statsMenu.setItemMeta(meta);
		}
	}

	/**
	 * Create skull from texture
	 * @param texture The texture to use
	 * @param name Item's name
	 * @param lore Item's lore
	 * @return A skull with custom texture
	 */
	public static ItemStack createHead(final String texture, final String name, final String... lore)
	{
		final ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
		final SkullMeta meta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), "");
		profile.getProperties().put("textures", new Property("textures", texture));
		Field profileField = null;
		try
		{
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		}
		catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
		{
			e.printStackTrace();
		}
		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(lore));
		head.setItemMeta(meta);

		return head;
	}

	/**
	 * Create an item for inventory gui
	 * @param material Material for item
	 * @param modelData Custom model data to use (0 for none)
	 * @param name Item's name
	 * @param lore Item's lore
	 * @return An item for inventory gui
	 */
	public static ItemStack createGuiItem(final Material material, final int modelData, final String name,
			final String... lore)
	{
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(lore));
		meta.setCustomModelData(modelData);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Create an item for advancement display
	 * @param material Material for item
	 * @param modelData Custom model data to use (0 for none)
	 * @param glitter Whether to turn on enchantment glitter on the item
	 * @return An item for advancement display
	 */
	public static ItemStack createAdvancementItem(final Material material, final int modelData, boolean glitter)
	{
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(modelData);

		if (glitter)
		{
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			//meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES | ItemFlag.HIDE_ENCHANTS);
		}
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Gets player's skull
	 * @param hp HuntPlayer to get the skull of
	 * @return The skull of hp, matching custom skin (if any)
	 */
	public static ItemStack getSkull(final HuntPlayer hp)
	{
		if (hp.getSkin() != -1)
			return Skin.getList().get(hp.getSkin()).getHead();
		else
		{
			ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta)skull.getItemMeta();
			meta.setOwningPlayer(hp.getOfflinePlayer());
			skull.setItemMeta(meta);

			return skull;
		}
	}

	/**
	 * Get player's skull when dying
	 * @param hp The player to get skull of
	 * @return A skull with the player's skin
	 *
	 * @note Depending on the game mode (team or not) the skull will contain different informations
	 * @note Will match the player's custom skin
	 */
	public static ItemStack getDeathSkull(final HuntPlayer hp)
	{
		if (hp.getSkin() != -1)
			return Skin.getList().get(hp.getSkin()).getHead();
		else
		{
			ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta)skull.getItemMeta();
			meta.setOwningPlayer(hp.getOfflinePlayer());
			if (Game.isTeamMode())
			{
				meta.setDisplayName(MessageFormat.format("§eTête de {0}", hp.getTeamColoredName()));
				meta.setLore(Arrays.asList("",
					MessageFormat.format("§7Tête de {0}", hp.getTeamColoredName()),
					"§7Faites click-droit sur",
					"§7le beacon du spawn pour",
					"§7ressusciter un joueur",
					"§7de votre équipe."));
			}
			else
			{
				meta.setDisplayName(MessageFormat.format("§eTête de {0}", hp.getName()));
				meta.setLore(Arrays.asList("",
					MessageFormat.format("§7Tête de §e{0}", hp.getName()),
					"§7Faites click-droit pour",
					"§7afficher votre chasseur."));
			}
			skull.setItemMeta(meta);

			return skull;
		}
	}


	/**
	 * Gets the Previous Arrow skull
	 * @return The Previous Arrow skull
	 */
	public static ItemStack getPrevArrow()
	{
		return m_prevArrow;
	}

	/**
	 * Gets the Next Arrow skull
	 * @return The Next Arrow skull
	 */
	public static ItemStack getNextArrow()
	{
		return m_nextArrow;
	}

	/**
	 * Gets the skin selector item
	 * @return The skin selector item
	 */
	public static ItemStack getSkinSelector()
	{
		return m_skinSelector;
	}

	/**
	 * Gets the accessory selector item
	 * @return The accessory selector item
	 */
	public static ItemStack getAccessorySelector()
	{
		return m_accessorySelector;
	}

	/**
	 * Gets the kit selector item
	 * @return The kit selector item
	 */
	public static ItemStack getKitSelector()
	{
		return m_kitSelector;
	}

	/**
	 * Gets the team selector item
	 * @return The team selector item
	 */
	public static ItemStack getTeamSelector()
	{
		return m_teamSelector;
	}

	/**
	 * Gets the team selector item
	 * @return The team selector item
	 */
	public static ItemStack getStatsMenu()
	{
		return m_statsMenu;
	}

	/**
	 * Gets the Tracker
	 * @return The Tracker
	 */
	public static ItemStack getTracker()
	{
		return m_tracker;
	}

	/**
	 * Gets the Zombie Tracker
	 * @return The Zombie Tracker
	 */
	public static ItemStack getZombieTracker()
	{
		return m_zombieTracker;
	}
}
