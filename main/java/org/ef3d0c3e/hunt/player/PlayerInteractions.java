package org.ef3d0c3e.hunt.player;

import net.minecraft.server.commands.GiveCommand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.game.Game;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

/**
 * Deal with players interactions
 */
public class PlayerInteractions
{
	public static class IRemove
	{
		public ItemStack[] items;

		public IRemove(final ItemStack[] items)
		{
			this.items = items;
		}
	}

	public static class IGive
	{
		public ItemStack[] items;
		public boolean drop;

		public IGive(final ItemStack[] items, final boolean drop)
		{
			this.items = items;
			this.drop = drop;
		}
	}

	public static class IEffect
	{
		public PotionEffect effect;

		public IEffect(final PotionEffect effect)
		{
			this.effect = effect;
		}
	}

	public static class IMessage
	{
		public String message;

		public IMessage(final String message)
		{
			this.message = message;
		}
	}

	public static class IDamage
	{
		public double amount;
		public HuntPlayer attacker;

		public IDamage(final double amount, final HuntPlayer attacker)
		{
			this.amount = amount;
			this.attacker = attacker;
		}
	}

	public static class IGeneric
	{
		public interface Operation
		{
			public void operation(HuntPlayer hp);
		}

		public Operation f;

		public IGeneric(final Operation f)
		{
			this.f = f;
		}
	}

	public static class InteractionsData
	{
		private Queue<IRemove> m_removeQueue;
		private Queue<IGive> m_giveQueue;
		private Queue<IEffect> m_effectQueue;
		private Queue<IMessage> m_messageQueue;
		private Queue<IDamage> m_damageQueue;
		private Queue<IGeneric> m_genericQueue;

		/**
		 * Constructor
		 */
		public InteractionsData()
		{
			m_removeQueue = new LinkedList<IRemove>();
			m_giveQueue = new LinkedList<IGive>();
			m_effectQueue = new LinkedList<IEffect>();
			m_messageQueue = new LinkedList<IMessage>();
			m_damageQueue = new LinkedList<IDamage>();
			m_genericQueue = new LinkedList<IGeneric>();
		}

		/**
		 * Performs all interactions for a player
		 * @param hp Player to perform all interactions for
		 */
		void performInteractions(final HuntPlayer hp)
		{
			if (hp.isAlive())
			{
				while (!m_removeQueue.isEmpty())
				{
					final IGive q = m_giveQueue.poll();

					ListIterator<ItemStack> it = hp.getPlayer().getInventory().iterator();
					while (it.hasNext())
					{
						final ItemStack item = it.next();
						for (final ItemStack match : q.items)
						{
							if (item.isSimilar(match))
								it.set(null);
						}
					}
				}

				while (!m_giveQueue.isEmpty())
				{
					final IGive q = m_giveQueue.poll();


					final HashMap<Integer, ItemStack> rest = hp.getPlayer().getInventory().addItem(q.items);
					// Drop rest on the ground
					if (q.drop)
						for (final ItemStack i : rest.values())
							hp.getPlayer().getWorld().dropItemNaturally(hp.getPlayer().getLocation(), i);
				}

				while (!m_effectQueue.isEmpty())
				{
					final IEffect q = m_effectQueue.poll();

					hp.getPlayer().addPotionEffect(q.effect);
				}

				final int noDamagteTicks = hp.getPlayer().getNoDamageTicks();
				hp.getPlayer().setNoDamageTicks(0);
				while (!m_damageQueue.isEmpty())
				{
					final IDamage q = m_damageQueue.poll();

					if (q.attacker == null)
						hp.getPlayer().damage(q.amount);
					else
						hp.getPlayer().damage(q.amount, (Entity) q.attacker.getPlayer());
				}
				hp.getPlayer().setNoDamageTicks(noDamagteTicks);
			}


			while (!m_genericQueue.isEmpty())
			{
				final IGeneric q = m_genericQueue.poll();

				q.f.operation(hp);
			}
		}

		/**
		 * Give items, effects & send messages to player when they reconnect
		 * @param hp The player
		 * @note If game is paused, will wait until game unpauses to perform pending interactions
		 */
		public void onJoin(HuntPlayer hp)
		{
			if (Game.isPaused())
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						if (!hp.isOnline())
							this.cancel();
						if (Game.isPaused())
							return;

						performInteractions(hp);
						this.cancel();
					}
				}.runTaskTimer(Hunt.plugin, 1, 1);
			else
				performInteractions(hp);
		}

		/**
		 * Add items to the remove queue
		 * @param q The IRemove to add
		 */
		public void add(final IRemove q)
		{
			m_removeQueue.add(q);
		}

		/**
		 * Add items to the give queue
		 * @param q The IGive to add
		 */
		public void add(final IGive q)
		{
			m_giveQueue.add(q);
		}

		/**
		 * Add an effect to the effect queue
		 * @param q The IEffect to add
		 */
		public void add(final IEffect q)
		{
			m_effectQueue.add(q);
		}

		/**
		 * Add a message to the message queue
		 * @param q The IMessage to add
		 */
		public void add(final IMessage q)
		{
			m_messageQueue.add(q);
		}

		/**
		 * Add damage to the damage queue
		 * @param q The IDamage to add
		 */
		public void add(final IDamage q)
		{
			m_damageQueue.add(q);
		}

		/**
		 * Add action to the generic action queue
		 * @param q The IGeneric to add
		 */
		public void add(final IGeneric q)
		{
			m_genericQueue.add(q);
		}
	}

	/**
	 * Clears all pending interactions
	 * @param hp Player to clear interactions for
	 */
	public static void clearAll(final HuntPlayer hp)
	{
		hp.getInteractions().m_removeQueue.clear();
		hp.getInteractions().m_giveQueue.clear();
		hp.getInteractions().m_effectQueue.clear();
		hp.getInteractions().m_messageQueue.clear();
		hp.getInteractions().m_damageQueue.clear();
		hp.getInteractions().m_genericQueue.clear();
	}

	/**
	 * Remove every occurences of items in player's inventory
	 * @param hp The player
	 * @param items The items to remove
	 * @return null if items were removed in player's inventory
	 * 		   An IRemove corresponding to the items that will be removed from the player
	 */
	public static IRemove removeItems(HuntPlayer hp, final ItemStack[] items)
	{
		if (hp.isOnline())
		{
			ListIterator<ItemStack> it = hp.getPlayer().getInventory().iterator();
			while (it.hasNext())
			{
				final ItemStack item = it.next();
				for (final ItemStack match : items)
				{
					if (item.isSimilar(match))
						it.set(null);
				}
			}
		}
		else
		{
			final IRemove q = new IRemove(items);

			hp.getInteractions().add(q);

			return q;
		}

		return null;
	}

	/**
	 * Give an item to a player that may have full inventory or be offline
	 * @param hp The player
	 * @param items The items
	 * @param dropIfFull Drop on the ground if player is full
	 * @param giveOffline Give it to player when they reconnect
	 * @return null If items were placed in the player inventory or dropped on the floor
	 * 		   An IGive corresponding to the items that will be given to the player
	 */
	public static IGive giveItem(HuntPlayer hp, final ItemStack[] items, final boolean dropIfFull, final boolean giveOffline)
	{
		if (hp.isOnline())
		{
			final HashMap<Integer, ItemStack> rest = hp.getPlayer().getInventory().addItem(items);

			// Drop rest on the ground
			if (dropIfFull)
				for (final ItemStack i : rest.values())
					hp.getPlayer().getWorld().dropItemNaturally(hp.getPlayer().getLocation(), i);
		}
		else if (giveOffline)
		{
			final IGive q = new IGive(items, dropIfFull);

			hp.getInteractions().add(q);

			return q;
		}

		return null;
	}

	/**
	 * Give an affect to a player that may be offline
	 * @param hp The player
	 * @param effect The effect
	 * @param giveOffline Give it to player when they reconnect
	 * @return null If the effect were applied to the player
	 * 		   An IEffect corresponding to the effect that will be given to the player
	 */
	public static IEffect giveEffect(HuntPlayer hp, final PotionEffect effect, final boolean giveOffline)
	{
		if (hp.isOnline())
		{
			hp.getPlayer().addPotionEffect(effect);
		}
		else if (giveOffline)
		{
			final IEffect q = new IEffect(effect);

			hp.getInteractions().add(q);

			return q;
		}

		return null;
	}

		/**
		 * Damage a player [by another player] that may be offline
		 * @param hp The player to damage
		 * @param amount Amount to damage player (set it to really high to attempt to kill the player)
		 * @param attacker The attacker (if null, damage will not be from player)
		 * @return null If damage were applied to the player
		 * 		   An IDamage corresponding to the damage that will be inflicted to the player
		 */
	public static IDamage damage(final HuntPlayer hp, final double amount, final HuntPlayer attacker)
	{
		if (hp.isOnline())
		{
			if (attacker == null)
				hp.getPlayer().damage(amount);
			else
				hp.getPlayer().damage(amount, (Entity)attacker.getPlayer());
		}
		else
		{
			final IDamage q = new IDamage(amount, attacker);

			hp.getInteractions().add(q);

			return q;
		}

		return null;
	}

	/**
	 * Schedule a generic task for a player
	 * @param hp The player to schedule the task for
	 * @param f The task
	 * @return null If the task were performed on the player
	 * 		   An IGeneric corresponding to the task that will be performed to the player
	 */
	public static IGeneric schedule(HuntPlayer hp, final IGeneric.Operation f)
	{
		if (hp.isOnline())
		{
			f.operation(hp);
		}
		else
		{
			final IGeneric q = new IGeneric(f);

			hp.getInteractions().add(q);

			return q;
		}

		return null;
	}
}