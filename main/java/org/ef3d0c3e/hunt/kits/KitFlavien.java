package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
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

	@Override
	public KitID getID() { return KitID.FLAVIEN; }


	/**
	 * Registers elytra recipe, firework & event listener
	 */
	@Override
	public void start()
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

		Bukkit.getServer().getPluginManager().registerEvents(new KitFlavienEvents(), Game.getPlugin());
	}
	/**
	 * Registers recipes, allows flight & Awards achievements
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		hp.getPlayer().discoverRecipe(new NamespacedKey(Game.getPlugin(), "flavien_elytra"));
		hp.getPlayer().setAllowFlight(true);
		HuntAchievement.PLAY_FLAVIEN.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_FLAVIEN.award(hp.getPlayer(), 1);
	}
	/**
	 * Disables fly mode
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		hp.getPlayer().setAllowFlight(false);
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

	@Override
	public Kit makeCopy()
	{
		return new KitFlavien();
	}
	public KitFlavien() {}
}
