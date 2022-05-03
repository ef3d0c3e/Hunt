package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Enzo's kit
 */
public class KitEnzo extends Kit
{
	static ItemStack keyItem;

	@Override
	public String getName() { return "enzo"; }
	@Override
	public String getDisplayName() { return "Enzo"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.GOLDEN_APPLE, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Fait tourner la roue de la fortune",
			Kit.itemLoreColor + " et obtient des items ou des effets"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bObtenez des clés pour la roue de la fortune toutes les 4 minutes.",
				"§c ╸ §bVous pouvez faire tourner la roue de la fortune pour obtenir des items ou des buffs.",
			},
		};
		return desc;
	}

	@Override
	public KitID getID() { return KitID.ENZO; }

	/**
	 * Registers key item
	 */
	@Override
	public void start()
	{
		keyItem = new ItemStack(Material.TRIPWIRE_HOOK, 2);
		{
			ItemMeta meta = keyItem.getItemMeta();
			meta.setDisplayName("§6Roue de la fortune");
			meta.setLore(Arrays.asList(
				"§7Click droit pour faire tourner",
				"§7la roue de la fortune"
			));
			keyItem.setItemMeta(meta);
		}
		items[0] = keyItem;

		Bukkit.getServer().getPluginManager().registerEvents(new KitEnzoEvents(), Game.getPlugin());
	}
	/**
	 * Awards achievement
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_ENZO.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievement
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_ENZO.award(hp.getPlayer(), 1);
	}

	static ItemStack[] items;
	static
	{
		ArrayList<ItemStack> list = new ArrayList<>();
		list.add(null); // Key item

		for (final EnzoReward r : getRewards())
		{
			if (!(r instanceof EnzoItemReward))
				continue;

			for (final ItemStack item : ((EnzoItemReward)r).items)
				list.add(item);
		}

		items = new ItemStack[list.size()];
		for (int i = 0; i < items.length; ++i)
			items[i] = list.get(i);
	}
	@Override
	public ItemStack[] getItems()
	{
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
		return new KitEnzo();
	}

	public KitEnzo()
	{
		pendingRewards = 1;
	}

	static public EnzoReward[] getRewards()
	{
		final EnzoReward diamond = new EnzoItemReward(HuntItems.createGuiItem(Material.DIAMOND, 0, "§bDiamant!"),
			new ItemStack[] { new ItemStack(Material.DIAMOND, 2) });
		final EnzoReward iron = new EnzoItemReward(HuntItems.createGuiItem(Material.IRON_INGOT, 0, "§bFer"),
			new ItemStack[] { new ItemStack(Material.IRON_INGOT, 6) });
		final EnzoReward ironTools = new EnzoItemReward(HuntItems.createGuiItem(Material.IRON_PICKAXE, 0, "§aOutils en Fer"),
			new ItemStack[] { new ItemStack(Material.IRON_PICKAXE, 1), new ItemStack(Material.IRON_SHOVEL, 1), new ItemStack(Material.IRON_AXE, 1) });
		final ItemStack fastPick = new ItemStack(Material.GOLDEN_PICKAXE, 1);
		{
			ItemMeta meta = fastPick.getItemMeta();
			meta.setDisplayName("§6Pioche Ultra-Rapide");
			meta.addEnchant(Enchantment.DIG_SPEED, 6, true);
			meta.addEnchant(Enchantment.DURABILITY, 3, true);
			fastPick.setItemMeta(meta);
		}
		final EnzoReward fastTool = new EnzoItemReward(HuntItems.createGuiItem(Material.GOLDEN_PICKAXE, 0, "§ePioche RAPIDE!!!"),
			new ItemStack[] { fastPick });
		final ItemStack luckPick = new ItemStack(Material.IRON_PICKAXE);
		{
			ItemMeta meta = luckPick.getItemMeta();
			meta.setDisplayName("§6Pioche Chanceuse");
			((Damageable)meta).setDamage(247);
			meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
			meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, true);
			luckPick.setItemMeta(meta);
		}
		final EnzoReward luckTool = new EnzoItemReward(HuntItems.createGuiItem(Material.GOLDEN_PICKAXE, 0, "§2Pioche CHANCEUSE!!!"),
			new ItemStack[] { luckPick });
		final EnzoReward tnt = new EnzoItemReward(HuntItems.createGuiItem(Material.TNT, 0, "§cExplosifs"),
			new ItemStack[] { new ItemStack(Material.TNT, 12) });
		final EnzoReward gapple = new EnzoItemReward(HuntItems.createGuiItem(Material.GOLDEN_APPLE, 0, "§ePommes d'Or"),
			new ItemStack[] { new ItemStack(Material.GOLDEN_APPLE, 4) });
		final ItemStack tridentItem = new ItemStack(Material.TRIDENT);
		{
			ItemMeta meta = tridentItem.getItemMeta();
			meta.setDisplayName("§3Trident de Cesàro");
			meta.addEnchant(Enchantment.LOYALTY, 3, true);
			meta.addEnchant(Enchantment.IMPALING, 3, true);
			tridentItem.setItemMeta(meta);
		}
		final EnzoReward trident = new EnzoItemReward(HuntItems.createGuiItem(Material.TRIDENT, 0, "§3Trident"),
			new ItemStack[] { tridentItem });
		final ItemStack kelianChestplateItem = new ItemStack(Material.LEATHER_CHESTPLATE);
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)kelianChestplateItem.getItemMeta();
			meta.setDisplayName("§cPlastron de Kélian");
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_DYE);
			((Damageable)meta).setDamage(70);
			meta.setColor(Color.RED);
			kelianChestplateItem.setItemMeta(meta);
		}
		final EnzoReward kelianChestplate = new EnzoItemReward(HuntItems.createGuiItem(Material.LEATHER_CHESTPLATE, 0, "§cPlastron de Kélian"),
			new ItemStack[] { kelianChestplateItem });
		final EnzoReward elytra = new EnzoItemReward(HuntItems.createGuiItem(Material.ELYTRA, 0, "§5Elytra"),
			new ItemStack[] { new ItemStack(Material.ELYTRA) });
		final EnzoReward exp = new EnzoItemReward(HuntItems.createGuiItem(Material.EXPERIENCE_BOTTLE, 0, "§eXP"),
			new ItemStack[] { new ItemStack(Material.EXPERIENCE_BOTTLE, 12) });
		final ItemStack crossbowAmmo = new ItemStack(Material.FIREWORK_ROCKET, 6);
		{
			FireworkMeta meta = (FireworkMeta)crossbowAmmo.getItemMeta();
			meta.setDisplayName("§6Munitions d'Arbalète");
			meta.setPower(1);
			meta.addEffects(FireworkEffect.builder().withColor(Color.BLACK).flicker(false).with(FireworkEffect.Type.BURST).build());
			meta.addEffects(FireworkEffect.builder().withColor(Color.GRAY).flicker(false).with(FireworkEffect.Type.BALL).build());
			crossbowAmmo.setItemMeta(meta);
		}
		final ItemStack crossbowItem = new ItemStack(Material.CROSSBOW);
		{
			CrossbowMeta meta = (CrossbowMeta)crossbowItem.getItemMeta();
			meta.setDisplayName("§6Super Arbalète");
			meta.addEnchant(Enchantment.QUICK_CHARGE, 2, true);
			meta.addChargedProjectile(crossbowAmmo);
			((Damageable)meta).setDamage(445);
			crossbowItem.setItemMeta(meta);
		}
		final EnzoReward crossbow = new EnzoItemReward(HuntItems.createGuiItem(Material.CROSSBOW, 0, "§6Super Arbalète"),
			new ItemStack[] { crossbowAmmo, crossbowItem });
		final EnzoReward obsidian = new EnzoItemReward(HuntItems.createGuiItem(Material.OBSIDIAN, 0, "§5Obsidian"),
			new ItemStack[] { new ItemStack(Material.OBSIDIAN, 24) });
		final EnzoReward carrot = new EnzoItemReward(HuntItems.createGuiItem(Material.GOLDEN_CARROT, 0, "§eCarottes Dorées"),
			new ItemStack[] { new ItemStack(Material.GOLDEN_CARROT, 12) });
		final EnzoReward cookedBeef = new EnzoItemReward(HuntItems.createGuiItem(Material.COOKED_BEEF, 0, "§cBoeuf"),
			new ItemStack[] { new ItemStack(Material.COOKED_BEEF, 16) });
		final EnzoReward bones = new EnzoItemReward(HuntItems.createGuiItem(Material.BONE, 0, "§7Loups"),
			new ItemStack[] { new ItemStack(Material.BONE, 16), new ItemStack(Material.WOLF_SPAWN_EGG, 3) });

		final EnzoReward speed1Effect = new EnzoEffectReward(HuntItems.createGuiItem(Material.SUGAR, 0, "§bVitesse"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.SPEED, 20*60*3, 0) });
		final EnzoReward speed2Effect = new EnzoEffectReward(HuntItems.createGuiItem(Material.SUGAR, 0, "§bSuper Vitesse"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.SPEED, 20*60, 1) });
		final EnzoReward jumpEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.RABBIT_FOOT, 0, "§dJump Boost"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.JUMP, 20*60*2, 1) });
		final EnzoReward resistEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.SHIELD, 0, "§dResistance"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*60*1, 0) });
		final EnzoReward strengthEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.BLAZE_POWDER, 0, "§cStrength"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20*25, 0) });
		final EnzoReward invisibilityEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.GLASS, 0, "§7Invisibility"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.INVISIBILITY, 20*60*2, 0) });
		final EnzoReward slowFallingEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.FEATHER, 0, "§8Slow Falling"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.SLOW_FALLING, 20*60*8, 0) });
		final EnzoReward waterBreathingEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.PUFFERFISH, 0, "§1Water Breathing"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.WATER_BREATHING, 20*60*15, 0) });
		final EnzoReward fireResistanceEffect = new EnzoEffectReward(HuntItems.createGuiItem(Material.LAVA_BUCKET, 0, "§6Fire Resistance"),
			new PotionEffect[] { new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20*60*12, 0) });

		return new EnzoReward[]
		{
			diamond, iron, ironTools, fastTool, luckTool, tnt, gapple, trident, kelianChestplate, elytra, exp, crossbow, obsidian, carrot, cookedBeef,
			speed1Effect, speed2Effect, jumpEffect, resistEffect, strengthEffect, invisibilityEffect, slowFallingEffect, waterBreathingEffect, fireResistanceEffect
		};
	}

	int pendingRewards;
}

/**
 * Abstract reward class
 */
abstract class EnzoReward
{
	public ItemStack display;

	/**
	 * Gives reward to player
	 * @param hp Player to give reward to
	 */
	abstract public void get(HuntPlayer hp);
}

/**
 * Items-based reward
 */
class EnzoItemReward extends EnzoReward
{
	ItemStack[] items;

	/**
	 * Creates an items-based reward
	 * @param display Reward's display item
	 * @param items Reward's items
	 */
	public EnzoItemReward(ItemStack display, ItemStack[] items)
	{
		this.display = display;
		this.items = items;
	}

	/**
	 * Gives items to player (even if offline or full)
	 * @param hp Player to give items to
	 */
	@Override
	public void get(HuntPlayer hp)
	{
		PlayerInteractions.giveItem(hp, items, true, true);
	}
}

/**
 * Effects-based reward
 */
class EnzoEffectReward extends EnzoReward
{
	PotionEffect[] effects;

	/**
	 * Creates an effects-based reward
	 * @param display Reward's display item
	 * @param effects Reward's effects
	 */
	public EnzoEffectReward(ItemStack display, PotionEffect[] effects)
	{
		this.display = display;
		this.effects = effects;
	}

	/**
	 * Applies effects to player (even if offline)
	 * @param hp Player to apply effects to
	 */
	@Override
	public void get(HuntPlayer hp)
	{
		for (final PotionEffect effect : effects)
			PlayerInteractions.giveEffect(hp, effect, true);
	}
}