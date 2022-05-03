package org.ef3d0c3e.hunt.combat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CombatMechanics implements Listener
{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent ev)
	{
		AttackSpeed.update(ev.getPlayer());
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent ev)
	{
		if (ev.getFrom().getWorld() == ev.getTo().getWorld())
			return;
		// World change
		AttackSpeed.update(ev.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
	{
		if (ev.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
			return;
		if (!(ev.getDamager() instanceof Player))
			return;
		// Could technically work on other entity types
		Player attacker = (Player)ev.getDamager();
		ev.setDamage(AttackDamage.getDamage(attacker.getInventory().getItemInMainHand().getType(), ev.getDamage()));
	}
	
	// Call on reload
	public CombatMechanics()
	{
		for (Player p : Bukkit.getOnlinePlayers())
			AttackSpeed.update(p);
	}
}
