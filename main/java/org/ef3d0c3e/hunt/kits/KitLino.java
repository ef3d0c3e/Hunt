package org.ef3d0c3e.hunt.kits;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.HPDeathEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Lino's kit
 */
public class KitLino extends Kit
{
    @Override
    public String getName() { return "lino"; }
    @Override
    public String getDisplayName() { return "Lino"; }
    @Override
    public ItemStack getDisplayItem()
    {
        return HuntItems.createGuiItem(Material.CROSSBOW, 0, Kit.itemColor + getDisplayName(),
                Kit.itemLoreColor + "╸ Échange de place avec les",
                Kit.itemLoreColor + " entitées touchées à l'arbalette",
                Kit.itemLoreColor + "╸ A une chance qu'un creeper donne",
                Kit.itemLoreColor + " une TNT en mourrant"
        );
    }
    @Override
    public String[][] getDescription()
    {
        String[][] desc = {
                {
                        "§c ╸ §bÉchange de place avec les entitées touchées à l'arbalette. Après avoir changé de place, elles obtiennent slowness, et vous speed.",
                        "§c ╸ §bTuer un creeper a 4/5 chance de donner une TNT.",
                }
        };

        return desc;
    }

    public KitLino()
    {
        arrows = new HashSet<>();
    }

    HashSet<Arrow> arrows;

    public static class Events implements Listener
    {
        @EventHandler
        public void onDeath(final HPDeathEvent ev)
        {
            final HuntPlayer hp = ev.getVictim();
            if (hp.getKit() == null || !(hp.getKit() instanceof KitLino))
                return;

            final KitLino kit = (KitLino)hp.getKit();
            kit.arrows.clear();
        }

        /**
         * Switches shooter and victim's location
         * @param ev Event
         */
        @EventHandler
        public void onEntityDamageByEntity(final EntityDamageByEntityEvent ev)
        {
            final HuntPlayer shooter = Util.getPlayerAttacker(ev);
            if (shooter == null || shooter.getKit() == null || !(shooter.getKit() instanceof KitLino))
                return;

            final KitLino kit = (KitLino)shooter.getKit();
            // This prevents player from shooting an arrow with a bow, then
            // switching to a crossbow (we would be checking held items then)
            if (!kit.arrows.contains(ev.getDamager()))
                return;

            if (!(ev.getEntity() instanceof LivingEntity))
            {
                kit.arrows.remove(ev.getDamager());
                return;
            }

            final Location l = shooter.getPlayer().getLocation();
            shooter.getPlayer().teleport(ev.getEntity());
            ev.getEntity().teleport(l);

            shooter.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
            ((LivingEntity)ev.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));

            l.getWorld().playSound(ev.getEntity().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 8.f, 1.f);
            l.getWorld().spawnParticle(Particle.CRIT_MAGIC, ev.getEntity().getLocation(), 80, 0.4, 0.9, 0.4);
            l.getWorld().playSound(shooter.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 8.f, 1.f);
            l.getWorld().spawnParticle(Particle.CRIT_MAGIC, shooter.getPlayer().getLocation(), 80, 0.4, 0.9, 0.4);

            kit.arrows.remove(ev.getDamager());
        }

        /**
         * Removes arrows from list if it hit
         * @param ev Event
         */
        @EventHandler
        public void onProjectileHit(final ProjectileHitEvent ev)
        {
            if (!(ev.getEntity().getShooter() instanceof Player) || ev.getHitEntity() != null) // Only removes if it hit a block
                return;
            final HuntPlayer hp = Game.getPlayer(((Player)ev.getEntity().getShooter()).getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitLino))
                return;
            ((KitLino)hp.getKit()).arrows.remove(ev.getEntity());
        }

        // FIXME: Arrow shot into the void never despawn...

        /**
         * Stores arrow when player shoots with a crossbow
         * @param ev Event
         */
        @EventHandler
        public void onShoot(final EntityShootBowEvent ev)
        {
            if (ev.getBow() == null || ev.getBow().getType() != Material.CROSSBOW)
                return;
            if (!(ev.getProjectile() instanceof Arrow) || !(ev.getEntity() instanceof Player))
                return;
            final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitLino))
                return;

            ((KitLino)hp.getKit()).arrows.add((Arrow)ev.getProjectile());
        }
    }
}
