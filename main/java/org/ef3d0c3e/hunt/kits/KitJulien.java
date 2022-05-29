package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.util.Arrays;

/**
 * Julien's kit
 */
public class KitJulien extends Kit
{
    static ItemStack milkItem;

    @Override
    public String getName() { return "julien"; }
    @Override
    public String getDisplayName() { return "Julien"; }
    @Override
    public ItemStack getDisplayItem()
    {
        return HuntItems.createGuiItem(Material.COBBLESTONE, 0, Kit.itemColor + getDisplayName(),
                Kit.itemLoreColor + "╸ Consomme de la cobblestone lorsqu'il",
                Kit.itemLoreColor + " tape, ce qui augmente ses dégâts",
                Kit.itemLoreColor + "╸ Ne peux pas avoir mieux que l'Épée en Pierre",
                Kit.itemLoreColor + "╸ Peut se mettre en sécurité en échange de 64 Cobblestone",
                Kit.itemLoreColor + "╸ Casse instantanément la Stone (avec une pioche)"
        );
    }

    @Override
    public String[][] getDescription()
    {
        String[][] desc = {
                {
                    "§c ╸ §bVous tapez plus fort à l'épée en échange de cobblestone.::§a↑ +30% de dégâts et donne\n Slowness II (3s) contre\n 4+2×<Niveau de sharpness> cobblestone\n§c↓ -25% de dégâts si pas de cobblestone",
                    "§c ╸ §bVous ne pouvez qu'utiliser des épées en pierre et en bois.",
                    "§c ╸ §bLes Golems de Fer ne vous attaquent pas.",
                    "§c ╸ §bSi vous droppez de la Cobblestone en sneak (et que vous avez plus de 64 Cobblestone dans votre inventaire), une cage apparaît et vous protège des dégâts de chute.",
                    "§c ╸ §bVous cassez la stone et la Stone plus rapidement.",
                    "§c ╸ §bLes autres joueurs peuvent vous milk::§c↓ Ils vous infligent ½❤ de dégâts.",
                }
        };

        return desc;
    }

    static
    {
        milkItem = new ItemStack(Material.MILK_BUCKET, 1);
        {
            ItemMeta meta = milkItem.getItemMeta();
            meta.setDisplayName("§6Lait Magique");
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            milkItem.setItemMeta(meta);
        }
    }

    @Override
    public ItemStack[] getItems()
    {
        return new ItemStack[] { milkItem };
    }

    public KitJulien() {}

    public static class Events implements Listener
    {
        /**
         * Prevents using wrong type of sword
         * @param ev Event
         */
        @EventHandler
        public void onLeftClick(PlayerInteractEvent ev)
        {
            if (ev.getAction() != Action.LEFT_CLICK_AIR && ev.getAction() != Action.LEFT_CLICK_BLOCK)
                return;
            if (ev.getItem() == null)
                return;
            if (!Util.containsMaterial(new Material[]{Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD}, ev.getItem().getType()))
                return;
            final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                return;

            ev.setCancelled(true);
            ev.getPlayer().getLocation().getWorld().dropItem(ev.getPlayer().getLocation(), ev.getItem());
            ev.getItem().setAmount(0);
        }

        @EventHandler
        public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
        {
            if (!(ev.getDamager() instanceof Player))
                return;
            if (((Player) ev.getDamager()).getInventory().getItemInMainHand() == null)
                return;
            final HuntPlayer hp = Game.getPlayer(ev.getDamager().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                return;

            // Drop sword
            if (Util.containsMaterial(new Material[]{Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD}, ((Player) ev.getDamager()).getInventory().getItemInMainHand().getType()))
            {
                ev.setCancelled(true);
                ev.getDamager().getLocation().getWorld().dropItem(ev.getDamager().getLocation(), ((Player) ev.getDamager()).getInventory().getItemInMainHand());
                ((Player) ev.getDamager()).getInventory().getItemInMainHand().setAmount(0);
                return;
            }

            int required = 4;
            if (((Player) ev.getDamager()).getItemInUse() != null)
                required += ((Player) ev.getDamager()).getItemInUse().getEnchantmentLevel(Enchantment.DAMAGE_ALL)*2;
            // Higher damage if has cobblestone
            if (Util.hasItems(hp.getPlayer().getInventory(), Material.COBBLESTONE, 4))
            {
                Util.removeItems(hp.getPlayer().getInventory(), Material.COBBLESTONE, 4);
                ev.setDamage(ev.getDamage() * 1.3);

                hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_STONE_HIT, SoundCategory.MASTER, 16.f, 1.0f);
                ev.getEntity().getWorld().spawnParticle(Particle.BLOCK_CRACK, ev.getEntity().getLocation(), 40, Material.ANVIL.createBlockData());
                if (ev.getEntity() instanceof LivingEntity)
                    ((LivingEntity)ev.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
            }
            else
                ev.setDamage(ev.getDamage() * 0.75);
        }

        /**
         * Prevents picking up wrong sword type
         * @param ev Event
         */
        @EventHandler
        public void onEntityPickupItem(EntityPickupItemEvent ev)
        {
            if (!(ev.getEntity() instanceof Player))
                return;
            final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                return;
            if (!Util.containsMaterial(new Material[]{Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD}, ev.getItem().getItemStack().getType()))
                return;
            ev.setCancelled(true);
        }

        /**
         * Prevents golem from targetting player
         * @param ev Event
         */
        @EventHandler
        public void onEntityTarget(EntityTargetLivingEntityEvent ev)
        {
            if (!(ev.getTarget() instanceof Player) || !(ev.getEntity() instanceof IronGolem))
                return;
            final HuntPlayer hp = Game.getPlayer(ev.getTarget().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                return;
            ev.setCancelled(true);
        }

        /**
         * Breaks stone instantly
         * @param ev Event
         */
        @EventHandler
        public void onBlockDamage(BlockDamageEvent ev)
        {
            if (ev.getBlock().getType() != Material.STONE && ev.getBlock().getType() != Material.COBBLESTONE)
                return;
            final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                return;
            if (hp.getPlayer().getInventory().getItemInMainHand() == null)
                return;
            if (!Util.containsMaterial(Util.pickaxe, ev.getPlayer().getInventory().getItemInMainHand().getType()))
                return;
            ev.setInstaBreak(true);
        }

        /**
         * Creates cobblestone cage around player
         * @param ev Event
         */
        @EventHandler
        public void onPlayerDropItem(PlayerDropItemEvent ev)
        {
            final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
            if (hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                return;
            if (!hp.getPlayer().isSneaking() || hp.getPlayer().getLocation().getY() < hp.getPlayer().getWorld().getMinHeight() + 7)
                return;

            ev.setCancelled(true);

            // We need to wait so that this event's cobblestone is 'restored' to player
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (!hp.isOnline() || !hp.isAlive() || hp.getKit() == null || !(hp.getKit() instanceof KitJulien))
                        return;

                    if (Util.hasItems(ev.getPlayer().getInventory(), Material.COBBLESTONE, 64))
                    {
                        Util.removeItems(ev.getPlayer().getInventory(), Material.COBBLESTONE, 64);
                        for (double x = -3.0; x <= 3.0; x += 1.0)
                            for (double y = -3.0; y <= 3.0; y += 1.0)
                                for (double z = -3.0; z <= 3.0; z += 1.0)
                                    ev.getPlayer().getLocation().clone().add(x, y, z).getBlock().setType(Material.COBBLESTONE);
                        for (double x = -2.0; x <= 2.0; x += 1.0)
                            for (double y = -2.0; y <= 2.0; y += 1.0)
                                for (double z = -2.0; z <= 2.0; z += 1.0)
                                    ev.getPlayer().getLocation().clone().add(x, y, z).getBlock().setType(Material.AIR);
                        for (double x = -2.0; x <= 2.0; x += 1.0)
                            for (double z = -2.0; z <= 2.0; z += 1.0)
                                ev.getPlayer().getLocation().clone().add(x, -2.0, z).getBlock().setType(Material.WATER);
                        for (double x = -2.0; x <= 2.0; x += 1.0)
                            for (double z = -2.0; z <= 2.0; z += 1.0)
                                ev.getPlayer().getLocation().clone().add(x, -3.0, z).getBlock().setType(Material.GLOWSTONE);
                    } else
                        ev.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7Vous n'avez pas assez de cobblestone sur vous!"));
                }
            }.runTaskLater(Game.getPlugin(), 1);
        }

        /**
         * Other players can milk player with a bucket
         * @param ev Event
         */
        @EventHandler
        public void onRightClick(final PlayerInteractEntityEvent ev)
        {
            if (!(ev.getRightClicked() instanceof Player))
                return;
            final HuntPlayer clicker = Game.getPlayer(ev.getPlayer().getName());
            ItemStack item = clicker.getPlayer().getInventory().getItem(ev.getHand());
            if (item.getType() != Material.BUCKET)
                return;
            final HuntPlayer clicked = Game.getPlayer(ev.getRightClicked().getName());
            if (clicked.getKit() == null || (clicked.getKit() instanceof KitJulien))
                return;

            if (!clicker.canDamage(clicked))
                return;

            item.setAmount(item.getAmount()-1);
            PlayerInteractions.giveItem(clicker, new ItemStack[] { KitJulien.milkItem }, true, true);
            PlayerInteractions.damage(clicked, 1, clicker);

            clicker.getPlayer().getWorld().playSound(clicked.getPlayer().getLocation(), Sound.ENTITY_COW_MILK, SoundCategory.MASTER, 16.f, 1.f);
        }

        /**
         * Applies effects when drinking milk
         * @param ev Event
         */
        @EventHandler
        public void onItemConsume(PlayerItemConsumeEvent ev)
        {
            if (!ev.getItem().isSimilar(KitJulien.milkItem))
                return;

            final HuntPlayer hp  = Game.getPlayer(ev.getPlayer().getName());

            ev.setCancelled(true);
            final ItemStack bucket = new ItemStack(Material.BUCKET);
            ev.getPlayer().getItemInUse().setType(bucket.getType());
            ev.getPlayer().getItemInUse().setItemMeta(bucket.getItemMeta());
            ev.getPlayer().getItemInUse().setAmount(1);
            ev.getPlayer().getItemInUse().setData(bucket.getData());

            if (Game.nextPosInt() % 6 == 1)
            {
                // Vomit
                hp.getPlayer().sendMessage("§7Vous n'avez pas bien digéré le lait.");
                hp.getPlayer().addPotionEffects(Arrays.asList(
                    new PotionEffect(PotionEffectType.POISON, 90, 0),
                    new PotionEffect(PotionEffectType.CONFUSION, 140, 0),
                    new PotionEffect(PotionEffectType.HUNGER, 80, 0)
                ));
            }
            else
            {
                // Resistance
                hp.getPlayer().sendMessage("§7Le calcium a renforcé vos os!");
                hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 510, 0));
            }
        }
    }
}