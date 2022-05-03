package org.ef3d0c3e.hunt.achievements;

import java.text.MessageFormat;
import java.util.ArrayList;

import eu.endercentral.crazy_advancements.advancement.criteria.Criteria;
import eu.endercentral.crazy_advancements.save.SaveFile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;

import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay.AdvancementFrame;
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility;
import eu.endercentral.crazy_advancements.JSONMessage;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import net.md_5.bungee.api.chat.TextComponent;

public enum HuntAchievement
{
	HUNT("minecraft:textures/block/stone.png", HuntItems.createAdvancementItem(Material.GOLDEN_APPLE, 0, false), AdvancementFrame.TASK, 1, "Hunt", "Les achievements du Hunt!"),
	PLAY_1(HUNT, -1, 0, HuntItems.createAdvancementItem(Material.CLOCK, 0, false), AdvancementFrame.TASK, 1, "Bizuth", "Joue au moins 1 partie."),
	PLAY_5(PLAY_1, -2, 0, HuntItems.createAdvancementItem(Material.COMPASS, 0, false), AdvancementFrame.TASK, 5, "L1", "Habitué du hunt, joue 5 parties."),
	PLAY_10(PLAY_5, -3, 0, HuntItems.createAdvancementItem(Material.GOLD_BLOCK, 0, false), AdvancementFrame.CHALLENGE, 10, "L2", "Tu n'as pas raté ta L1, bravo! Joue au moins 10 parties."),

	DIAMOND_1(HUNT, -1, -1, HuntItems.createAdvancementItem(Material.DIAMOND, 0, false), AdvancementFrame.TASK, 1, "Moulaga!", "Obtient au moins une fois un diamant."),
	DIAMOND_10(DIAMOND_1, -2, -1, HuntItems.createAdvancementItem(Material.DIAMOND_CHESTPLATE, 0, false), AdvancementFrame.TASK, 10, "Diamants", "Obtient 10 diamants en tout."),
	DIAMOND_10_ONCE(DIAMOND_10, -4, -1, HuntItems.createAdvancementItem(Material.DIAMOND_BLOCK, 0, false), AdvancementFrame.TASK, 10, "X-Ray", "Obtient 10 diamants en une partie."),

	VAMPIRE_KILL(HUNT, 1, 2, HuntItems.createAdvancementItem(Material.SPECTRAL_ARROW, 0, false), AdvancementFrame.CHALLENGE, 1, "Tueur de vampire", "Tue Jean-Baptiste sans que sa malédiction ne te tue."),
	VAMPIRE_REVIVE(HUNT, 1, 3, HuntItems.createAdvancementItem(Material.WITHER_ROSE, 0, false), AdvancementFrame.GOAL, 1, "Dracula", "Résuscite après avoir été tué entant que Jean-Baptiste."),


	// Gets awarded when player starts or win game (in kit's hooks)
	PLAY_ESTEBAN(HUNT, 1, -2, HuntItems.createAdvancementItem(Material.DIRT, 0, false), AdvancementFrame.TASK, 1, "Estéban", "Joue une partie avec le kit Estéban."),
	WIN_ESTEBAN(PLAY_ESTEBAN, 2, -2, HuntItems.createAdvancementItem(Material.DIRT, 0, false), AdvancementFrame.CHALLENGE, 1, "Immunisé à l'alcool", "Gagne une partie avec le kit Estéban."),
	PLAY_MEHDI(HUNT, 1, -3, HuntItems.createAdvancementItem(Material.HONEYCOMB, 0, false), AdvancementFrame.TASK, 1, "Mehdi", "Joue une partie avec le kit Mehdi."),
	WIN_MEHDI(PLAY_MEHDI, 2, -3, HuntItems.createAdvancementItem(Material.HONEYCOMB, 0, false), AdvancementFrame.CHALLENGE, 1, "Maître du Karma", "Gagne une partie avec le kit Mehdi."),
	PLAY_JB(HUNT, 1, -4, HuntItems.createAdvancementItem(Material.WITHER_ROSE, 0, false), AdvancementFrame.TASK, 1, "Jean-Baptiste", "Joue une partie avec le kit Jean-Baptiste."),
	WIN_JB(PLAY_JB, 2, -4, HuntItems.createAdvancementItem(Material.WITHER_ROSE, 0, false), AdvancementFrame.CHALLENGE, 1, "Guérit de la covid", "Gagne une partie avec le kit Jean-Baptiste."),
	PLAY_BAPTISTE(HUNT, 1, -5, HuntItems.createAdvancementItem(Material.SPECTRAL_ARROW, 0, false), AdvancementFrame.TASK, 1, "Baptiste", "Joue une partie avec le kit Baptiste."),
	WIN_BAPTISTE(PLAY_BAPTISTE, 2, -5, HuntItems.createAdvancementItem(Material.SPECTRAL_ARROW, 0, false), AdvancementFrame.CHALLENGE, 1, "Majorant", "Gagne une partie avec le kit Baptiste."),
	PLAY_LINO(HUNT, 1, -6, HuntItems.createAdvancementItem(Material.CROSSBOW, 0, false), AdvancementFrame.TASK, 1, "Lino", "Joue une partie avec le kit Lino."),
	WIN_LINO(PLAY_LINO, 2, -6, HuntItems.createAdvancementItem(Material.CROSSBOW, 0, false), AdvancementFrame.CHALLENGE, 1, "FC Chômeur", "Gagne une partie avec le kit Lino."),
	PLAY_JULIEN(HUNT, 1, -7, HuntItems.createAdvancementItem(Material.COBBLESTONE, 0, false), AdvancementFrame.TASK, 1, "Julien", "Joue une partie avec le kit Julien."),
	WIN_JULIEN(PLAY_JULIEN, 2, -7, HuntItems.createAdvancementItem(Material.COBBLESTONE, 0, false), AdvancementFrame.CHALLENGE, 1, "גולם", "Gagne une partie avec le kit Julien."),
	PLAY_KELIAN(HUNT, 1, -8, HuntItems.createAdvancementItem(Material.HEART_OF_THE_SEA, 0, false), AdvancementFrame.TASK, 1, "Kélian", "Joue une partie avec le kit Kélian."),
	WIN_KELIAN(PLAY_KELIAN, 2, -8, HuntItems.createAdvancementItem(Material.HEART_OF_THE_SEA, 0, false), AdvancementFrame.CHALLENGE, 1, "Breton", "Gagne une partie avec le kit Kélian."),
	PLAY_THOMAS(HUNT, 1, -9, HuntItems.createAdvancementItem(Material.ENDER_EYE, 0, false), AdvancementFrame.TASK, 1, "Thomas", "Joue une partie avec le kit Thomas."),
	WIN_THOMAS(PLAY_THOMAS, 2, -9, HuntItems.createAdvancementItem(Material.ENDER_EYE, 0, false), AdvancementFrame.CHALLENGE, 1, "Maire de Levallois", "Gagne une partie avec le kit Thomas."),
	PLAY_ENZO(HUNT, 1, -10, HuntItems.createAdvancementItem(Material.GOLDEN_APPLE, 0, false), AdvancementFrame.TASK, 1, "Enzo", "Joue une partie avec le kit Enzo."),
	WIN_ENZO(PLAY_ENZO, 2, -10, HuntItems.createAdvancementItem(Material.GOLDEN_APPLE, 0, false), AdvancementFrame.CHALLENGE, 1, "La chance", "Gagne une partie avec le kit Enzo."),
	PLAY_TOM(HUNT, 1, -11, HuntItems.createAdvancementItem(Material.COMPASS, 0, false), AdvancementFrame.TASK, 1, "Tom", "Joue une partie avec le kit Tom."),
	WIN_TOM(PLAY_TOM, 2, -11, HuntItems.createAdvancementItem(Material.COMPASS, 0, false), AdvancementFrame.CHALLENGE, 1, "Chicha", "Gagne une partie avec le kit Tom."),
	PLAY_FLAVIEN(HUNT, 1, -12, HuntItems.createAdvancementItem(Material.ELYTRA, 0, false), AdvancementFrame.TASK, 1, "Flavien", "Joue une partie avec le kit Flavien."),
	WIN_FLAVIEN(PLAY_FLAVIEN, 2, -12, HuntItems.createAdvancementItem(Material.ELYTRA, 0, false), AdvancementFrame.CHALLENGE, 1, "Charro", "Gagne une partie avec le kit Flavien."),
	PLAY_BK(HUNT, 1, -13, HuntItems.createAdvancementItem(Material.SUGAR, 0, false), AdvancementFrame.TASK, 1, "BlueKatss", "Joue une partie avec le kit BlueKatss."),
	WIN_BK(PLAY_BK, 2, -13, HuntItems.createAdvancementItem(Material.SUGAR, 0, false), AdvancementFrame.CHALLENGE, 1, "Prend de la C", "Gagne une partie avec le kit BlueKatss."),
	PLAY_LANCZOS(HUNT, 1, -14, HuntItems.createAdvancementItem(Material.CLOCK, 0, false), AdvancementFrame.TASK, 1, "Lanczos", "Joue une partie avec le kit Lanczos."),
	WIN_LANCZOS(PLAY_LANCZOS, 2, -14, HuntItems.createAdvancementItem(Material.CLOCK, 0, false), AdvancementFrame.CHALLENGE, 1, "Rollback salvateur", "Gagne une partie avec le kit Lanczos."),
	PLAY_HASAGI(HUNT, 1, -15, HuntItems.createAdvancementItem(Material.NETHERITE_SWORD, 0, false), AdvancementFrame.TASK, 1, "Hasagi", "Joue une partie avec le kit Hasagi."),
	WIN_HASAGI(PLAY_HASAGI, 2, -15, HuntItems.createAdvancementItem(Material.NETHERITE_SWORD, 0, false), AdvancementFrame.CHALLENGE, 1, "Aseryo", "Gagne une partie avec le kit Hasagi."),
	;
	private static AdvancementManager manager;
	private static SaveFile m_save = null; // TODO

	private Advancement m_achievement;
	private final HuntAchievement m_parent;
	private final String m_background;
	private final float m_x, m_y;
	private final ItemStack m_icon;
	private final AdvancementFrame m_frame;
	private int m_required;
	private final String m_title;
	private final String m_desc;

	private HuntAchievement(String background, ItemStack icon, AdvancementFrame frame, int required, String title, String desc)
	{
		m_achievement = null;
		m_parent = null;
		m_background = background;
		m_x = 0;
		m_y = 0;
		m_icon = icon;
		m_frame = frame;
		m_required = required;
		m_title = title;
		m_desc = desc;
	}

	private HuntAchievement(HuntAchievement parent, float x, float y, ItemStack icon, AdvancementFrame frame, int required, String title, String desc)
	{
		m_achievement = null;
		m_parent = parent;
		m_background = null;
		m_x = x;
		m_y = y;
		m_icon = icon;
		m_frame = frame;
		m_required = required;
		m_title = title;
		m_desc = desc;
	}
	
	public Advancement getAchievement() { return m_achievement; }
	public HuntAchievement getParent() { return m_parent; }
	public String getBackground() { return m_background; }
	public float getX() { return m_x; }
	public float getY() { return m_y; }
	public ItemStack getIcon() { return m_icon; }
	public AdvancementFrame getFrame() { return m_frame; }
	public int getRequired() { return m_required; }
	public String getTitle() { return m_title; }
	public String getDesc() { return m_desc; }
	
	public static void registerAchievements()
	{
		manager = new AdvancementManager(new NameKey("hunt_advancements", "hunt_ad_manager"));
		ArrayList<Advancement> loaded = new ArrayList<>();

		// Generate
		int i = 0;
		for (HuntAchievement ach : values())
		{
			AdvancementDisplay display = new AdvancementDisplay(
				ach.getIcon(),
				new JSONMessage(new TextComponent(ach.getTitle())),
				new JSONMessage(new TextComponent(ach.getDesc())),
				ach.getFrame(),
				AdvancementVisibility.ALWAYS
			);

			if (ach.getBackground() != null)
				display.setBackgroundTexture(ach.getBackground());
			else
				display.setCoordinates(ach.getX(), ach.getY());

			ach.m_achievement = new Advancement(ach.getParent() != null
					? ach.getParent().getAchievement()
					: null,
				new NameKey("hunt_achievements", MessageFormat.format("{0}", i++)),
				display
			);
			ach.m_achievement.setCriteria(new Criteria(ach.getRequired()));
			loaded.add(ach.getAchievement());
		}

		manager.addAdvancement(loaded.toArray(new Advancement[loaded.size()]));
	}
	
	public void award(final Player p, final int amt)
	{
		final int progress = manager.getCriteriaProgress(p, getAchievement());
		if (progress < getAchievement().getCriteria().getRequiredNumber())
			manager.setCriteriaProgress(p, getAchievement(), progress + amt);
	}

	public int get(final Player p)
	{
		return manager.getCriteriaProgress(p, getAchievement());
	}
	
	public static AdvancementManager getManager()
	{
		return manager;
	}

	public static void onStart()
	{
		Bukkit.getPluginManager().registerEvents(new AchievementEvents(), Game.getPlugin());
	}

	public static SaveFile getSave()
	{
		return m_save;
	}
}
