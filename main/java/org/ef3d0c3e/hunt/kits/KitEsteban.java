package org.ef3d0c3e.hunt.kits;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

/**
 * Esteban's kit
 */
public class KitEsteban extends Kit {
	static ItemStack tornadoItem;
	static ItemStack potionItem;
	static ItemStack splashPotionItem;

	int tornadoCount; // Keep track of how many tornadoes are active

	@Override
	public String getName() { return "esteban"; }
	@Override
	public String getDisplayName() { return "Estéban"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.DIRT, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Place de la Dirt à l'infini",
			Kit.itemLoreColor + "╸ Récupère de la nourriture en cassant de la terre",
			Kit.itemLoreColor + "╸ Tire de la Dirt en la droppant",
			Kit.itemLoreColor + "╸ Peut crafter des potions de 'Luck'",
			Kit.itemLoreColor + " qui lui donnent un effet positif et",
			Kit.itemLoreColor + " négatif aux autres",
			Kit.itemLoreColor + "╸ Peut crafter une tornade"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bPeut tirer des blocs de terre en droppanet de la terre.",
				"§c ╸ §bQuand il place de la terre, elle reste dans son inventaire.",
				"§c ╸ §bCasser de la terre lui redonne un peu de nourriture.",
				"§c ╸ §bPeut crafter des potions de luck §e(voir p2-3)§b qui lui donnent un effet positif et négatif à ses adversaires.",
				"§c ╸ §bPeut crafter une tornade de terre §e(voir p4)§b qui lui permet de se déplacer, de creuser ou de repousser les joueurs."
			},
			{
				"§b§nCraft de la potion de luck:",
				"",
				" §8[ §dSSS §8]",
				" §8[ §dSWS §8]",
				" §8[ §dSSS §8]",
				"§c ╸ §dS§b = Sable des âmes",
				"§c ╸ §dW§b = Bouteille d'eau"
			},
			{
				"§b§nCraft de la potion de luck splash:",
				"",
				" §8[ §7### §8]",
				" §8[ §dGL§7# §8]",
				" §8[ §7### §8]",
				"§c ╸ §dG§b = Poudre à canon",
				"§c ╸ §dL§b = Potion de Luck"
			},
			{
				"§b§nCraft de la tornade de terre:",
				"",
				" §8[ §dDFD §8]",
				" §8[ §dD§7#§dD §8]",
				" §8[ §dDFD §8]",
				"§c ╸ §dD§b = Terre",
				"§c ╸ §dF§b = Lingot de fer",
				"§c ╸ §b§aClick droit§b pour créer une tornade à 3 blocs de vous",
				"§c ╸ §b§aSneak-Click droit§b pour créer une tornade qui vous emporte",
				"§c ╸ §bLa tornade explose au contact d'un portail",
				"§c ╸ §bLa tornade allume les TNTs",
				"§c ╸ §bLorsqu'elle vous emporte, vous pouvez controller la tornade"
			}
		};
		return desc;
	}

	@Override
	public KitID getID() { return KitID.ESTEBAN; }

	/**
	 * Registers tornado & potions items & recipes & event listener
	 */
	@Override
	public void start()
	{
		// Register tornado recipe
		tornadoItem = new ItemStack(Material.HEART_OF_THE_SEA);
		{
			ItemMeta meta = tornadoItem.getItemMeta();
			meta.setDisplayName("§bTornade");
			meta.setLore(Arrays.asList(
				"§7Click droit pour lancer la tornade",
				"§7Sneak+Click droit pour lancer la tornade et voyager avec"
			));
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			tornadoItem.setItemMeta(meta);
			tornadoItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			tornadoItem.setAmount(2);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "esteban_tornado");
			ShapedRecipe recipe = new ShapedRecipe(key, tornadoItem);
			recipe.shape("DID", "D D", "DID");
			recipe.setIngredient('D', Material.DIRT);
			recipe.setIngredient('I', Material.IRON_INGOT);
			Bukkit.addRecipe(recipe);
		}

		// Register potion recipe
		// FIXME: Potion crafting accepts any potion (even for splash...)
		potionItem = new ItemStack(Material.POTION);
		{
			PotionMeta meta = (PotionMeta)potionItem.getItemMeta();
			meta.setDisplayName("§bPotion de Luck");
			meta.setLore(Arrays.asList(
				"§7Donne un effet positif aux joueurs d'Estéban",
				"§7Donne un effet négatif aux autre joueurs"
			));
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			meta.setColor(Color.fromRGB(0xCE537A));
			potionItem.setItemMeta(meta);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "esteban_potion");
			ShapedRecipe recipe = new ShapedRecipe(key, potionItem);
			recipe.shape("SSS", "SWS", "SSS");
			recipe.setIngredient('S', Material.SOUL_SAND);
			ItemStack waterBottle = new ItemStack(Material.POTION);
			{
				PotionMeta bottleMeta = (PotionMeta)waterBottle.getItemMeta();
				bottleMeta.setBasePotionData(new PotionData(PotionType.WATER));
				waterBottle.setItemMeta(bottleMeta);
			}
			recipe.setIngredient('W', new RecipeChoice.ExactChoice(waterBottle));
			Bukkit.addRecipe(recipe);
		}

		splashPotionItem = new ItemStack(Material.SPLASH_POTION);
		{
			PotionMeta meta = (PotionMeta)splashPotionItem.getItemMeta();
			meta.setDisplayName("§bPotion de Luck");
			meta.setLore(Arrays.asList(
				"§7Donne un effet positif aux joueurs d'Estéban",
				"§7Donne un effet négatif aux autre joueurs"
			));
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			meta.setColor(Color.fromRGB(0xCE537A));
			splashPotionItem.setItemMeta(meta);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "esteban_splash_potion");
			ShapelessRecipe recipe = new ShapelessRecipe(key, splashPotionItem);
			recipe.addIngredient(Material.GUNPOWDER);
			recipe.addIngredient(new RecipeChoice.ExactChoice(potionItem));
			Bukkit.addRecipe(recipe);
		}

		Bukkit.getServer().getPluginManager().registerEvents(new KitEstebanEvents(), Game.getPlugin());
	}
	/**
	 * Sends recipes to player & Awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		PlayerInteractions.schedule(hp, (o) -> {
			hp.getPlayer().discoverRecipes(Arrays.asList(
				new NamespacedKey(Game.getPlugin(), "esteban_tornado"),
				new NamespacedKey(Game.getPlugin(), "esteban_potion"),
				new NamespacedKey(Game.getPlugin(), "esteban_splash_potion")
			));
		});
		PlayerInteractions.giveItem(hp, new ItemStack[]{ tornadoItem }, true, true);
		HuntAchievement.PLAY_ESTEBAN.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_ESTEBAN.award(hp.getPlayer(), 1);
	}

	@Override
	public ItemStack[] getItems()
	{
		final ItemStack items[] = new ItemStack[]
		{
			tornadoItem, potionItem, splashPotionItem
		};
		return items;
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !Util.containsItem(getItems(), item);
	}

	@Override
	public Kit makeCopy()
	{
		return new KitEsteban();
	}
	
	public KitEsteban()
	{
		tornadoCount = 0;
	}
}
