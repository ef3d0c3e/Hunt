package org.ef3d0c3e.hunt.teams;

import java.util.*;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.packets.MetadataHelper;

/**
 * Team class
 */
public class Team
{
	private TeamColor m_color;
	private String m_name;
	private Vector<HuntPlayer> playerList; // Is filled with the players that are actively playing in this team (there must not be teamless spectators)
	@Getter @Setter
	private int score;
	@Getter
	private boolean alive;

	@Getter @Setter
	private Team target;
	@Getter @Setter
	private Team hunter;

	/**
	 * Constructor
	 *
	 * @param color Team's color (must be unique)
	 * @param name Team's name
	 */
	public Team(TeamColor color, String name)
	{
		this.m_color = color;
		this.m_name = name;
		playerList = new Vector<HuntPlayer>();
		score = 0;
		alive = false;
		
		target = null;
		hunter = null;
	}

	/**
	 * Hook to call when game starts
	 *
	 * Sets up the team's playerlist & start team listener
	 */
	public void start()
	{
	}

	/**
	 * Get team's name
	 * @return Team name
	 */
	public String getName()
	{
		return m_name;
	}

	/**
	 * Get team's color
	 * @return Team color
	 */
	public TeamColor getColor()
	{
		return m_color;
	}

	/**
	 * Get colored name for team (uses team color)
	 * @return The colored team name
	 */
	public String getColoredName()
	{
		return Messager.getColored(m_color.getColor() + m_name);
	}

	/**
	 * Returns whether the team still has alive players (for round mode) in it
	 * @return true If team still has alive players (for round mode)
	 */
	public boolean isAliveRound()
	{
		alive = false;
		for (final HuntPlayer hp : playerList)
		{
			if (!hp.isAlive())
				continue;

			alive = true;
			break;
		}

		return alive;
	}

	public void updateAlive()
	{
		alive = false;
		for (final HuntPlayer hp : playerList)
		{
			if (!hp.isAlive())
				continue;

			alive = true;
			break;
		}

		if (!alive)
			target = hunter = null;
	}

	/**
	 * Gets number of players in team
	 * @return Number of players
	 */
	public int size()
	{
		return playerList.size();
	}

	/**
	 * Interface for `forAll` parameters
	 */
	public interface ForAllPlayers
	{
		public void operation(HuntPlayer hp);
	}

	/**
	 * Execute lambda for all players in team
	 * @param f Lambda expressiopn to execute for all players
	 */
	public void forAllPlayers(ForAllPlayers f)
	{
		for (HuntPlayer hp : playerList)
			f.operation(hp);
	}

	/**
	 * Get closest (alive) player to hp in team
	 * @param hp The player
	 * @return The closest player to hp, null if players are offline or in another world
	 */
	public HuntPlayer getClosestPlayer(final HuntPlayer hp)
	{
		double dist = 1e90;
		HuntPlayer ret = null;
		for (final HuntPlayer m : playerList)
		{
			if (m.getPlayer().getWorld() != hp.getPlayer().getWorld() ||
				!m.isOnline() ||
				(Game.isRoundMode() && !m.getRoundData().isAlive()))
				continue;
			final double d = m.getPlayer().getLocation().distanceSquared(hp.getPlayer().getLocation());
			if (d < dist)
			{
				ret = m;
				dist = d;
			}
		}
		return ret;
	}

	static private HashMap<String, Team> teamList = new HashMap<>();

	/**
	 * Gets number of teams
	 * @return Number of teams
	 */
	public static int getTeamListSize()
	{
		return teamList.size();
	}

	/**
	 * Adds a team
	 * @param color Team's color (muse be unique)
	 * @param name Team's name
	 * @return The newly created team
	 */
	public static Team addTeam(TeamColor color, String name)
	{
		Team team;
		team = teamList.get(name);
		if (team != null)
			return team;

		team = new Team(color, name);
		teamList.put(name, team);
		return team;
	}

	/**
	 * Gets team from name
	 * @param name Team's name
	 * @return The team corresponding to name
	 */
	public static Team getTeam(String name)
	{
		return teamList.get(name);
	}

	/**
	 * Deletes team from name
	 * @param name Team's name
	 */
	public static void delTeam(String name)
	{
		Team team = teamList.get(name);
		HuntPlayer.forEach(hp -> {
			if (hp.getTeam() == team)
				hp.setTeam(null);
		});
		teamList.remove(name);
	}

	/**
	 * Get whether or not a team exists
	 * @param name The team's name
	 * @return true If the team exists, false otherwise
	 */
	public static boolean teamExists(String name)
	{
		return teamList.get(name) != null;
	}

	/**
	 * Get whether or not a team color is already taken
	 * @param color The team color
	 * @return true If the team color is taken, false otherwise
	 */
	public static boolean teamColorTaken(TeamColor color)
	{
		for (HashMap.Entry<String, Team> set : teamList.entrySet())
		{
			if (set.getValue().getColor() == color)
			{
				return true;
			}
		}

		return false;
	}

	public interface ForEach
	{
		public void operation(final Team team);
	}

	public static void forEach(final ForEach f)
	{
		for (final var set : teamList.entrySet())
			f.operation(set.getValue());
	}

	/**
	 * Event class
	 */
	public static class Events implements Listener
	{
		/**
		 * Notify other players in team that this player is glowing
		 * Notify the player that his team members are glowing
		 * @param hp The player to set the glowing state of
		 */
		public void updateGlowing(final HuntPlayer hp)
		{
			ProtocolManager manager = Game.getProtocolManager();

			MetadataHelper metadata = new MetadataHelper();
			metadata.setStatus((byte)(MetadataHelper.Status.Glowing));
			metadata.setNoGravity(true);

			final PacketContainer metadataPacket = metadata.getPacket(hp.getPlayer().getEntityId(), false);
			Vector<PacketContainer> members = new Vector<>();

			// Notify others & build up list
			hp.getTeam().forAllPlayers((other) -> {
				if (other == hp)
					return;

				members.add(metadata.getPacket(other.getPlayer().getEntityId(), false));

				try
				{
					manager.sendServerPacket(other.getPlayer(), metadataPacket);
				}
				catch (Exception e) {}
			});

			// Notify player
			for (final PacketContainer packet : members)
			{
				try
				{
					manager.sendServerPacket(hp.getPlayer(), packet);
				}
				catch (Exception e) {}
			}
		}

		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			// Force update
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					HuntPlayer.forEach(hp ->
					{
						if (hp.getTeam() == null || !hp.isOnline()) // No team or offline
							return;
						updateGlowing(hp);
					});
				}
			}.runTaskLater(Hunt.plugin, 20);

			Team.forEach(team -> {
				// Populate player list
				HuntPlayer.forEach(hp -> {
					if (hp.getTeam() != team)
						return;

					team.playerList.add(hp);
				});

				// At this point, all players should be alive
				if (!team.playerList.isEmpty())
					team.alive = true;

				// TODO: Check if this is enough to prevent target selection from selecting empty teams
				if (!team.alive) // TODO: might need to remove using iterators...
					teamList.remove(team);
			});

		}

		/**
		 * Set packet watcher & set glowing status once
		 */
		public Events()
		{

			ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Hunt.plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA)
			{
				@Override
				public void onPacketSending(PacketEvent ev)
				{
					PacketContainer packet = ev.getPacket();
					final Entity ent = packet.getEntityModifier(ev).read(0);

					if (!(ent instanceof Player))
						return;

					final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
					if (hp.getTeam() == null)
						return;
					final HuntPlayer other = HuntPlayer.getPlayer(ev.getPlayer());

					if (other.getTeam() != hp.getTeam())
						return;

					WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(ent).deepClone();
					WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

					byte m = dataWatcher.getByte(0);
					m |= 0x40; // Glowing
					dataWatcher.setObject(0, byteSerializer, m);

					packet.getIntegers().write(0, ent.getEntityId());
					packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

					ev.setPacket(packet);
				}
			});
		}
	}
}
