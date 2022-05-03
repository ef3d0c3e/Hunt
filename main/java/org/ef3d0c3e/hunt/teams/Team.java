package org.ef3d0c3e.hunt.teams;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import packets.MetadataHelper;

/**
 * Team class
 */
public class Team
{
	private TeamColor m_color;
	private String m_name;
	private Vector<HuntPlayer> m_playerList; // Is filled with the players that are actively playing in this team (there must not be teamless spectators)
	private int m_score;
	private boolean m_alive;

	private Team m_target;
	private Team m_hunter;

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
		m_playerList = new Vector<HuntPlayer>();
		m_score = 0;
		m_alive = false;
		
		m_target = null;
		m_hunter = null;
	}

	/**
	 * Hook to call when game starts
	 *
	 * Sets up the team's playerlist & start team listener
	 */
	public void start()
	{
		// Populate player list
		for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
		{
			if (set.getValue().getTeam() != this)
				continue;
			
			m_playerList.add(set.getValue());
		}

		if (!m_playerList.isEmpty())
			m_alive = true;
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
	 * Get list of players in team
	 * @return A list containing all team's players
	 */
	public Vector<HuntPlayer> getPlayerList()
	{
		return m_playerList;
	}

	/**
	 * Get the team's score
	 * @return The team's score
	 */
	public int getScore()
	{
		return m_score;
	}

	/**
	 * Sets the team's score
	 * @param score Sets the team's score
	 * @note Low-Level : will not update players' scoreboards
	 */
	public void setScore(final int score)
	{
		m_score = score;
	}

	/**
	 * Get the team's current target (may be null if team has no target)
	 * @return The team's current target
	 */
	public Team getTarget()
	{
		return m_target;
	}

	/**
	 * Set the team's target (team should match with the target's hunter)
	 * @param target The team's target
	 */
	public void setTarget(Team target)
	{
		m_target = target;
	}

	/**
	 * Get the team's current hunter (may be null if team has no hunter)
	 * @return The team's current hunter
	 */
	public Team getHunter()
	{
		return m_hunter;
	}

	/**
	 * Set the team's hunter (team should match with the hunter's target)
	 * @param hunter The team's hunter
	 */
	public void setHunter(Team hunter)
	{
		m_hunter = hunter;
	}

	/**
	 * Returns whether the team still has alive players in it
	 * @return true If team still has alive players
	 */
	public boolean isAlive()
	{
		return m_alive;
	}

	/**
	 * Returns whether the team still has alive players (for round mode) in it
	 * @return true If team still has alive players (for round mode)
	 */
	public boolean isAliveRound()
	{
		m_alive = false;
		for (final HuntPlayer hp : m_playerList)
		{
			if (!hp.isAlive())
				continue;

			m_alive = true;
			break;
		}

		return m_alive;
	}

	public void updateAlive()
	{
		m_alive = false;
		for (final HuntPlayer hp : m_playerList)
		{
			if (!hp.isAlive())
				continue;

			m_alive = true;
			break;
		}

		if (!m_alive)
			m_target = m_hunter = null;
	}

	/**
	 * Interface for `forAll` parameters
	 */
	public interface ForAll
	{
		public void operation(HuntPlayer hp);
	}

	/**
	 * Execute lambda for all players in team
	 * @param f Lambda expressiopn to execute for all players
	 */
	public void forAll(ForAll f)
	{
		for (HuntPlayer hp : m_playerList)
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
		for (final HuntPlayer m : m_playerList)
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

	/**
	 * Event class
	 */
	public static class TeamEvents implements Listener
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
			hp.getTeam().forAll((other) -> {
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


		/**
		 * Set packet watcher & set glowing status once
		 */
		public TeamEvents()
		{
			// Force update at start
			for (final HuntPlayer hp : Game.getPlayerList().values())
			{
				if (hp.getTeam() == null) // No team
					continue;
				updateGlowing(hp);
			}

			ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Game.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA)
			{
				@Override
				public void onPacketSending(PacketEvent ev)
				{
					PacketContainer packet = ev.getPacket();
					final Entity ent = packet.getEntityModifier(ev).read(0);

					if (!(ent instanceof Player))
						return;

					final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
					if (hp.getTeam() == null)
						return;
					final HuntPlayer other = Game.getPlayer(ent.getName());

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
