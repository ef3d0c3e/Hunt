package org.ef3d0c3e.hunt.kits;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Pair;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import packets.DestroyEntityHelper;
import packets.LivingEntityHelper;
import packets.MetadataHelper;

import java.util.*;

/**
 * Kélian's kit's events
 */
public class KitKelianEvents implements Listener
{
	/**
	 * Squids drop diamond detector on death
	 * @param ev Event
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev)
	{
		if (!(ev.getEntity() instanceof Squid))
			return;

		final HuntPlayer killer = Util.getPlayerKiller(ev.getEntity().getKiller());
		if (killer == null || killer.getKit() == null || killer.getKit().getID() != KitID.KELIAN)
			return;

		if (Game.nextPosInt() % 4 == 0)
			ev.getDrops().add(KitKelian.detectorItem);
	}

	/**
	 * Sends shulker to player
	 * @param loc Shulker's location
	 * @param hp Player
	 * @param entityId Shulker's id
	 * @param uuid Shulker's UUID
	 */
	private void sendShulker(final Location loc, final HuntPlayer hp, final int entityId, final UUID uuid)
	{
		LivingEntityHelper ent = new LivingEntityHelper(entityId, uuid);
		ent.setType(LivingEntityHelper.Mobs.SHULKER);
		ent.setPosition(loc.getX(), loc.getY(), loc.getZ());
		final PacketContainer spawnPacket = ent.getPacket();

		MetadataHelper metadata = new MetadataHelper();
		metadata.setStatus((byte)(MetadataHelper.Status.Glowing));
		metadata.setNoGravity(true);
		final PacketContainer metadataPacket = metadata.getPacket(entityId, false);

		// Send packets
		try
		{
			ProtocolManager manager = Game.getProtocolManager();
			manager.sendServerPacket(hp.getPlayer(), spawnPacket);
			manager.sendServerPacket(hp.getPlayer(), metadataPacket);
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Sends every shulkers to player upon joining
	 * @param ev Event
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (!hp.isAlive() || !hp.isPlaying())
			return;
		if (hp.getKit() == null || hp.getKit().getID() != KitID.KELIAN)
			return;

		for (HashMap.Entry<Location, Pair<Integer, UUID>> set : ((KitKelian)hp.getKit()).shulkers.entrySet())
			sendShulker(set.getKey(), hp, set.getValue().first, set.getValue().second);
	}

	/**
	 * Sends every shulkers to player upon changing world
	 * @param ev Event
	 */
	@EventHandler
	public void onWorldChange(final PlayerChangedWorldEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (!hp.isAlive() || !hp.isPlaying())
			return;
		if (hp.getKit() == null || hp.getKit().getID() != KitID.KELIAN)
			return;

		for (HashMap.Entry<Location, Pair<Integer, UUID>> set : ((KitKelian)hp.getKit()).shulkers.entrySet())
			sendShulker(set.getKey(), hp, set.getValue().first, set.getValue().second);
	}

	/**
	 * Reveals diamonds around player
	 * @param ev Eveny
	 */
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null || !ev.getItem().isSimilar(KitKelian.detectorItem))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.KELIAN)
			return;

		ev.setCancelled(true);

		boolean found = false;
		for (double x = -15.0; x <= 15.0; x += 1.0)
		{
			for (double y = -15.0; y <= 15.0; y += 1.0)
			{
				for (double z = -15.0; z <= 15.0; z += 1.0)
				{
					final Location current = new Location(hp.getPlayer().getWorld(),
						((int)hp.getPlayer().getLocation().getX()) + x,
						((int)hp.getPlayer().getLocation().getY()) + y,
						((int)hp.getPlayer().getLocation().getZ()) + z
					);
					if (current.getBlock().getType() != Material.DIAMOND_ORE && current.getBlock().getType() != Material.DEEPSLATE_DIAMOND_ORE)
						continue;
					found = true;

					final int entityId = Game.nextPosInt();
					final UUID uuid = UUID.randomUUID();

					sendShulker(current, hp, entityId, uuid);
					((KitKelian)hp.getKit()).shulkers.put(current, new Pair<>(entityId, uuid));
				}
			}
		}

		if (found)
			ev.getItem().setAmount(ev.getItem().getAmount()-1);
		else
			hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§eRien n'a été trouvé..."));
	}

	/**
	 * Destroys shulker when player excavates nearby
	 * @param ev Event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		if (ev.getClickedBlock().getType() == Material.DIAMOND_ORE || ev.getClickedBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE)
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.KELIAN)
			return;

		Iterator<Map.Entry<Location, Pair<Integer, UUID>>> entryIt = ((KitKelian)hp.getKit()).shulkers.entrySet().iterator();
		ArrayList<Integer> eids = new ArrayList<>();
		while (entryIt.hasNext())
		{
			Map.Entry<Location, Pair<Integer, UUID>> set = entryIt.next();
			if (set.getKey().distanceSquared(ev.getClickedBlock().getLocation()) > 1.0)
				continue;

			eids.add(set.getValue().first);
			entryIt.remove();
		}

		if (eids.isEmpty())
			return;

		try
		{
			ProtocolManager manager = Game.getProtocolManager();
			manager.sendServerPacket(hp.getPlayer(), new DestroyEntityHelper(eids).getPacket());
		}
		catch (Exception e)
		{

		}
	}

	/**
	 * Gives buffs when players are in water or if it is raining
	 */
	public KitKelianEvents()
	{
		// Give buffs when raining & in water
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (HuntPlayer hp : Game.getPlayerList().values())
				{
					if (!hp.isOnline() || !hp.isAlive())
						continue;
					if (hp.getKit() == null || hp.getKit().getID() != KitID.KELIAN)
						continue;
					if (hp.getPlayer().getWorld() != Game.getOverworld())
						continue;
					if (!Game.getOverworld().hasStorm() && hp.getPlayer().getLocation().getBlock().getType() != Material.WATER)
						continue;

					hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 4 + 1, 1));
					hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 4 + 1, 0));
					hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 20 * 4 + 1, 1));
				}
			}
		}.runTaskTimer(Game.getPlugin(), 0, 20 * 4);
	}
}
