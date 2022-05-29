package org.ef3d0c3e.hunt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import de.freesoccerhdx.advancedworldcreatorapi.AdvancedWorldCreatorAPI;
import de.freesoccerhdx.advancedworldcreatorapi.EnvironmentBuilder;
import de.freesoccerhdx.advancedworldcreatorapi.GeneratorConfiguration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.accessories.AccessoryMenu;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.combat.CombatMechanics;
import org.ef3d0c3e.hunt.commands.CmdCompass;
import org.ef3d0c3e.hunt.commands.CmdInv;
import org.ef3d0c3e.hunt.commands.Commands;
import org.ef3d0c3e.hunt.commands.Completion;
import org.ef3d0c3e.hunt.game.Events;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.generator.HuntGenerator;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.kits.KitMehdi;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.skins.Skin;
import org.ef3d0c3e.hunt.skins.SkinMenu;
import org.ef3d0c3e.hunt.stats.StatSaves;
import org.ef3d0c3e.hunt.stats.StatsMenu;
import org.ef3d0c3e.hunt.teams.TeamMenu;
import de.freesoccerhdx.advancedworldcreatorapi.AdvancedWorldCreator;

import javax.naming.Name;
import java.net.StandardSocketOptions;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

//import net.minecraft.data.worldgen.biome.VanillaBiomes;

public class Hunt extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		final Plugin plugin = this;
		final Server server = getServer();
		
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				ProtocolLibrary l;
				Game.init(plugin, ProtocolLibrary.getProtocolManager());
				StatSaves.init();
				HuntItems.init();
				Skin.init();
				KitMenu.init();
				Round.init();
				Fast.init();

				// Events
				server.getPluginManager().registerEvents(new CombatMechanics(), plugin);
				server.getPluginManager().registerEvents(new Events(), plugin);
				server.getPluginManager().registerEvents(new SkinMenu(), plugin);
				server.getPluginManager().registerEvents(new AccessoryMenu.AccessoryMenuEvents(), plugin);
				server.getPluginManager().registerEvents(new KitMenu(), plugin);
				server.getPluginManager().registerEvents(new TeamMenu(), plugin);
				server.getPluginManager().registerEvents(new CmdCompass.CmdCompassEvents(), plugin);
				server.getPluginManager().registerEvents(new CmdInv.CmdInvEvents(), plugin);
				server.getPluginManager().registerEvents(new StatsMenu(), plugin);

				// Commands
				Commands cmds = new Commands();
				Completion completer = new Completion();
				getCommand("hunt").setExecutor(cmds);
				getCommand("hunt").setTabCompleter(completer);
				getCommand("info").setExecutor(cmds);
				getCommand("info").setTabCompleter(completer);
				getCommand("help").setExecutor(cmds);
				getCommand("help").setTabCompleter(completer);
				getCommand("changelog").setExecutor(cmds);
				getCommand("changelog").setTabCompleter(completer);
				getCommand("kit").setExecutor(cmds);
				getCommand("kit").setTabCompleter(completer);
				getCommand("kititems").setExecutor(cmds);
				getCommand("kititems").setTabCompleter(completer);
				getCommand("kitinfo").setExecutor(cmds);
				getCommand("kitinfo").setTabCompleter(completer);
				getCommand("addteam").setExecutor(cmds);
				getCommand("addteam").setTabCompleter(completer);
				getCommand("delteam").setExecutor(cmds);
				getCommand("delteam").setTabCompleter(completer);
				getCommand("join").setExecutor(cmds);
				getCommand("join").setTabCompleter(completer);
				getCommand("compass").setExecutor(cmds);
				getCommand("compass").setTabCompleter(completer);
				getCommand("inv").setExecutor(cmds);
				getCommand("inv").setTabCompleter(completer);
				getCommand("cibles").setExecutor(cmds);
				getCommand("cibles").setTabCompleter(completer);
				getCommand("accessoires").setExecutor(cmds);
				getCommand("accessoires").setTabCompleter(completer);

				// Achievements
				HuntAchievement.registerAchievements();
		
				server.getConsoleSender().sendMessage("§f[§9Hunt§f] Plugin activé!");

				// MOTD
				Game.getProtocolManager().addPacketListener(new PacketAdapter(
					plugin, ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO)
				{
					@Override
					public void onPacketSending(PacketEvent ev)
					{
						WrappedServerPing ping = ev.getPacket().getServerPings().read(0);
						final String sep = "§d§k|§r";
						String modes = "";
						boolean close = false;
						if (Game.isKitMode())
						{
							close = true;
							modes += sep + "§eKits";
						}
						if (Game.isTeamMode())
						{
							close = true;
							modes += sep + "§eTeams";
						}
						if (Game.isIslandMode())
						{
							close = true;
							modes += sep + "§eÎles";
						}
						if (Game.isRoundMode())
						{
							close = true;
							modes += sep + "§eRound";
						}
						if (Game.isFastMode())
						{
							close = true;
							modes += sep + "§eFast";
						}
						if (close)
							modes += sep;
						if (Game.hasStarted())
							ping.setMotD(MessageFormat.format("    §9§lHUNT §c§m  §f§m     §8§o'{'§m §r   {0}   §8[§c{1}:{2}§8] §f<§o{3}§o En vie§f>", modes,
								Game.getMinutes() < 10 ? MessageFormat.format("0{0}", Game.getMinutes()) : Game.getMinutes(),
								Game.getSeconds() < 10 ? MessageFormat.format("0{0}", Game.getSeconds()) : Game.getSeconds(),
								Game.getPlayerNum()));
						else
							ping.setMotD(MessageFormat.format("    §9§lHUNT §f§m       §8§o'{'§m §r   {0}   §f§lEN ATTENTE", modes));

						ping.setVersionProtocol(999);
						ping.setVersionName(MessageFormat.format("§d<§e1.18+§d> §6» §l{0}/{1}", ping.getPlayersOnline(), ping.getPlayersMaximum()));
						if (Game.nextPosInt() % 4 == 0)
							ping.setPlayers(Arrays.asList(
								new WrappedGameProfile(UUID.randomUUID(), "§0"),
								new WrappedGameProfile(UUID.randomUUID(), "§4█§0██████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4██§0█████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4███§0████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4████§0███████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4█████§0██████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4██████§0█████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4███████§0████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4████████§0███████████████████████"),

								new WrappedGameProfile(UUID.randomUUID(), "§4█████████§f██████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4██████████§f█████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4███████████§f████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4████████████§f███████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4█████████████§f██████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4████████████§f███████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4███████████§f████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4██████████§f█████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4█████████§f██████████████████████"),

								new WrappedGameProfile(UUID.randomUUID(), "§4████████§2███████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4███████§2████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4██████§2█████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4█████§2██████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4████§2███████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4███§2████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4██§2█████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§4█§2██████████████████████████████")
							));
						else
							ping.setPlayers(Arrays.asList(
								/*
								new WrappedGameProfile(UUID.randomUUID(), "§b§n   /\\   "),
								new WrappedGameProfile(UUID.randomUUID(), "§b\\/ §c┣┫§b \\/"),
								new WrappedGameProfile(UUID.randomUUID(), "§b§n/\\   /\\"),
								new WrappedGameProfile(UUID.randomUUID(), "§b   \\/")
								*/
								new WrappedGameProfile(UUID.randomUUID(), "§0"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§1███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████§1█§f███████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████████§1█§f§1█§f§1█§f██████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████§1█§f§1█§f█§1█§f§1█§f█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████§1█§f███§1█§f█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f███████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████§1█§f███§1█§f█████§1█§f███§1█§f████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████§1█§f§1█§f█§1█§f§1█§f█████§1█§f§1█§f█§1█§f§1█§f████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████§1█§f§1█§f§1█§f███████§1█§f§1█§f§1█§f█████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████§1█§f█████████§1█§f██████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████§1█§f█████████§1█§f██████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████§1█§f§1█§f§1█§f███████§1█§f§1█§f§1█§f█████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████§1█§f§1█§f█§1█§f§1█§f█████§1█§f§1█§f█§1█§f§1█§f████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████§1█§f███§1█§f█████§1█§f███§1█§f████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f§1█§f███████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████§1█§f███§1█§f█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████§1█§f§1█§f█§1█§f§1█§f█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████████§1█§f§1█§f§1█§f██████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████§1█§f███████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§1███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████")
								/*
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§1███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████#███████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████████###██████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████##█##█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████#███#█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████#################███████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████#███#█████#███#████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████##█##█████##█##████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████###███████###█████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████#█████████#██████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████#█████████#██████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████###███████###█████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████##█##█████##█##████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f████████#███#█████#███#████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████#################███████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████#███#█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f█████████████##█##█████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f██████████████###██████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████#███████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§1███████████████████████████████"),
								new WrappedGameProfile(UUID.randomUUID(), "§f███████████████████████████████")
								*/
							));
						ev.getPacket().getServerPings().write(0, ping);
					}
				});

				for (Player p : Bukkit.getOnlinePlayers())
				{
					HuntPlayer hp = Game.addPlayer(p);
					hp.onConnect(p);
					hp.setKit(new KitMehdi());
				}
			}
		}.runTaskLater(plugin, 1);

		//WorldCreator wc = new WorldCreator("overworld");
		//wc.environment(World.Environment.NORMAL);
		//wc.generator(GeneratorConfiguration.FLOATING_ISLANDS)
		//wc.generatorSettings("{\"layers\": [{\"block\": \"stone\", \"height\": 1}, {\"block\": \"grass_block\", \"height\": 1}], \"biome\":\"plains\"}");
		//wc.createWorld();

		// TODO
		//AdvancedWorldCreator ac = new AdvancedWorldCreator("overworld");
		//ac.seed(123);
		//GeneratorConfiguration cfg = new GeneratorConfiguration(new NamespacedKey("minecraft", "overworld"));
		//cfg.setDefaultBlock(Material.OAK_PLANKS);
		//cfg.getNoiseGeneration().setMinY(0);
		//cfg.getNoiseGeneration().setHeight(256+128);
		//ac.setGeneratorConfiguration(cfg);
		//ac.setEnvironmentBuilder(new EnvironmentBuilder(new NamespacedKey("minecraft", "overworld")));

		//World world = ac.createWorld();
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String name, String id)
	{
		return new HuntGenerator();
	}

	@Override
	public void onDisable()
	{
		Server server = getServer();
		
		server.getConsoleSender().sendMessage("§f[§9Hunt§f] Plugin désactivé.");
	}
}
