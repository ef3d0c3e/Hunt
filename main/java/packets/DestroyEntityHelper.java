package packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import java.util.List;

public class DestroyEntityHelper
{
	PacketContainer packet;

	/**
	 * Creates a destroy entity packet with an array of entities
	 * @param eids Entities to destroy
	 */
	public DestroyEntityHelper(final List<Integer> eids)
	{
		packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntLists().write(0, eids);
	}

	/**
	 * Creates an empty destroy entity packet
	 */
	public DestroyEntityHelper()
	{
		packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
	}

	/**
	 * Sets the entities to destroy
	 * @param eids Entities to destroy
	 */
	public void setEntities(final List<Integer> eids)
	{
		packet.getIntLists().write(0, eids);
	}

	/**
	 * Get packet
	 * @return Packet to be sent to players
	 */
	public PacketContainer getPacket()
	{ return packet; }
}
